import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { message } from 'antd'
import paramService, { PARAM_TYPES } from '../services/paramService'
import type { ApiLikeError } from '../utils/errorHandler'

type ParamItem = {
  paramCode?: string
  paramName?: string
  paramValue?: string
  sortOrder?: number
}

type ParamMap = Record<string, ParamItem[]>
type ParamOption = { value: string; label: string }

type ParamContextValue = {
  params: ParamMap
  loading: boolean
  initialized: boolean
  loadParams: () => Promise<void>
  loadParamsByType: (paramType: string) => Promise<ParamItem[]>
  getParamText: (paramType: string, paramCode?: string) => string
  getLevelText: (level?: string) => string
  getTeamText: (team?: string) => string
  getCategoryText: (category?: string) => string
  getPlatformText: (platform?: string) => string
  getTypeText: (type?: string) => string
  getParamOptions: (paramType: string) => ParamOption[]
  getLevelOptions: () => ParamOption[]
  getTeamOptions: () => ParamOption[]
  refreshParams: () => Promise<void>
}

const toApiError = (error: unknown): ApiLikeError => error as ApiLikeError

const emptyParams: ParamMap = {
  [PARAM_TYPES.LEVEL]: [],
  [PARAM_TYPES.TEAM]: [],
  [PARAM_TYPES.CATEGORY]: [],
  [PARAM_TYPES.PLATFORM]: [],
  [PARAM_TYPES.TYPE]: [],
}

const ParamContext = createContext<ParamContextValue | null>(null)

export const ParamProvider = ({ children }: { children: React.ReactNode }) => {
  const [params, setParams] = useState<ParamMap>(emptyParams)
  const [loading, setLoading] = useState(false)
  const [initialized, setInitialized] = useState(false)

  const loadParams = useCallback(async () => {
    if (loading) return

    setLoading(true)
    try {
      const [levelParams, teamParams, platformParams, categoryParams] = await Promise.all([
        paramService.getParamsByType(PARAM_TYPES.LEVEL),
        paramService.getParamsByType(PARAM_TYPES.TEAM),
        paramService.getParamsByType(PARAM_TYPES.PLATFORM),
        paramService.getParamsByType(PARAM_TYPES.CATEGORY),
      ])

      setParams((prev) => ({
        ...prev,
        [PARAM_TYPES.LEVEL]: levelParams || [],
        [PARAM_TYPES.TEAM]: teamParams || [],
        [PARAM_TYPES.PLATFORM]: platformParams || [],
        [PARAM_TYPES.CATEGORY]: categoryParams || [],
      }))
      setInitialized(true)
    } catch (error) {
      console.error('加载参数失败:', toApiError(error))
    } finally {
      setLoading(false)
    }
  }, [loading])

  const loadParamsByType = useCallback(async (paramType: string) => {
    try {
      const typeParams = (await paramService.getParamsByType(paramType)) || []
      setParams((prev) => ({
        ...prev,
        [paramType]: typeParams,
      }))
      return typeParams
    } catch (error) {
      console.error(`加载${paramType}参数失败:`, toApiError(error))
      return []
    }
  }, [])

  const getParamText = useCallback(
    (paramType: string, paramCode?: string) => {
      if (!paramCode) return '-'
      const typeParams = params[paramType]
      if (!typeParams?.length) return paramCode

      const normalized = String(paramCode).toLowerCase()
      const byCode = typeParams.find((p) => p.paramCode?.toLowerCase() === normalized)
      if (byCode?.paramName) return byCode.paramName

      const byName = typeParams.find((p) => p.paramName === paramCode)
      if (byName?.paramName) return byName.paramName

      const byValue = typeParams.find((p) => p.paramValue === paramCode)
      if (byValue?.paramName) return byValue.paramName

      const enToCnMap: Record<string, string> = {
        junior: '初级',
        senior: '中级',
        advanced: '高级',
        expert: '资深',
        frontend: '前端',
        backend: '后端',
        mobile: '移动端',
        ai: '人工智能',
      }
      const mapped = enToCnMap[normalized]
      if (mapped) {
        const mappedItem = typeParams.find((p) => p.paramName === mapped)
        if (mappedItem?.paramName) return mappedItem.paramName
      }

      return paramCode
    },
    [params],
  )

  const getLevelText = useCallback((level?: string) => getParamText(PARAM_TYPES.LEVEL, level), [getParamText])
  const getTeamText = useCallback((team?: string) => getParamText(PARAM_TYPES.TEAM, team), [getParamText])
  const getCategoryText = useCallback((category?: string) => getParamText(PARAM_TYPES.CATEGORY, category), [getParamText])
  const getPlatformText = useCallback((platform?: string) => getParamText(PARAM_TYPES.PLATFORM, platform), [getParamText])
  const getTypeText = useCallback((type?: string) => getParamText(PARAM_TYPES.TYPE, type), [getParamText])

  const getParamOptions = useCallback(
    (paramType: string): ParamOption[] => {
      const typeParams = params[paramType] || []
      return [...typeParams]
        .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
        .map((param) => ({
          value: param.paramCode || '',
          label: param.paramName || param.paramCode || '',
        }))
        .filter((item) => item.value)
    },
    [params],
  )

  const getLevelOptions = useCallback(() => getParamOptions(PARAM_TYPES.LEVEL), [getParamOptions])
  const getTeamOptions = useCallback(() => getParamOptions(PARAM_TYPES.TEAM), [getParamOptions])

  const refreshParams = useCallback(async () => {
    paramService.clearCache()
    await loadParams()
    message.success('参数已刷新')
  }, [loadParams])

  useEffect(() => {
    loadParams()
  }, [loadParams])

  const value = useMemo<ParamContextValue>(
    () => ({
      params,
      loading,
      initialized,
      loadParams,
      loadParamsByType,
      getParamText,
      getLevelText,
      getTeamText,
      getCategoryText,
      getPlatformText,
      getTypeText,
      getParamOptions,
      getLevelOptions,
      getTeamOptions,
      refreshParams,
    }),
    [
      params,
      loading,
      initialized,
      loadParams,
      loadParamsByType,
      getParamText,
      getLevelText,
      getTeamText,
      getCategoryText,
      getPlatformText,
      getTypeText,
      getParamOptions,
      getLevelOptions,
      getTeamOptions,
      refreshParams,
    ],
  )

  return <ParamContext.Provider value={value}>{children}</ParamContext.Provider>
}

export const useParam = () => {
  const context = useContext(ParamContext)
  if (!context) throw new Error('useParam must be used within a ParamProvider')
  return context
}

export default ParamContext

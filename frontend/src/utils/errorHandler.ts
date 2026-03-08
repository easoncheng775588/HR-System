import { message } from 'antd'
import { getErrorMessage } from './errorMessages'

type NavigateFn = (path: string) => void

interface ApiErrorResponseData {
  errorMsg?: string
}

interface ApiErrorResponse {
  status?: number
  data?: ApiErrorResponseData
}

export interface ApiLikeError {
  code?: string
  message?: string
  request?: unknown
  response?: ApiErrorResponse
}

export const handleApiError = (
  error: ApiLikeError,
  defaultMessage = `${getErrorMessage('common.operationFailed')}，请稍后重试`,
  navigate: NavigateFn | null = null,
): string => {
  console.error('API Error:', error)

  let errorMsg = defaultMessage

  if (error.code === 'ECONNABORTED') {
    errorMsg = getErrorMessage('common.timeout')
  } else if (error.response) {
    const { status, data } = error.response

    switch (status) {
      case 401:
        errorMsg = getErrorMessage('common.sessionExpired')
        localStorage.removeItem('token')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
        break
      case 403:
        errorMsg = getErrorMessage('common.permissionDenied')
        break
      case 404:
        errorMsg = getErrorMessage('common.notFound')
        break
      case 500:
        errorMsg = getErrorMessage('common.serverError')
        break
      default:
        errorMsg = data?.errorMsg || defaultMessage
    }
  } else if (error.request) {
    errorMsg = getErrorMessage('common.networkError')
  }

  message.error(errorMsg)

  if (navigate) {
    navigate('/recruitment-request')
  }

  return errorMsg
}

export const handleSubmitError = (
  error: ApiLikeError,
  isEditMode: boolean,
  navigate: NavigateFn | null = null,
): void => {
  const defaultMessage = isEditMode
    ? `${getErrorMessage('recruitmentRequest.updateFailed')}，请稍后重试`
    : `${getErrorMessage('recruitmentRequest.submitFailed')}，请稍后重试`

  handleApiError(error, defaultMessage, navigate)
}

export const handleSaveDraftError = (
  error: ApiLikeError,
  navigate: NavigateFn | null = null,
): void => {
  handleApiError(error, `${getErrorMessage('common.operationFailed')}，请稍后重试`, navigate)
}

export const handleLoadError = (
  error: ApiLikeError,
  navigate: NavigateFn | null = null,
): void => {
  handleApiError(error, `${getErrorMessage('common.operationFailed')}，请稍后重试`, navigate)
}

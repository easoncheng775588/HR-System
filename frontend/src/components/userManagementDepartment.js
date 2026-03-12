const findTeamConfig = (departmentOptions, teamName) =>
  (departmentOptions || []).find((item) => item.teamName === teamName)

export const getTeamOptions = (departmentOptions = []) =>
  departmentOptions
    .filter((item) => item?.teamName)
    .map((item) => ({
      label: item.teamName,
      value: item.teamName,
    }))

export const getGroupOptions = (departmentOptions = [], teamName) => {
  const teamConfig = findTeamConfig(departmentOptions, teamName)
  return (teamConfig?.groupOptions || [])
    .filter((item) => item?.groupName)
    .map((item) => ({
      label: item.groupName,
      value: item.groupName,
    }))
}

export const validateDepartmentSelection = ({
  teamName,
  groupName,
  departmentOptions = [],
}) => {
  if (!teamName) return '团队名称不能为空'

  const teamConfig = findTeamConfig(departmentOptions, teamName)
  if (!teamConfig) return '团队名称不存在'

  const normalizedGroupName = typeof groupName === 'string' ? groupName.trim() : groupName
  if (!teamConfig.requiresGroup) {
    return normalizedGroupName ? '所选团队不允许填写室组名称' : null
  }

  if (!normalizedGroupName) return '所选团队要求必须填写室组名称'

  const groupMatched = (teamConfig.groupOptions || []).some(
    (item) => item.groupName === normalizedGroupName,
  )

  return groupMatched ? null : '室组名称不存在或不属于该团队'
}

export const normalizeDepartmentPayload = (values, departmentOptions = []) => {
  const teamConfig = findTeamConfig(departmentOptions, values?.teamName)

  return {
    ...values,
    teamName: values?.teamName,
    groupName: teamConfig?.requiresGroup ? values?.groupName || undefined : undefined,
  }
}

export const shouldRequireGroup = (departmentOptions = [], teamName) =>
  Boolean(findTeamConfig(departmentOptions, teamName)?.requiresGroup)

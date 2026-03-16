export const RECRUITMENT_LEVEL_OPTIONS = ['PT', 'PG', 'AP', 'ASA', 'SA', 'SSA', '初级行政', '中级行政', '高级行政']

const toFallbackOptions = (values) => values.map((value) => ({ value, label: value }))

export const buildEntryLevelOptions = (levelOptions) => {
  const normalized = (levelOptions || []).filter((item) => item?.value && item?.label)
  return normalized.length > 0 ? normalized : toFallbackOptions(RECRUITMENT_LEVEL_OPTIONS)
}

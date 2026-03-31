import React from 'react'
import { Tooltip } from 'antd'
import { InfoCircleOutlined } from '@ant-design/icons'

const levelHintContent = `一、各级别对应最低年限要求：
SSA (>7 years)
SA (>5 years)
ASA (>4 years)
AP (>3 years)
PG (>1 years)
PT (<1 years)
行政（初级）：>1 years
行政（中级）：>3 years
行政（高级）：>5 years
行政（资深）：>8 years`

type RecruitmentLevelHintProps = {
  label?: string
  content?: string
}

const RecruitmentLevelHint = ({
  label = '建议级别',
  content = levelHintContent,
}: RecruitmentLevelHintProps) => (
  <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
    <span>{label}</span>
    <Tooltip title={<div style={{ whiteSpace: 'pre-line' }}>{content}</div>} placement="top">
      <InfoCircleOutlined style={{ color: 'rgba(0, 0, 0, 0.45)', cursor: 'help' }} />
    </Tooltip>
  </span>
)

export default RecruitmentLevelHint

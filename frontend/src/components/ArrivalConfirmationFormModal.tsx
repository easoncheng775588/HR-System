import React, { useEffect, useMemo } from 'react'
import { DatePicker, Form, Modal, Select } from 'antd'
import { buildCandidateLabel, matchOption } from './arrivalConfirmationHelpers'

interface ArrivalConfirmationFormModalProps {
  open: boolean
  submitting: boolean
  form: any
  candidates: any[]
  suppliers: any[]
  supplierHrs: any[]
  roomManagers: any[]
  orgUnits: any[]
  levelOptions: Array<{ value: string; label: string }>
  onCancel: () => void
  onSubmit: () => void
}

const ArrivalConfirmationFormModal: React.FC<ArrivalConfirmationFormModalProps> = ({
  open,
  submitting,
  form,
  candidates,
  suppliers,
  supplierHrs,
  roomManagers,
  orgUnits,
  levelOptions,
  onCancel,
  onSubmit,
}) => {
  const selectedEntryRecordId = Form.useWatch('entryRecordId', form)

  const candidateMap = useMemo(() => {
    const map = new Map<number, any>()
    ;(candidates || []).forEach((item) => {
      map.set(Number(item.entryRecordId), item)
    })
    return map
  }, [candidates])

  const selectedCandidate = useMemo(() => {
    if (selectedEntryRecordId === undefined || selectedEntryRecordId === null) return null
    return candidateMap.get(Number(selectedEntryRecordId)) || null
  }, [candidateMap, selectedEntryRecordId])

  useEffect(() => {
    if (!selectedCandidate) {
      return
    }
    form.setFieldsValue({
      supplierId: selectedCandidate.supplierId ?? undefined,
      supplierHrUserId: selectedCandidate.supplierHrUserId || undefined,
      targetOrgUnitName: selectedCandidate.targetOrgUnitName || undefined,
      roomManagerUserId: selectedCandidate.recommendedRoomManagerUserId || undefined,
    })
  }, [form, selectedCandidate])

  return (
    <Modal
      title="发起到岗确认"
      open={open}
      onCancel={onCancel}
      onOk={onSubmit}
      confirmLoading={submitting}
      destroyOnClose
      width={720}
    >
      <Form form={form} layout="vertical" preserve={false}>
        <Form.Item
          name="entryRecordId"
          label="到岗人员"
          rules={[{ required: true, message: '请选择到岗人员' }]}
        >
          <Select
            showSearch
            placeholder="请选择到岗人员"
            filterOption={matchOption}
            options={(candidates || []).map((item) => ({
              value: item.entryRecordId,
              label: buildCandidateLabel(item),
            }))}
          />
        </Form.Item>

        <Form.Item
          name="supplierId"
          label="所属外包供应商"
          rules={[{ required: true, message: '请选择所属外包供应商' }]}
        >
          <Select
            showSearch
            placeholder="请选择所属外包供应商"
            filterOption={matchOption}
            options={(suppliers || []).map((item) => ({
              value: item.supplierId,
              label: item.supplierName,
            }))}
          />
        </Form.Item>

        <Form.Item
          name="supplierHrUserId"
          label="供应商HR"
          rules={[{ required: true, message: '请选择供应商HR' }]}
        >
          <Select
            showSearch
            disabled
            placeholder="系统将根据候选人自动带出供应商HR"
            filterOption={matchOption}
            options={(supplierHrs || []).map((item) => ({
              value: item.userId,
              label: `${item.realName} / ${item.department || '-'}`,
            }))}
          />
        </Form.Item>

        <Form.Item
          name="targetOrgUnitName"
          label="用人团队/部室"
          rules={[{ required: true, message: '请选择用人团队/部室' }]}
        >
          <Select
            showSearch
            placeholder="请选择用人团队/部室"
            filterOption={matchOption}
            options={(orgUnits || []).map((item) => ({
              value: item.unitName,
              label: item.parentUnitName ? `${item.parentUnitName} / ${item.unitName}` : item.unitName,
            }))}
          />
        </Form.Item>

        <Form.Item
          name="roomManagerUserId"
          label="所属室经理"
          rules={[{ required: true, message: '请选择所属室经理' }]}
        >
          <Select
            showSearch
            placeholder="请选择所属室经理"
            filterOption={matchOption}
            options={(roomManagers || []).map((item) => ({
              value: item.userId,
              label: `${item.realName} / ${item.department || '-'}`,
            }))}
          />
        </Form.Item>

        <Form.Item
          name="entryDate"
          label="人员进场日期"
          rules={[{ required: true, message: '请选择人员进场日期' }]}
        >
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item
          name="positionLevel"
          label="人员级别"
          rules={[{ required: true, message: '请选择人员级别' }]}
        >
          <Select
            showSearch
            placeholder="请选择人员级别"
            filterOption={matchOption}
            options={levelOptions}
          />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default ArrivalConfirmationFormModal

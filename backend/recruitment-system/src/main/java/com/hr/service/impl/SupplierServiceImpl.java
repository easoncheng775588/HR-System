package com.hr.service.impl;

import com.hr.entity.Supplier;
import com.hr.mapper.SupplierMapper;
import com.hr.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class SupplierServiceImpl implements SupplierService {

    private static final List<String> VALID_STATUS = Arrays.asList("ACTIVE", "PAUSED", "INVALID");

    @Autowired
    private SupplierMapper supplierMapper;

    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierMapper.getAllSuppliers();
    }

    @Override
    public Supplier getSupplierById(Long supplierId) {
        Supplier supplier = supplierMapper.getSupplierById(supplierId);
        if (supplier == null) {
            return null;
        }
        supplier.setUserIds(supplierMapper.getUserIdsBySupplierId(supplierId));
        return supplier;
    }

    @Override
    @Transactional
    public int createSupplier(Supplier supplier) {
        Integer nameCount = supplierMapper.countBySupplierName(supplier.getSupplierName(), null);
        if (nameCount != null && nameCount > 0) {
            throw new RuntimeException("Supplier name already exists");
        }

        Date now = new Date();
        if (supplier.getStatus() == null || supplier.getStatus().trim().isEmpty()) {
            supplier.setStatus("ACTIVE");
        }
        validateStatus(supplier.getStatus());

        if (supplier.getCreateTime() == null) {
            supplier.setCreateTime(now);
        }
        if (supplier.getUpdateTime() == null) {
            supplier.setUpdateTime(now);
        }
        if (supplier.getCreateUserId() == null || supplier.getCreateUserId().trim().isEmpty()) {
            supplier.setCreateUserId("1001");
        }
        if (supplier.getCreateUserName() == null || supplier.getCreateUserName().trim().isEmpty()) {
            supplier.setCreateUserName("SYSTEM");
        }
        if (supplier.getUpdateUserId() == null || supplier.getUpdateUserId().trim().isEmpty()) {
            supplier.setUpdateUserId(supplier.getCreateUserId());
        }
        if (supplier.getUpdateUserName() == null || supplier.getUpdateUserName().trim().isEmpty()) {
            supplier.setUpdateUserName(supplier.getCreateUserName());
        }

        int affected = supplierMapper.insertSupplier(supplier);
        if (affected > 0) {
            bindSupplierUsers(
                supplier.getSupplierId(),
                supplier.getUserIds(),
                supplier.getCreateUserId(),
                supplier.getCreateUserName()
            );
        }
        return affected;
    }

    @Override
    @Transactional
    public int updateSupplier(Long supplierId, Supplier supplier) {
        Supplier existing = supplierMapper.getSupplierById(supplierId);
        if (existing == null) {
            return 0;
        }

        Integer nameCount = supplierMapper.countBySupplierName(supplier.getSupplierName(), supplierId);
        if (nameCount != null && nameCount > 0) {
            throw new RuntimeException("Supplier name already exists");
        }
        validateStatus(supplier.getStatus());

        supplier.setSupplierId(supplierId);
        supplier.setUpdateTime(new Date());
        if (supplier.getUpdateUserId() == null || supplier.getUpdateUserId().trim().isEmpty()) {
            supplier.setUpdateUserId("1001");
        }
        if (supplier.getUpdateUserName() == null || supplier.getUpdateUserName().trim().isEmpty()) {
            supplier.setUpdateUserName("SYSTEM");
        }

        int affected = supplierMapper.updateSupplier(supplier);
        if (affected > 0) {
            bindSupplierUsers(
                supplierId,
                supplier.getUserIds(),
                supplier.getUpdateUserId(),
                supplier.getUpdateUserName()
            );
        }
        return affected;
    }

    @Override
    @Transactional
    public int deleteSupplier(Long supplierId) {
        Supplier supplier = supplierMapper.getSupplierById(supplierId);
        if (supplier == null) {
            return 0;
        }
        supplierMapper.deleteSupplierUsersBySupplierId(supplierId);
        return supplierMapper.deleteSupplier(supplierId);
    }

    private void bindSupplierUsers(Long supplierId, List<String> userIds, String operatorId, String operatorName) {
        supplierMapper.deleteSupplierUsersBySupplierId(supplierId);
        if (userIds != null && !userIds.isEmpty()) {
            supplierMapper.batchInsertSupplierUsers(supplierId, userIds, operatorId, operatorName);
        }
    }

    private void validateStatus(String status) {
        if (status == null || !VALID_STATUS.contains(status)) {
            throw new RuntimeException("Invalid supplier status");
        }
    }
}


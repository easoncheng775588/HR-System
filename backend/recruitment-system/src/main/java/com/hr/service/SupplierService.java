package com.hr.service;

import com.hr.entity.Supplier;

import java.util.List;

public interface SupplierService {

    List<Supplier> getAllSuppliers();

    Supplier getSupplierById(Long supplierId);

    int createSupplier(Supplier supplier);

    int updateSupplier(Long supplierId, Supplier supplier);

    int deleteSupplier(Long supplierId);
}


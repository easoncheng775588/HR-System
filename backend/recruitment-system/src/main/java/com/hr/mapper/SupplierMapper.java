package com.hr.mapper;

import com.hr.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SupplierMapper {

    List<Supplier> getAllSuppliers();

    Supplier getSupplierById(@Param("supplierId") Long supplierId);

    int insertSupplier(Supplier supplier);

    int updateSupplier(Supplier supplier);

    int deleteSupplier(@Param("supplierId") Long supplierId);

    int deleteSupplierUsersBySupplierId(@Param("supplierId") Long supplierId);

    int batchInsertSupplierUsers(@Param("supplierId") Long supplierId,
                                 @Param("userIds") List<String> userIds,
                                 @Param("createUserId") String createUserId,
                                 @Param("createUserName") String createUserName);

    List<String> getUserIdsBySupplierId(@Param("supplierId") Long supplierId);

    Integer countBySupplierName(@Param("supplierName") String supplierName,
                                @Param("excludeSupplierId") Long excludeSupplierId);
}


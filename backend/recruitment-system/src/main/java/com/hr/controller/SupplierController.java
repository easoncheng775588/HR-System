package com.hr.controller;

import com.hr.entity.Supplier;
import com.hr.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public Map<String, Object> getAllSuppliers() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Supplier> suppliers = supplierService.getAllSuppliers();
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", suppliers);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to get supplier list: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @GetMapping("/{supplierId}")
    public Map<String, Object> getSupplierById(@PathVariable Long supplierId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Supplier supplier = supplierService.getSupplierById(supplierId);
            if (supplier == null) {
                result.put("returnCode", "ERR0004");
                result.put("errorMsg", "Supplier does not exist");
                result.put("body", null);
                return result;
            }
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", supplier);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to get supplier detail: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> createSupplier(@RequestBody Supplier supplier) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (supplier == null || supplier.getSupplierName() == null || supplier.getSupplierName().trim().isEmpty()) {
                result.put("returnCode", "ERR0003");
                result.put("errorMsg", "Supplier name cannot be empty");
                result.put("body", null);
                return result;
            }

            int count = supplierService.createSupplier(supplier);
            if (count > 0) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "Supplier created successfully");
            } else {
                result.put("returnCode", "ERR0002");
                result.put("errorMsg", "Failed to create supplier");
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to create supplier: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @PutMapping("/{supplierId}")
    public Map<String, Object> updateSupplier(@PathVariable Long supplierId, @RequestBody Supplier supplier) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (supplier == null || supplier.getSupplierName() == null || supplier.getSupplierName().trim().isEmpty()) {
                result.put("returnCode", "ERR0003");
                result.put("errorMsg", "Supplier name cannot be empty");
                result.put("body", null);
                return result;
            }

            int count = supplierService.updateSupplier(supplierId, supplier);
            if (count > 0) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "Supplier updated successfully");
            } else {
                result.put("returnCode", "ERR0004");
                result.put("errorMsg", "Supplier does not exist");
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to update supplier: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @DeleteMapping("/{supplierId}")
    public Map<String, Object> deleteSupplier(@PathVariable Long supplierId) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = supplierService.deleteSupplier(supplierId);
            if (count > 0) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "Supplier deleted successfully");
            } else {
                result.put("returnCode", "ERR0004");
                result.put("errorMsg", "Supplier does not exist");
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to delete supplier: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
}


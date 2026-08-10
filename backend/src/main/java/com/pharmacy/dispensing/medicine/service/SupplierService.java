package com.pharmacy.dispensing.medicine.service;

import com.pharmacy.dispensing.common.exception.ResourceNotFoundException;
import com.pharmacy.dispensing.medicine.dto.SupplierRequest;
import com.pharmacy.dispensing.medicine.dto.SupplierResponse;
import com.pharmacy.dispensing.medicine.entity.Supplier;
import com.pharmacy.dispensing.medicine.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = new Supplier();
        mapRequestToEntity(request, supplier);
        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> findAll() {
        return supplierRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return mapToResponse(supplier);
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        mapRequestToEntity(request, supplier);
        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void deactivate(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    // ---- Mapping helpers ----

    private void mapRequestToEntity(SupplierRequest request, Supplier supplier) {
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
    }

    public SupplierResponse mapToResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getActive(),
                supplier.getCreatedAt()
        );
    }
}

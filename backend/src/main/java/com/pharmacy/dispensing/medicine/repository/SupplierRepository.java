package com.pharmacy.dispensing.medicine.repository;

import com.pharmacy.dispensing.medicine.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByActiveTrue();

    Optional<Supplier> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}

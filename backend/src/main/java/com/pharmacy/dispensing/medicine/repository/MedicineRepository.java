package com.pharmacy.dispensing.medicine.repository;

import com.pharmacy.dispensing.medicine.entity.Medicine;
import com.pharmacy.dispensing.medicine.entity.MedicineCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Page<Medicine> findByActiveTrue(Pageable pageable);

    Optional<Medicine> findByIdAndActiveTrue(Long id);

    boolean existsByNameIgnoreCaseAndActiveTrue(String name);

    /**
     * Full-text style search across name and generic name (case-insensitive).
     * Searches only active medicines.
     */
    @Query("""
            SELECT m FROM Medicine m
            WHERE m.active = true
              AND (LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<Medicine> searchByNameOrGenericName(@Param("query") String query, Pageable pageable);

    Page<Medicine> findByCategoryAndActiveTrue(MedicineCategory category, Pageable pageable);
}

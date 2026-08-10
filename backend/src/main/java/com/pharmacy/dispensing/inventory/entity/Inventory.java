package com.pharmacy.dispensing.inventory.entity;

import com.pharmacy.dispensing.medicine.entity.Medicine;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root representing the stock position for a single {@link Medicine}.
 * <p>
 * {@code totalQuantity} is the sum of all non-recalled, non-disposed batch quantities.
 * It is updated atomically whenever a {@link MedicineBatch} quantity changes.
 * <p>
 * {@code @Version} enables optimistic locking to prevent concurrent update races
 * during high-throughput dispensing scenarios.
 */
@Entity
@Table(name = "inventory")
@Audited
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** One-to-one relationship: each medicine has exactly one inventory record. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_id", nullable = false, unique = true)
    private Medicine medicine;

    /**
     * Aggregate available quantity across all ACTIVE batches.
     * Kept in sync by {@link com.pharmacy.dispensing.inventory.service.MedicineBatchService}.
     */
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 50;

    /**
     * JPA optimistic locking version field.
     * Prevents concurrent dispensing transactions from corrupting stock counts.
     */
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicineBatch> batches = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Inventory() {}

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<MedicineBatch> getBatches() { return batches; }
    public void setBatches(List<MedicineBatch> batches) { this.batches = batches; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

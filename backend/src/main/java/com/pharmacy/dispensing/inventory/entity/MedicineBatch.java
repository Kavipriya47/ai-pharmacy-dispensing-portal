package com.pharmacy.dispensing.inventory.entity;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single procurement lot (batch) of a medicine.
 * <p>
 * Every time stock is received from a supplier, a new {@code MedicineBatch} record
 * is created with a unique {@code batchNumber}. This allows per-batch expiry tracking
 * and FEFO (First-Expiry-First-Out) dispensing selection.
 * <p>
 * {@code @Version} enables optimistic locking to prevent concurrent dispensing
 * from over-drawing the same batch.
 */
@Entity
@Table(name = "medicine_batches")
@Audited
public class MedicineBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Column(name = "batch_number", nullable = false, length = 100)
    private String batchNumber;

    @Column(length = 200)
    private String manufacturer;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "quantity_received", nullable = false)
    private Integer quantityReceived;

    @Column(name = "quantity_remaining", nullable = false)
    private Integer quantityRemaining;

    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BatchStatus status = BatchStatus.ACTIVE;

    /**
     * JPA optimistic locking version field.
     * Prevents concurrent dispensing from over-drawing this batch.
     */
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @NotAudited
    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryTransaction> transactions = new ArrayList<>();

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

    public MedicineBatch() {}

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public LocalDate getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(LocalDate manufacturingDate) { this.manufacturingDate = manufacturingDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Integer getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }

    public Integer getQuantityRemaining() { return quantityRemaining; }
    public void setQuantityRemaining(Integer quantityRemaining) { this.quantityRemaining = quantityRemaining; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<InventoryTransaction> getTransactions() { return transactions; }
    public void setTransactions(List<InventoryTransaction> transactions) { this.transactions = transactions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

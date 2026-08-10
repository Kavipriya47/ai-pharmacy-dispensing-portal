package com.pharmacy.dispensing.inventory.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Immutable ledger entry recording every stock movement against a {@link MedicineBatch}.
 * <p>
 * This table is <strong>insert-only</strong> — records are never updated or deleted.
 * It provides a complete audit trail answering: <em>who moved how much stock, when, and why</em>.
 * <p>
 * Unlike Hibernate Envers (which tracks entity field changes), this table records
 * <em>business intent</em>: RECEIVE, DISPENSE, DISPOSE, ADJUSTMENT, RECALL, RETURN.
 */
@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private MedicineBatch batch;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    /** Always positive. Direction is implied by {@code transactionType}. */
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    /** Free-text reason or additional context. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Optional external reference: dispensation record ID, purchase order number, etc.
     * Enables cross-table traceability.
     */
    @Column(name = "reference_id", length = 200)
    private String referenceId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public InventoryTransaction() {}

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MedicineBatch getBatch() { return batch; }
    public void setBatch(MedicineBatch batch) { this.batch = batch; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

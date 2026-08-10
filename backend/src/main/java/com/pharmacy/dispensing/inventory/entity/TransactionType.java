package com.pharmacy.dispensing.inventory.entity;

/**
 * Categorises every entry in the {@code inventory_transactions} ledger.
 * <ul>
 *   <li>{@code RECEIVE}    – new stock received from supplier</li>
 *   <li>{@code DISPENSE}   – stock removed for patient dispensation</li>
 *   <li>{@code DISPOSE}    – expired / damaged stock written off</li>
 *   <li>{@code ADJUSTMENT} – manual stock count correction</li>
 *   <li>{@code RECALL}     – batch recalled by manufacturer</li>
 *   <li>{@code RETURN}     – previously dispensed medication returned</li>
 * </ul>
 */
public enum TransactionType {
    RECEIVE,
    DISPENSE,
    DISPOSE,
    ADJUSTMENT,
    RECALL,
    RETURN
}

package com.pharmacy.dispensing.medicine.dto;

import java.time.LocalDateTime;

/**
 * Read-only response view of a {@link com.pharmacy.dispensing.medicine.entity.Supplier}.
 */
public class SupplierResponse {

    private Long id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private Boolean active;
    private LocalDateTime createdAt;

    public SupplierResponse() {}

    public SupplierResponse(Long id, String name, String contactPerson, String email,
                            String phone, String address, Boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.active = active;
        this.createdAt = createdAt;
    }

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

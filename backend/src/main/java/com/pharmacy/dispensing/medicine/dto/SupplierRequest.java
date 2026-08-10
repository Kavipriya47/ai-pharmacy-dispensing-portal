package com.pharmacy.dispensing.medicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating or updating a {@link com.pharmacy.dispensing.medicine.entity.Supplier}.
 */
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 150, message = "Supplier name must not exceed 150 characters")
    private String name;

    @Size(max = 100, message = "Contact person name must not exceed 100 characters")
    private String contactPerson;

    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;

    private String address;

    // ---- Getters & Setters ----

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
}

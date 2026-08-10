package com.pharmacy.dispensing.auth.dto;

import java.util.Set;

public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Boolean active;
    private Set<String> roles;

    public UserDto() {
    }

    public UserDto(Long id, String username, String email, String fullName, Boolean active, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.active = active;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}

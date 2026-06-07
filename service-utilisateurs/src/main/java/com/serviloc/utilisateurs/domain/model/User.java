package com.serviloc.utilisateurs.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {

    private final UUID id;
    private String email;
    private String password;
    private String phone;
    private UserRole role;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static User create(String email, String password, String phone, UserRole role) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email obligatoire");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password obligatoire");
        if (phone == null || phone.isBlank())
            throw new IllegalArgumentException("Téléphone obligatoire");

        return new User(UUID.randomUUID(), email, password, phone, role,
                false, LocalDateTime.now(), LocalDateTime.now());
    }


    private User(UUID id, String email, String password, String phone,
                 UserRole role, boolean active,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public void activate() {
        if (this.active) throw new IllegalStateException("Compte déjà actif");
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void reactivate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }


    public UUID getId()                 { return id; }
    public String getEmail()            { return email; }
    public String getPassword()         { return password; }
    public String getPhone()            { return phone; }
    public UserRole getRole()           { return role; }
    public boolean isActive()           { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
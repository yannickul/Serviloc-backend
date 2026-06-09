package com.serviloc.utilisateurs.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {

    public enum Status { ACTIVE, SUSPENDED, PENDING }

    private final UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private UserRole role;
    private Status status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static User create(String firstName, String lastName,
                              String email, String password,
                              String phone, UserRole role) {
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("Prénom obligatoire");
        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("Nom obligatoire");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email obligatoire");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password obligatoire");
        if (phone == null || phone.isBlank())
            throw new IllegalArgumentException("Téléphone obligatoire");

        return new User(UUID.randomUUID(), firstName, lastName, email, password,
                phone, role, Status.PENDING, LocalDateTime.now(), LocalDateTime.now());
    }

    private User(UUID id, String firstName, String lastName, String email,
                 String password, String phone, UserRole role, Status status,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public void activate() {
        if (this.status == Status.ACTIVE)
            throw new IllegalStateException("Compte déjà actif");
        this.status = Status.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = Status.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reactivate() {
        this.status = Status.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }


    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getAvatarInitial() {
        return firstName != null && !firstName.isBlank()
                ? String.valueOf(firstName.charAt(0)).toUpperCase()
                : "?";
    }

    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }


    public UUID getId()                 { return id; }
    public String getFirstName()        { return firstName; }
    public String getLastName()         { return lastName; }
    public String getEmail()            { return email; }
    public String getPassword()         { return password; }
    public String getPhone()            { return phone; }
    public UserRole getRole()           { return role; }
    public Status getStatus()           { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
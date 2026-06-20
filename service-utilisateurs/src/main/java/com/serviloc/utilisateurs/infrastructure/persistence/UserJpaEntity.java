package com.serviloc.utilisateurs.infrastructure.persistence;

import com.serviloc.utilisateurs.domain.model.User;
import com.serviloc.utilisateurs.domain.model.UserRole;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email", unique = true)
        })
@EntityListeners(AuditingEntityListener.class)
public class UserJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private User.Status status = User.Status.PENDING;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected UserJpaEntity() {}

    public UserJpaEntity(UUID id, String firstName, String lastName,
                         String email, String password, String phone,
                         UserRole role, User.Status status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.status = status;
    }


    public UUID getId()                   { return id; }
    public String getFirstName()          { return firstName; }
    public String getLastName()           { return lastName; }
    public String getEmail()              { return email; }
    public String getPassword()           { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone()              { return phone; }
    public UserRole getRole()             { return role; }
    public User.Status getStatus()        { return status; }
    public void setStatus(User.Status s)  { this.status = s; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }
}
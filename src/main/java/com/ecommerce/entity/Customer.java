package com.ecommerce.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_email", columnList = "email", unique = true),
    @Index(name = "idx_username", columnList = "username", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")  // Changed from "customer_id" to "id"
    private Integer id;

    @NotBlank(message = "Username is required")
    @Column(length = 100, nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password is required")
    @Column(length = 255, nullable = false)
    private String password;

    @NotBlank(message = "First name is required")
    @Column(length = 100, nullable = false)
    private String first_name;

    @NotBlank(message = "Last name is required")
    @Column(length = 100, nullable = false)
    private String last_name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String zip_code;

    @Column(length = 100)
    private String country;

    @Column(columnDefinition = "VARCHAR(50) DEFAULT 'ROLE_USER'")
    private String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (role == null) {
            role = "ROLE_USER";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
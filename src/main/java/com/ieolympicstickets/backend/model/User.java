package com.ieolympicstickets.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))

public class User {
    @Id @GeneratedValue( strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false, unique = true, length = 255)
    private String email;

    @Column(name ="password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name="first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name="last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth",nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    /**
     * User id key
     * */
    @Column(name= "user_key",
        nullable = false,
        unique = true,
         updatable = false,
        length = 36)
    private String userKey;


    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name="updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist
    public void prePersist() {
        //checks if there is a unique key when insert
        if(userKey == null)
        {
            userKey= UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    public  void preUpdate() {
        this.updatedAt = Instant.now();
    }

}

package com.finassist.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false, unique = true, length = 30, name = "customer_number")
    private String customerNumber;

    @Column(nullable = false, length = 50, name = "first_name")
    private String firstName;

    @Column(nullable = false, length = 50, name = "last_name")
    private String lastName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 25, name = "phone_number")
    private String phoneNumber;

    @Column(length = 50, name = "home_city")
    @Builder.Default
    private String homeCity = "London";

    @Column(length = 3)
    @Builder.Default
    private String currency = "GBP";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}

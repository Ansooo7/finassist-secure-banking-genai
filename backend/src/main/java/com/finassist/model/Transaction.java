package com.finassist.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "GBP";

    @Column(nullable = false, length = 10)
    private String direction; // DEBIT or CREDIT

    @Column(nullable = false, length = 50)
    private String category; // Dining, Groceries, Transport, Entertainment, Utilities, Rent, Shopping, Income

    @Column(nullable = false, length = 100, name = "merchant_name")
    private String merchantName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_recurring")
    @Builder.Default
    private boolean isRecurring = false;

    @Column(name = "transaction_time", nullable = false)
    private ZonedDateTime transactionTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}

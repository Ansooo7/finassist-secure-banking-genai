package com.finassist.repository;

import com.finassist.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Customer Isolated Transaction Queries
    List<Transaction> findByCustomerIdOrderByTransactionTimeDesc(UUID customerId);

    Page<Transaction> findByCustomerIdOrderByTransactionTimeDesc(UUID customerId, Pageable pageable);

    List<Transaction> findByCustomerIdAndTransactionTimeBetweenOrderByTransactionTimeDesc(
            UUID customerId, ZonedDateTime start, ZonedDateTime end
    );

    List<Transaction> findByCustomerIdAndCategoryOrderByTransactionTimeDesc(
            UUID customerId, String category
    );

    @Query("SELECT t FROM Transaction t WHERE t.customer.id = :customerId AND t.isRecurring = true")
    List<Transaction> findRecurringByCustomerId(@Param("customerId") UUID customerId);
}

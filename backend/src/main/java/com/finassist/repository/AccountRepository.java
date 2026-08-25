package com.finassist.repository;

import com.finassist.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByCustomerId(UUID customerId);
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByIdAndCustomerId(UUID accountId, UUID customerId);
}

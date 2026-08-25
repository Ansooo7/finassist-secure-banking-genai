package com.finassist.service;

import com.finassist.dto.BankingDTOs.TransactionDTO;
import com.finassist.dto.PageResponse;
import com.finassist.model.Account;
import com.finassist.model.Customer;
import com.finassist.model.Transaction;
import com.finassist.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerIsolationTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void testCustomerTransactionQueryIsIsolatedByCustomerId() {
        UUID oliverCustomerId = UUID.fromString("c0000001-0000-0000-0000-000000000001");
        Customer oliver = Customer.builder()
                .id(oliverCustomerId)
                .customerNumber("CUST-UK-1001")
                .firstName("Oliver")
                .lastName("Twist")
                .build();

        Account account = Account.builder()
                .id(UUID.randomUUID())
                .customer(oliver)
                .build();

        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .account(account)
                .customer(oliver)
                .amount(new BigDecimal("45.00"))
                .currency("GBP")
                .direction("DEBIT")
                .category("Dining")
                .merchantName("Nando's")
                .transactionTime(ZonedDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of(tx), pageable, 1);

        when(customerService.getCustomerByUsername("oliver")).thenReturn(oliver);
        when(transactionRepository.findByCustomerIdOrderByTransactionTimeDesc(eq(oliverCustomerId), eq(pageable)))
                .thenReturn(page);

        PageResponse<TransactionDTO> response = transactionService.getCustomerTransactions("oliver", pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Nando's", response.getContent().get(0).getMerchantName());

        // Verify that the repository was called strictly with Oliver's customer ID
        verify(transactionRepository).findByCustomerIdOrderByTransactionTimeDesc(oliverCustomerId, pageable);
    }
}

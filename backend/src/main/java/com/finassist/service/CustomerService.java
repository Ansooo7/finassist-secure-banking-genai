package com.finassist.service;

import com.finassist.dto.BankingDTOs.AccountDTO;
import com.finassist.dto.BankingDTOs.CustomerProfileResponse;
import com.finassist.model.Account;
import com.finassist.model.Customer;
import com.finassist.repository.AccountRepository;
import com.finassist.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer profile not found for user: " + username));
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse getCustomerProfile(String username) {
        Customer customer = getCustomerByUsername(username);
        List<Account> accounts = accountRepository.findByCustomerId(customer.getId());

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AccountDTO> accountDTOs = accounts.stream()
                .map(acc -> AccountDTO.builder()
                        .id(acc.getId())
                        .accountNumber(acc.getAccountNumber())
                        .sortCode(acc.getSortCode())
                        .accountType(acc.getAccountType())
                        .currency(acc.getCurrency())
                        .balance(acc.getBalance())
                        .status(acc.getStatus())
                        .build())
                .collect(Collectors.toList());

        return CustomerProfileResponse.builder()
                .customerId(customer.getId())
                .customerNumber(customer.getCustomerNumber())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .fullName(customer.getFirstName() + " " + customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .homeCity(customer.getHomeCity())
                .currency(customer.getCurrency())
                .totalBalance(totalBalance)
                .accounts(accountDTOs)
                .build();
    }
}

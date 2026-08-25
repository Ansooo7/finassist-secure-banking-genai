package com.finassist.service;

import com.finassist.dto.BankingDTOs.CategorySpendDTO;
import com.finassist.dto.BankingDTOs.RecurringExpenseDTO;
import com.finassist.dto.BankingDTOs.SpendingSummaryResponse;
import com.finassist.dto.BankingDTOs.TransactionDTO;
import com.finassist.dto.PageResponse;
import com.finassist.model.Customer;
import com.finassist.model.Transaction;
import com.finassist.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CustomerService customerService;

    @Transactional(readOnly = true)
    public PageResponse<TransactionDTO> getCustomerTransactions(String username, Pageable pageable) {
        Customer customer = customerService.getCustomerByUsername(username);
        Page<Transaction> page = transactionRepository.findByCustomerIdOrderByTransactionTimeDesc(customer.getId(), pageable);

        List<TransactionDTO> dtos = page.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PageResponse.<TransactionDTO>builder()
                .content(dtos)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<Transaction> getRecentCustomerTransactions(UUID customerId) {
        return transactionRepository.findByCustomerIdOrderByTransactionTimeDesc(customerId);
    }

    @Transactional(readOnly = true)
    public SpendingSummaryResponse getSpendingSummary(String username) {
        Customer customer = customerService.getCustomerByUsername(username);
        List<Transaction> all = transactionRepository.findByCustomerIdOrderByTransactionTimeDesc(customer.getId());

        if (all.isEmpty()) {
            return SpendingSummaryResponse.builder()
                    .currentMonth("August 2026")
                    .previousMonth("July 2026")
                    .currentMonthSpend(BigDecimal.ZERO)
                    .previousMonthSpend(BigDecimal.ZERO)
                    .spendDelta(BigDecimal.ZERO)
                    .percentageChange(0.0)
                    .categoryBreakdown(Collections.emptyList())
                    .recurringExpenses(Collections.emptyList())
                    .build();
        }

        // Partition into August 2026 (current) and July 2026 (previous)
        List<Transaction> augList = all.stream()
                .filter(t -> t.getTransactionTime().getMonthValue() == 8 && t.getTransactionTime().getYear() == 2026)
                .collect(Collectors.toList());

        List<Transaction> julyList = all.stream()
                .filter(t -> t.getTransactionTime().getMonthValue() == 7 && t.getTransactionTime().getYear() == 2026)
                .collect(Collectors.toList());

        BigDecimal augSpend = augList.stream()
                .filter(t -> "DEBIT".equalsIgnoreCase(t.getDirection()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal julySpend = julyList.stream()
                .filter(t -> "DEBIT".equalsIgnoreCase(t.getDirection()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal delta = augSpend.subtract(julySpend);
        double pctChange = julySpend.compareTo(BigDecimal.ZERO) > 0
                ? delta.divide(julySpend, 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0.0;

        // Category breakdown for current month
        Map<String, BigDecimal> catTotals = new HashMap<>();
        Map<String, Integer> catCounts = new HashMap<>();

        for (Transaction t : augList) {
            if ("DEBIT".equalsIgnoreCase(t.getDirection())) {
                catTotals.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
                catCounts.merge(t.getCategory(), 1, Integer::sum);
            }
        }

        List<CategorySpendDTO> breakdown = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : catTotals.entrySet()) {
            double pct = augSpend.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(augSpend, 4, RoundingMode.HALF_UP).doubleValue() * 100
                    : 0.0;

            breakdown.add(CategorySpendDTO.builder()
                    .category(entry.getKey())
                    .amount(entry.getValue())
                    .percentage(Math.round(pct * 10.0) / 10.0)
                    .transactionCount(catCounts.get(entry.getKey()))
                    .build());
        }
        breakdown.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        String topCat = !breakdown.isEmpty() ? breakdown.get(0).getCategory() : "N/A";
        BigDecimal topCatAmount = !breakdown.isEmpty() ? breakdown.get(0).getAmount() : BigDecimal.ZERO;

        // Recurring expenses
        List<RecurringExpenseDTO> recurring = all.stream()
                .filter(Transaction::isRecurring)
                .collect(Collectors.toMap(
                        Transaction::getMerchantName,
                        t -> RecurringExpenseDTO.builder()
                                .merchantName(t.getMerchantName())
                                .category(t.getCategory())
                                .amount(t.getAmount())
                                .currency(t.getCurrency())
                                .latestDate(t.getTransactionTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                                .build(),
                        (existing, replacing) -> existing
                ))
                .values()
                .stream()
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());

        return SpendingSummaryResponse.builder()
                .currentMonth("August 2026")
                .previousMonth("July 2026")
                .currentMonthSpend(augSpend)
                .previousMonthSpend(julySpend)
                .spendDelta(delta)
                .percentageChange(Math.round(pctChange * 10.0) / 10.0)
                .topCategory(topCat)
                .topCategoryAmount(topCatAmount)
                .categoryBreakdown(breakdown)
                .recurringExpenses(recurring)
                .build();
    }

    private TransactionDTO mapToDTO(Transaction t) {
        return TransactionDTO.builder()
                .id(t.getId())
                .accountId(t.getAccount().getId())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .direction(t.getDirection())
                .category(t.getCategory())
                .merchantName(t.getMerchantName())
                .description(t.getDescription())
                .isRecurring(t.isRecurring())
                .transactionTime(t.getTransactionTime())
                .build();
    }
}

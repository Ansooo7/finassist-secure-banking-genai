package com.finassist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class BankingDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerProfileResponse {
        private UUID customerId;
        private String customerNumber;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phoneNumber;
        private String homeCity;
        private String currency;
        private BigDecimal totalBalance;
        private List<AccountDTO> accounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDTO {
        private UUID id;
        private String accountNumber;
        private String sortCode;
        private String accountType;
        private String currency;
        private BigDecimal balance;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDTO {
        private UUID id;
        private UUID accountId;
        private BigDecimal amount;
        private String currency;
        private String direction;
        private String category;
        private String merchantName;
        private String description;
        private boolean isRecurring;
        private ZonedDateTime transactionTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpendingSummaryResponse {
        private String currentMonth;
        private String previousMonth;
        private BigDecimal currentMonthSpend;
        private BigDecimal previousMonthSpend;
        private BigDecimal spendDelta;
        private Double percentageChange;
        private String topCategory;
        private BigDecimal topCategoryAmount;
        private List<CategorySpendDTO> categoryBreakdown;
        private List<RecurringExpenseDTO> recurringExpenses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySpendDTO {
        private String category;
        private BigDecimal amount;
        private Double percentage;
        private int transactionCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecurringExpenseDTO {
        private String merchantName;
        private String category;
        private BigDecimal amount;
        private String currency;
        private String latestDate;
    }

    // AI Microservice Payload DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIOrchestrationRequestPayload {
        private String message;
        @JsonProperty("customer_context")
        private CustomerContextPayload customerContext;
        @JsonProperty("recent_transactions")
        private List<TransactionItemPayload> recentTransactions;
        @JsonProperty("session_id")
        private String sessionId;
        @JsonProperty("conversation_history")
        private List<ChatHistoryItemPayload> conversationHistory;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerContextPayload {
        @JsonProperty("customer_id")
        private String customerId;
        @JsonProperty("customer_name")
        private String customerName;
        @JsonProperty("account_number")
        private String accountNumber;
        @JsonProperty("current_balance")
        private Double currentBalance;
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionItemPayload {
        private String id;
        private Double amount;
        private String currency;
        private String direction;
        private String category;
        @JsonProperty("merchant_name")
        private String merchantName;
        private String description;
        @JsonProperty("is_recurring")
        private Boolean isRecurring;
        @JsonProperty("transaction_time")
        private String transactionTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatHistoryItemPayload {
        private String sender;
        private String text;
    }
}

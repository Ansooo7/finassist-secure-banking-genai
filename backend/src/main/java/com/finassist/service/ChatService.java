package com.finassist.service;

import com.finassist.dto.BankingDTOs.*;
import com.finassist.dto.ChatDTOs.*;
import com.finassist.model.*;
import com.finassist.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final CustomerService customerService;
    private final TransactionService transactionService;
    private final AIClientService aiClientService;
    private final AuditService auditService;
    private final ConversationSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public ChatResponse processUserQuery(String username, ChatRequest request) {
        Customer customer = customerService.getCustomerByUsername(username);

        // 1. Resolve or Create Conversation Session
        ConversationSession session;
        if (request.getSessionId() != null) {
            session = sessionRepository.findByIdAndCustomerId(request.getSessionId(), customer.getId())
                    .orElseGet(() -> createNewSession(customer, request.getMessage()));
        } else {
            session = createNewSession(customer, request.getMessage());
        }

        // 2. Fetch Customer Account Context & Transactions (Isolated to this customer)
        List<Account> accounts = accountRepository.findByCustomerId(customer.getId());
        Account primaryAccount = !accounts.isEmpty() ? accounts.get(0) : null;
        double balance = primaryAccount != null ? primaryAccount.getBalance().doubleValue() : 0.0;
        String accNum = primaryAccount != null ? primaryAccount.getAccountNumber() : "00000000";

        CustomerContextPayload customerContext = CustomerContextPayload.builder()
                .customerId(customer.getId().toString())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .accountNumber(accNum)
                .currentBalance(balance)
                .currency(customer.getCurrency())
                .build();

        List<Transaction> transactions = transactionService.getRecentCustomerTransactions(customer.getId());
        List<TransactionItemPayload> txPayloads = transactions.stream()
                .map(t -> TransactionItemPayload.builder()
                        .id(t.getId().toString())
                        .amount(t.getAmount().doubleValue())
                        .currency(t.getCurrency())
                        .direction(t.getDirection())
                        .category(t.getCategory())
                        .merchantName(t.getMerchantName())
                        .description(t.getDescription())
                        .isRecurring(t.isRecurring())
                        .transactionTime(t.getTransactionTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .build())
                .collect(Collectors.toList());

        // 3. Save User Message
        ChatMessage userMsg = ChatMessage.builder()
                .session(session)
                .customer(customer)
                .sender("USER")
                .messageText(request.getMessage())
                .guardrailStatus("PENDING")
                .build();
        messageRepository.save(userMsg);

        // 4. Construct AI Orchestrator Request
        AIOrchestrationRequestPayload aiRequest = AIOrchestrationRequestPayload.builder()
                .message(request.getMessage())
                .customerContext(customerContext)
                .recentTransactions(txPayloads)
                .sessionId(session.getId().toString())
                .conversationHistory(Collections.emptyList())
                .build();

        // 5. Call AI Microservice
        AIClientService.AIOrchestrationResponsePayload aiResponse = aiClientService.orchestrateQuery(aiRequest);

        // 6. Save Assistant Response Message
        String dataPointsSummary = aiResponse.getExplainability() != null && aiResponse.getExplainability().getDataPointsUsed() != null
                ? String.join("; ", aiResponse.getExplainability().getDataPointsUsed())
                : "Standard response";

        ChatMessage assistantMsg = ChatMessage.builder()
                .session(session)
                .customer(customer)
                .sender("ASSISTANT")
                .messageText(aiResponse.getAnswer())
                .intentDetected(aiResponse.getIntent())
                .guardrailStatus(aiResponse.getGuardrailStatus())
                .dataPointsSummary(dataPointsSummary)
                .build();
        messageRepository.save(assistantMsg);

        // 7. Record Immutable AI Audit Log
        String correlationId = MDC.get("correlationId");
        String guardrailDetails = (aiResponse.getExplainability() != null && aiResponse.getExplainability().getGuardrailChecks() != null)
                ? aiResponse.getExplainability().getGuardrailChecks().toString()
                : "{}";
        String retrievedSources = (aiResponse.getExplainability() != null && aiResponse.getExplainability().getRetrievedFaqSources() != null)
                ? aiResponse.getExplainability().getRetrievedFaqSources().toString()
                : "[]";

        auditService.logAiInteraction(
                customer,
                username,
                correlationId,
                request.getMessage(),
                aiResponse.getIntent(),
                aiResponse.getGuardrailStatus(),
                guardrailDetails,
                retrievedSources,
                aiResponse.getLatencyMs(),
                "SUCCESS"
        );

        return ChatResponse.builder()
                .messageId(assistantMsg.getId())
                .sessionId(session.getId())
                .answer(aiResponse.getAnswer())
                .intent(aiResponse.getIntent())
                .guardrailStatus(aiResponse.getGuardrailStatus())
                .explainability(aiResponse.getExplainability())
                .latencyMs(aiResponse.getLatencyMs())
                .timestamp(ZonedDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ChatHistoryMessageDTO> getSessionHistory(String username, UUID sessionId) {
        Customer customer = customerService.getCustomerByUsername(username);
        ConversationSession session = sessionRepository.findByIdAndCustomerId(sessionId, customer.getId())
                .orElseThrow(() -> new RuntimeException("Session not found for customer"));

        List<ChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());

        return messages.stream()
                .map(m -> ChatHistoryMessageDTO.builder()
                        .id(m.getId())
                        .sender(m.getSender())
                        .text(m.getMessageText())
                        .intentDetected(m.getIntentDetected())
                        .guardrailStatus(m.getGuardrailStatus())
                        .dataPointsSummary(m.getDataPointsSummary())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private ConversationSession createNewSession(Customer customer, String firstMessage) {
        String title = firstMessage.length() > 40 ? firstMessage.substring(0, 37) + "..." : firstMessage;
        ConversationSession session = ConversationSession.builder()
                .customer(customer)
                .sessionTitle(title)
                .build();
        return sessionRepository.save(session);
    }
}

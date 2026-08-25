package com.finassist.service;

import com.finassist.dto.ChatDTOs.ChatRequest;
import com.finassist.dto.ChatDTOs.ChatResponse;
import com.finassist.dto.ChatDTOs.ExplainabilityDTO;
import com.finassist.model.*;
import com.finassist.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatOrchestrationTest {

    @Mock
    private CustomerService customerService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private AIClientService aiClientService;
    @Mock
    private AuditService auditService;
    @Mock
    private ConversationSessionRepository sessionRepository;
    @Mock
    private ChatMessageRepository messageRepository;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    void testChatOrchestrationWorkflow() {
        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .customerNumber("CUST-UK-1001")
                .firstName("Oliver")
                .lastName("Twist")
                .currency("GBP")
                .build();

        Account account = Account.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .accountNumber("12345678")
                .balance(new BigDecimal("15420.50"))
                .currency("GBP")
                .build();

        ConversationSession session = ConversationSession.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .sessionTitle("Spending Inquiries")
                .build();

        AIClientService.AIOrchestrationResponsePayload mockAiPayload = AIClientService.AIOrchestrationResponsePayload.builder()
                .answer("You spent £460.99 in August 2026.")
                .intent("TRANSACTION_ANALYTICS")
                .guardrailStatus("PASSED")
                .explainability(ExplainabilityDTO.builder()
                        .dataPointsUsed(List.of("Evaluated 6 transactions"))
                        .retrievedFaqSources(Collections.emptyList())
                        .guardrailChecks(Map.of("PROMPT_INJECTION_DEFENSE", "PASSED"))
                        .intentDetected("TRANSACTION_ANALYTICS")
                        .intentConfidence(0.95)
                        .isGrounded(true)
                        .build())
                .latencyMs(25)
                .build();

        when(customerService.getCustomerByUsername("oliver")).thenReturn(customer);
        when(accountRepository.findByCustomerId(customer.getId())).thenReturn(List.of(account));
        when(transactionService.getRecentCustomerTransactions(customer.getId())).thenReturn(Collections.emptyList());
        when(sessionRepository.save(any(ConversationSession.class))).thenReturn(session);
        when(aiClientService.orchestrateQuery(any())).thenReturn(mockAiPayload);
        when(messageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage msg = invocation.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });

        ChatRequest request = ChatRequest.builder()
                .message("How much did I spend this month?")
                .build();

        ChatResponse response = chatService.processUserQuery("oliver", request);

        assertNotNull(response);
        assertEquals("You spent £460.99 in August 2026.", response.getAnswer());
        assertEquals("TRANSACTION_ANALYTICS", response.getIntent());
        assertEquals("PASSED", response.getGuardrailStatus());

        verify(messageRepository, times(2)).save(any(ChatMessage.class));
        verify(auditService).logAiInteraction(
                eq(customer),
                eq("oliver"),
                any(),
                eq("How much did I spend this month?"),
                eq("TRANSACTION_ANALYTICS"),
                eq("PASSED"),
                any(),
                any(),
                eq(25),
                eq("SUCCESS")
        );
    }
}

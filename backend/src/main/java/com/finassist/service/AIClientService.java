package com.finassist.service;

import com.finassist.dto.BankingDTOs.AIOrchestrationRequestPayload;
import com.finassist.dto.ChatDTOs.ChatResponse;
import com.finassist.dto.ChatDTOs.ExplainabilityDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
public class AIClientService {

    private final RestClient restClient;

    public AIClientService(
            RestClient.Builder restClientBuilder,
            @Value("${ai.service.url:http://localhost:8000}") String aiServiceUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(aiServiceUrl)
                .build();
    }

    public AIOrchestrationResponsePayload orchestrateQuery(AIOrchestrationRequestPayload payload) {
        log.info("Dispatching query to AI Microservice: [Customer: {}, Message: '{}']",
                payload.getCustomerContext().getCustomerId(), payload.getMessage());

        try {
            return restClient.post()
                    .uri("/api/v1/ai/orchestrate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(AIOrchestrationResponsePayload.class);
        } catch (Exception e) {
            log.error("AI Microservice call failed: {}. Generating fallback response.", e.getMessage());
            
            return AIOrchestrationResponsePayload.builder()
                    .answer("I am temporarily having trouble contacting the AI analysis service. Please try again shortly.")
                    .intent("SERVICE_DEGRADED")
                    .guardrailStatus("FALLBACK")
                    .explainability(ExplainabilityDTO.builder()
                            .dataPointsUsed(Collections.singletonList("System fallback due to network timeout"))
                            .retrievedFaqSources(Collections.emptyList())
                            .intentDetected("SERVICE_DEGRADED")
                            .intentConfidence(0.0)
                            .isGrounded(false)
                            .build())
                    .latencyMs(0)
                    .build();
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AIOrchestrationResponsePayload {
        private String answer;
        private String intent;
        @com.fasterxml.jackson.annotation.JsonProperty("guardrail_status")
        private String guardrailStatus;
        private ExplainabilityDTO explainability;
        @com.fasterxml.jackson.annotation.JsonProperty("latency_ms")
        private Integer latencyMs;
    }
}

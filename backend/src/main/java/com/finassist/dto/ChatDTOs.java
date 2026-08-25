package com.finassist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        private UUID sessionId;
        
        @NotBlank(message = "Message cannot be blank")
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FAQDocumentSourceDTO {
        @JsonProperty("doc_id")
        private String docId;
        private String category;
        private String title;
        @JsonProperty("content_snippet")
        private String contentSnippet;
        @JsonProperty("similarity_score")
        private Double similarityScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoMVarianceItemDTO {
        private String category;
        @JsonProperty("previous_amount")
        private Double previousAmount;
        @JsonProperty("current_amount")
        private Double currentAmount;
        @JsonProperty("delta_amount")
        private Double deltaAmount;
        @JsonProperty("percentage_change")
        private Double percentageChange;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExplainabilityDTO {
        @JsonProperty("data_points_used")
        private List<String> dataPointsUsed;
        
        @JsonProperty("retrieved_faq_sources")
        private List<FAQDocumentSourceDTO> retrievedFaqSources;
        
        @JsonProperty("intent_detected")
        private String intentDetected;
        
        @JsonProperty("intent_confidence")
        private Double intentConfidence;
        
        @JsonProperty("guardrail_checks")
        private Map<String, String> guardrailChecks;
        
        @JsonProperty("variance_breakdown")
        private List<MoMVarianceItemDTO> varianceBreakdown;
        
        @JsonProperty("is_grounded")
        private Boolean isGrounded;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponse {
        private UUID messageId;
        private UUID sessionId;
        private String answer;
        private String intent;
        private String guardrailStatus;
        private ExplainabilityDTO explainability;
        private Integer latencyMs;
        private ZonedDateTime timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatHistoryMessageDTO {
        private UUID id;
        private String sender;
        private String text;
        private String intentDetected;
        private String guardrailStatus;
        private String dataPointsSummary;
        private ZonedDateTime createdAt;
    }
}

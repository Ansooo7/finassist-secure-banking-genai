package com.finassist.controller;

import com.finassist.dto.ApiResponse;
import com.finassist.dto.ChatDTOs.ChatHistoryMessageDTO;
import com.finassist.dto.ChatDTOs.ChatRequest;
import com.finassist.dto.ChatDTOs.ChatResponse;
import com.finassist.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "GenAI Assistant", description = "Conversational Banking AI Assistant with Explainability")
@SecurityRequirement(name = "Bearer Authentication")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/message")
    @Operation(summary = "Submit natural language query to the GenAI Banking Assistant")
    public ResponseEntity<ApiResponse<ChatResponse>> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChatRequest request
    ) {
        ChatResponse response = chatService.processUserQuery(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    @Operation(summary = "Retrieve message history for a conversation session")
    public ResponseEntity<ApiResponse<List<ChatHistoryMessageDTO>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam UUID sessionId
    ) {
        List<ChatHistoryMessageDTO> history = chatService.getSessionHistory(userDetails.getUsername(), sessionId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}

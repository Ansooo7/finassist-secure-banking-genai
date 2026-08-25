package com.finassist.controller;

import com.finassist.dto.ApiResponse;
import com.finassist.dto.BankingDTOs.SpendingSummaryResponse;
import com.finassist.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Transaction Analytics", description = "Customer Spending Analytics and MoM Breakdown")
@SecurityRequirement(name = "Bearer Authentication")
public class AnalyticsController {

    private final TransactionService transactionService;

    @GetMapping("/spending-summary")
    @Operation(summary = "Get MoM spending variance, category totals, and recurring expenses")
    public ResponseEntity<ApiResponse<SpendingSummaryResponse>> getSpendingSummary(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SpendingSummaryResponse summary = transactionService.getSpendingSummary(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}

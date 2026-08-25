package com.finassist.controller;

import com.finassist.dto.ApiResponse;
import com.finassist.dto.BankingDTOs.TransactionDTO;
import com.finassist.dto.PageResponse;
import com.finassist.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Customer Transactions with Data Isolation")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/my-transactions")
    @Operation(summary = "Get paginated transaction list for authenticated customer")
    public ResponseEntity<ApiResponse<PageResponse<TransactionDTO>>> getMyTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<TransactionDTO> response = transactionService.getCustomerTransactions(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

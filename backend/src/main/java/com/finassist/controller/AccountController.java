package com.finassist.controller;

import com.finassist.dto.ApiResponse;
import com.finassist.dto.BankingDTOs.AccountDTO;
import com.finassist.dto.BankingDTOs.CustomerProfileResponse;
import com.finassist.service.CustomerService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Bank Accounts", description = "Customer Account Management Endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountController {

    private final CustomerService customerService;

    @GetMapping("/my-accounts")
    @Operation(summary = "List all accounts for the authenticated customer")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getMyAccounts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CustomerProfileResponse profile = customerService.getCustomerProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(profile.getAccounts()));
    }
}

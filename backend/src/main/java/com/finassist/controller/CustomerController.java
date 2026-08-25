package com.finassist.controller;

import com.finassist.dto.ApiResponse;
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

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Profile", description = "Authenticated Customer Banking Profile Endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated customer profile and balances")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CustomerProfileResponse profile = customerService.getCustomerProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}

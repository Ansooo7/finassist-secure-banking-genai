package com.finassist.controller;

import com.finassist.dto.ApiResponse;
import com.finassist.dto.AuthDTOs.AuthRequest;
import com.finassist.dto.AuthDTOs.AuthResponse;
import com.finassist.dto.AuthDTOs.RegisterRequest;
import com.finassist.model.Account;
import com.finassist.model.Customer;
import com.finassist.model.Role;
import com.finassist.model.User;
import com.finassist.repository.AccountRepository;
import com.finassist.repository.CustomerRepository;
import com.finassist.repository.UserRepository;
import com.finassist.security.CustomUserDetails;
import com.finassist.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User Login and Registration Endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        Optional<Customer> customerOpt = customerRepository.findByUserId(userDetails.getId());
        UUID customerId = customerOpt.map(Customer::getId).orElse(null);
        String customerNumber = customerOpt.map(Customer::getCustomerNumber).orElse(null);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .fullName(userDetails.getFullName())
                .role(userDetails.getAuthorities().iterator().next().getAuthority())
                .customerId(customerId)
                .customerNumber(customerNumber)
                .expiresInMs(86400000)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new customer account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Username is already in use"));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email is already in use"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(Role.ROLE_CUSTOMER)
                .enabled(true)
                .build();
        userRepository.save(user);

        String[] parts = request.getFullName().split(" ", 2);
        String first = parts[0];
        String last = parts.length > 1 ? parts[1] : "Customer";

        Customer customer = Customer.builder()
                .user(user)
                .customerNumber("CUST-UK-" + (1000 + (int)(Math.random() * 9000)))
                .firstName(first)
                .lastName(last)
                .email(request.getEmail())
                .phoneNumber("+4479" + (10000000 + (int)(Math.random() * 89999999)))
                .homeCity("London")
                .currency("GBP")
                .build();
        customerRepository.save(customer);

        Account account = Account.builder()
                .customer(customer)
                .accountNumber(String.valueOf(10000000 + (int)(Math.random() * 89999999)))
                .sortCode("204514")
                .accountType("CURRENT")
                .currency("GBP")
                .balance(new BigDecimal("2500.00"))
                .status("ACTIVE")
                .build();
        accountRepository.save(account);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .customerId(customer.getId())
                .customerNumber(customer.getCustomerNumber())
                .expiresInMs(86400000)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }
}

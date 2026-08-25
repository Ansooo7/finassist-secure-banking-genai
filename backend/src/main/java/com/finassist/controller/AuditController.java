package com.finassist.controller;

import com.finassist.dto.ApiResponse;
import com.finassist.dto.PageResponse;
import com.finassist.model.AiAuditLog;
import com.finassist.repository.AiAuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin & Compliance", description = "AI Interaction Audit Logs and Compliance Telemetry")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditController {

    private final AiAuditLogRepository auditLogRepository;

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get paginated AI interaction audit logs (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<AiAuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AiAuditLog> auditPage = auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(auditPage)));
    }
}

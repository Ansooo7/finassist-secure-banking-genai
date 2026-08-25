package com.finassist.repository;

import com.finassist.model.AiAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiAuditLogRepository extends JpaRepository<AiAuditLog, UUID> {
    List<AiAuditLog> findByCustomerIdOrderByTimestampDesc(UUID customerId);
    Page<AiAuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}

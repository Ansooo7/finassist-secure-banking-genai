package com.finassist.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversation_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "session_title", length = 150)
    @Builder.Default
    private String sessionTitle = "Banking Inquiries";

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private ZonedDateTime startedAt;

    @UpdateTimestamp
    @Column(name = "last_activity_at")
    private ZonedDateTime lastActivityAt;
}

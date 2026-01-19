package com.example.whisperQ.domain.session.entity;

import com.example.whisperQ.domain.auth.entity.User;
import com.example.whisperQ.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 강의/세미나 세션 정보
 */
@Entity
@Table(name = "sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Session extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_code", length = 6, unique = true, nullable = false)
    private String sessionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facilitator_id", nullable = false)
    private User facilitator;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    public void endSession() {
        this.status = SessionStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }
}

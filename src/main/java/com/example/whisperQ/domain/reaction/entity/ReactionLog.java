package com.example.whisperQ.domain.reaction.entity;

import com.example.whisperQ.domain.session.entity.Session;
import com.example.whisperQ.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 영구 저장용 반응 로그 (분석용 원장 데이터)
 */
@Entity
@Table(name = "reaction_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReactionLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;
}

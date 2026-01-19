package com.example.whisperQ.domain.reaction.repository;

import com.example.whisperQ.domain.reaction.entity.ReactionLog;
import com.example.whisperQ.domain.reaction.entity.ReactionType;
import com.example.whisperQ.domain.session.entity.Session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 리액션 로그 Repository
 */
@Repository
public interface ReactionLogRepository extends JpaRepository<ReactionLog, Long> {

    /**
     * 세션별 리액션 로그 조회
     */
    List<ReactionLog> findBySessionOrderByCreatedAtAsc(Session session);

    /**
     * 세션 ID로 리액션 로그 조회
     */
    List<ReactionLog> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    /**
     * 세션별 특정 타입의 리액션 개수 조회
     */
    long countBySessionAndType(Session session, ReactionType type);

    /**
     * 세션별 전체 리액션 개수 조회
     */
    long countBySession(Session session);

    /**
     * 세션 ID로 전체 리액션 개수 조회
     */
    long countBySessionId(Long sessionId);

    /**
     * 특정 시간 범위 내의 리액션 로그 조회 (분석용)
     */
    @Query("SELECT r FROM ReactionLog r WHERE r.session.id = :sessionId " +
           "AND r.createdAt BETWEEN :startTime AND :endTime ORDER BY r.createdAt ASC")
    List<ReactionLog> findBySessionIdAndTimeRange(
            @Param("sessionId") Long sessionId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 세션별 리액션 타입별 개수 그룹핑 조회
     */
    @Query("SELECT r.type, COUNT(r) FROM ReactionLog r WHERE r.session.id = :sessionId GROUP BY r.type")
    List<Object[]> countBySessionIdGroupByType(@Param("sessionId") Long sessionId);

    /**
     * 특정 세션의 로그 삭제
     */
    void deleteBySessionId(Long sessionId);
}

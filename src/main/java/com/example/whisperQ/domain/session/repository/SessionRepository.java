package com.example.whisperQ.domain.session.repository;

import com.example.whisperQ.domain.auth.entity.User;
import com.example.whisperQ.domain.session.entity.Session;
import com.example.whisperQ.domain.session.entity.SessionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 세션 Repository
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    /**
     * 세션 코드로 세션 조회 (참여자 입장용)
     */
    Optional<Session> findBySessionCode(String sessionCode);

    /**
     * 세션 코드 존재 여부 확인
     */
    boolean existsBySessionCode(String sessionCode);

    /**
     * 진행자별 세션 목록 조회
     */
    List<Session> findByFacilitatorOrderByCreatedAtDesc(User facilitator);

    /**
     * 진행자별 특정 상태의 세션 목록 조회
     */
    List<Session> findByFacilitatorAndStatusOrderByCreatedAtDesc(User facilitator, SessionStatus status);

    /**
     * 진행자 ID로 세션 목록 조회
     */
    @Query("SELECT s FROM Session s WHERE s.facilitator.id = :facilitatorId ORDER BY s.createdAt DESC")
    List<Session> findByFacilitatorId(@Param("facilitatorId") Long facilitatorId);

    /**
     * 활성 상태인 세션만 조회
     */
    List<Session> findByStatus(SessionStatus status);
}

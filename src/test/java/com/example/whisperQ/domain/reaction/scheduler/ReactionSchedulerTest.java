package com.example.whisperQ.domain.reaction.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.whisperQ.domain.reaction.dto.ReactionUpdateEvent;

@SpringBootTest
class ReactionSchedulerTest {

    @Autowired
    private ReactionScheduler reactionScheduler;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @AfterEach
    void tearDown() {
        redisTemplate.delete("active_sessions");
        // 필요한 경우 다른 키도 삭제
    }

    @Test
    @DisplayName("활성 세션이 있을 경우 스케줄러가 메시지를 발행(Broadcast)해야 한다")
    void broadcastUpdates_shouldSendMessages_whenSessionIsActive() {
        // Given
        String sessionId = "test_scheduler_session";
        // 1. 활성 세션 등록
        redisTemplate.opsForSet().add("active_sessions", sessionId);
        redisTemplate.expire("active_sessions", Duration.ofMinutes(1));

        // When
        // 스케줄러 메서드 직접 호출 (주기적 실행을 기다리는 대신 로직 검증)
        reactionScheduler.broadcastUpdates();

        // Then
        // verify: messagingTemplate.convertAndSend가 호출되었는지 확인
        // 경로: /topic/session/{sessionId}/reactions
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/session/" + sessionId + "/reactions"), 
            any(ReactionUpdateEvent.class)
        );
    }

    @Test
    @DisplayName("활성 세션이 없으면 메시지를 보내지 않아야 한다")
    void broadcastUpdates_shouldDoNothing_whenNoActiveSessions() {
        // Given
        // Redis "active_sessions" is empty

        // When
        reactionScheduler.broadcastUpdates();

        // Then
        verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    }
}

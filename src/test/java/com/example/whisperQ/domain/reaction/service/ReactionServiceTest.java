package com.example.whisperQ.domain.reaction.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.example.whisperQ.domain.reaction.dto.ReactionMessage;
import com.example.whisperQ.domain.reaction.dto.ReactionUpdateEvent;
import com.example.whisperQ.domain.reaction.entity.ReactionType;

@SpringBootTest
class ReactionServiceTest {

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    @AfterEach
    void tearDown() {
        // 테스트 후 데이터 정리 (선택 사항: 전체 flush 대신 사용하는 키만 삭제 권장)
        // redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        
        // 여기서는 특정 패턴 키만 삭제 예시
        Set<String> keys = redisTemplate.keys("session:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        redisTemplate.delete("active_sessions");
    }

    @Test
    @DisplayName("리액션 저장 시 Redis ZSet에 데이터가 저장되고 Active Session에 추가되어야 한다")
    void saveReaction_shouldAddDataToRedis() {
        // Given
        String sessionId = "test_session_1";
        String type = ReactionType.CONFUSED.name(); // "CONFUSED"
        ReactionMessage message = new ReactionMessage(sessionId, type);

        // When
        reactionService.saveReaction(message);

        // Then
        // 1. ZSet 확인 (Window)
        String windowKey = "session:" + sessionId + ":reaction:" + type + ":window";
        Long zCount = redisTemplate.opsForZSet().zCard(windowKey);
        assertThat(zCount).isEqualTo(1);

        // 2. 누적 Count 확인
        String countKey = "session:" + sessionId + ":reaction:" + type + ":count";
        String totalCount = (String) redisTemplate.opsForValue().get(countKey);
        assertThat(totalCount).isEqualTo("1");

        // 3. Active Session 확인
        Boolean isMember = redisTemplate.opsForSet().isMember("active_sessions", sessionId);
        assertThat(isMember).isTrue();
    }
    @Test
    @DisplayName("Intensity 계산 시 30초 이내의 데이터만 집계되어야 한다")
    void calculateIntensity_shouldCountOnlyRecentReactions() {
        // Given
        String sessionId = "test_session_2";
        String type = ReactionType.CONFUSED.name(); 
        String windowKey = "session:" + sessionId + ":reaction:" + type + ":window";

        long now = System.currentTimeMillis();
        long oldTime = now - 40000; // 40초 전 (윈도우 밖)
        long recentTime1 = now - 10000; // 10초 전 (윈도우 안)
        long recentTime2 = now - 5000;  // 5초 전 (윈도우 안)

        // 직접 Redis에 데이터 주입
        redisTemplate.opsForZSet().add(windowKey, "old_member", oldTime);
        redisTemplate.opsForZSet().add(windowKey, "recent_member_1", recentTime1);
        redisTemplate.opsForZSet().add(windowKey, "recent_member_2", recentTime2);

        // When
        ReactionUpdateEvent result = reactionService.calculateIntensity(sessionId);

        // Then
        Map<String, Long> counts = result.reactionCounts();
        
        // 30초 윈도우 밖의 데이터(old_member)는 제외되고 2개만 카운트되어야 함
        assertThat(counts.get(type)).isEqualTo(2L);
        
        // Redis에서 실제로 삭제되었는지 확인
        Long zCard = redisTemplate.opsForZSet().zCard(windowKey);
        assertThat(zCard).isEqualTo(2L);
    }
}

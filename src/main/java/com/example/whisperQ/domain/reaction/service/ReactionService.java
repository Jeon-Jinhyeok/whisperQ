package com.example.whisperQ.domain.reaction.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.whisperQ.domain.reaction.dto.ReactionMessage;
import com.example.whisperQ.domain.reaction.dto.ReactionUpdateEvent;
import com.example.whisperQ.domain.reaction.entity.ReactionType;

import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class ReactionService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Window size (30 seconds)
    private static final long TIME_WINDOW_MS = 30000;

    /**
     * Save a reaction to Redis using ZSET.
     * key: reaction:session:{sessionId}:{type}
     * score: timestamp
     * member: uniqueId (timestamp + random)
     */
    @Transactional
    public void saveReaction(ReactionMessage message) {
        String sessionId = message.sessionId();
        String type = message.type();
        long timestamp = System.currentTimeMillis();

        // Redis Key 설계
        // 1. 단순 누적 카운트: session:{id}:reaction:{type}:count
        String countKey = "session:" + sessionId + ":reaction:" + type + ":count";
        redisTemplate.opsForValue().increment(countKey);

        // 2. 최근 30초 윈도우 (ZSet): session:{id}:reaction:{type}:window
        String windowKey = "session:" + sessionId + ":reaction:" + type + ":window";
        
        // Member UniqueID (UUID)
        String member = UUID.randomUUID().toString();

        // ZSet에 저장 (Score: Timestamp)
        redisTemplate.opsForZSet().add(windowKey, member, timestamp);
        
        // 자동 만료 설정 (윈도우 크기보다 조금 넉넉하게)
        redisTemplate.expire(windowKey, Duration.ofMillis(TIME_WINDOW_MS + 10000));
        redisTemplate.expire(countKey, Duration.ofHours(2)); // 누적 카운트는 오래 유지
        
        // 활성 세션 추적
        redisTemplate.opsForSet().add("active_sessions", sessionId);
        redisTemplate.expire("active_sessions", Duration.ofMinutes(5));
    }

    /**
     * 최근 30초간의 반응 개수를 계산함.
     */
    @Transactional
    public ReactionUpdateEvent calculateIntensity(String sessionId) {
        long now = System.currentTimeMillis();
        long windowStart = now - TIME_WINDOW_MS;

        Map<String, Long> counts = new HashMap<>();
        
        // Iterate over all reaction types
        for (ReactionType type : ReactionType.values()) {
            String typeName = type.name();
            String windowKey = "session:" + sessionId + ":reaction:" + typeName + ":window";

            // 1. 30초 지난 데이터 삭제 (ZREMRANGEBYSCORE)
            redisTemplate.opsForZSet().removeRangeByScore(windowKey, 0, windowStart - 1);

            // 2. 현재 윈도우 내 개수 조회 (ZCARD)
            // 이미 삭제했으므로 전체 개수 = 윈도우 내 개수
            Long count = redisTemplate.opsForZSet().zCard(windowKey);
            
            if (count != null && count > 0) {
                counts.put(typeName, count);
            }
        }

        return new ReactionUpdateEvent(sessionId, counts);
    }
}

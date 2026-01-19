package com.example.whisperQ.domain.reaction.scheduler;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.whisperQ.domain.reaction.dto.ReactionUpdateEvent;
import com.example.whisperQ.domain.reaction.service.ReactionService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReactionScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ReactionService reactionService;

    @Scheduled(fixedRate = 500)
    public void broadcastUpdates() {
        // Get all active sessions
        Set<Object> activeSessions = redisTemplate.opsForSet().members("active_sessions");
        
        if (activeSessions == null || activeSessions.isEmpty()) {
            return;
        }

        for (Object sessionIdObj : activeSessions) {
            String sessionId = (String) sessionIdObj;
            
            // Calculate intensity
            ReactionUpdateEvent event = reactionService.calculateIntensity(sessionId);

            // Broadcast to topic
            messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId + "/reactions", 
                event
            );
        }
    }
}

package com.example.whisperQ.domain.reaction.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.whisperQ.domain.reaction.dto.ReactionMessage;
import com.example.whisperQ.domain.reaction.service.ReactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService; // Redis 로직 담당


    /**
     * POST /api/reactions
     * HTTP 리액션 전송 API. WebSocket 연결 백업용도.
     */
    @PostMapping("/api/reactions")
    public void sendReaction(@RequestBody ReactionMessage message, java.security.Principal principal) {
        log.info("HTTP Reaction received: session={}, type={}, user={}", message.sessionId(), message.type(), (principal != null ? principal.getName() : "anonymous"));
        reactionService.saveReaction(message, (principal != null ? principal.getName() : null));
    }
    
    /**
     * WebSocket으로 리액션 전송
     * 경로: /app/reactions
     */
    @MessageMapping("/reactions")
    public void receiveReaction(@Payload ReactionMessage message, java.security.Principal principal) {
        log.info("WebSocket Reaction received: session={}, type={}, user={}", message.sessionId(), message.type(), (principal != null ? principal.getName() : "anonymous"));
        reactionService.saveReaction(message, (principal != null ? principal.getName() : null));
    }
}

package com.example.whisperQ.domain.session.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.whisperQ.domain.session.dto.SessionCreateRequest;
import com.example.whisperQ.domain.session.entity.Session;
import com.example.whisperQ.domain.session.service.SessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionController {
    
    private final SessionService sessionService;

    /**
     * GET /api/sessions/:code
     * 세션 코드로 세션 조회
     */
    @GetMapping("/{code}")
    public ResponseEntity<Session> getSessionByCode(@PathVariable String code) {
        Session session = sessionService.getSessionByCode(code);
        return ResponseEntity.ok(session);
    }

    // /**
    //  * POST /api/sessions
    //  * 새 세션 생성
    //  */
    @PostMapping
    public ResponseEntity<Session> createSession(@RequestBody @Valid SessionCreateRequest request) {
        Session session = sessionService.createSession(request.title(), request.facilitatorId());
        return ResponseEntity.ok(session);
    }
}

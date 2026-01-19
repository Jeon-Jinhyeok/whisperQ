package com.example.whisperQ.domain.session.service;

import org.springframework.stereotype.Service;

import com.example.whisperQ.domain.auth.entity.User;
import com.example.whisperQ.domain.auth.repository.UserRepository;
import com.example.whisperQ.domain.session.entity.Session;
import com.example.whisperQ.domain.session.entity.SessionStatus;
import com.example.whisperQ.domain.session.repository.SessionRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class SessionService {
    
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    /**
     * 세션 생성
     * @param title : 제목
     * @param facilitatorId : 진행자 ID
     * @return 생성된 세션
     */
    @Transactional
    public Session createSession(String title, Long facilitatorId){

        User facilitator = userRepository.findById(facilitatorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID입니다: " + facilitatorId));

        String sessionCode = generateUniqueSessionCode();
        Session session = Session.builder()
                .title(title)
                .facilitator(facilitator)
                .sessionCode(sessionCode)
                .status(SessionStatus.ACTIVE)
                .build();

        return sessionRepository.save(session);
    }

    /**
     * 세션 코드로 세션 조회
     * @param code : 세션 코드
     * @return 세션
     */
    public Session getSessionByCode(String code) {
        return sessionRepository.findBySessionCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션 코드입니다: " + code));
    }

    // 랜덤 코드 생성 로직
    private String generateUniqueSessionCode() {
        String code;
        do{
            code = generateRandomCode();
        } while (sessionRepository.existsBySessionCode(code)); // 중복이면 다시 생성

        return code;
    }

    private String generateRandomCode(){
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < CODE_LENGTH; i++){
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }
}

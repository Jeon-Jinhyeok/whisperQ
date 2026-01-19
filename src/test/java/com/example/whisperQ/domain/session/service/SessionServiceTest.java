package com.example.whisperQ.domain.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.whisperQ.domain.auth.entity.User;
import com.example.whisperQ.domain.auth.repository.UserRepository;
import com.example.whisperQ.domain.session.entity.Session;
import com.example.whisperQ.domain.session.entity.SessionStatus;
import com.example.whisperQ.domain.session.repository.SessionRepository;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @InjectMocks
    private SessionService sessionService;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("세션 생성 성공: 유효한 요청이 오면 세션을 생성하고 저장한다")
    void createSession_success() {
        // Given
        Long facilitatorId = 1L;
        String title = "Test Session";
        User mockUser = User.builder().id(facilitatorId).email("test@example.com").build();

        when(userRepository.findById(facilitatorId)).thenReturn(Optional.of(mockUser));
        when(sessionRepository.existsBySessionCode(anyString())).thenReturn(false); // 충돌 없음
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Session createdSession = sessionService.createSession(title, facilitatorId);

        // Then
        assertThat(createdSession).isNotNull();
        assertThat(createdSession.getTitle()).isEqualTo(title);
        assertThat(createdSession.getFacilitator()).isEqualTo(mockUser);
        assertThat(createdSession.getSessionCode()).isNotNull().hasSize(6);
        assertThat(createdSession.getStatus()).isEqualTo(SessionStatus.ACTIVE);

        verify(userRepository).findById(facilitatorId);
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    @DisplayName("세션 생성 실패: 진행자(User)를 찾을 수 없으면 예외가 발생한다")
    void createSession_userNotFound() {
        // Given
        Long facilitatorId = 999L;
        when(userRepository.findById(facilitatorId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sessionService.createSession("Title", facilitatorId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("존재하지 않는 사용자 ID입니다");
        
        // save가 호출되지 않아야 함
        verify(sessionRepository, times(0)).save(any(Session.class));
    }

    @Test
    @DisplayName("코드 충돌 재시도: 생성된 코드가 중복되면 다시 생성한다")
    void createSession_codeCollisionRetry() {
        // Given
        Long facilitatorId = 1L;
        User mockUser = User.builder().id(facilitatorId).email("test@example.com").build();
        when(userRepository.findById(facilitatorId)).thenReturn(Optional.of(mockUser));

        // existsBySessionCode가 첫 번째 호출(아무 문자열)에는 true(중복), 두 번째에는 false(미중복) 반환
        when(sessionRepository.existsBySessionCode(anyString()))
            .thenReturn(true)
            .thenReturn(false);

        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Session session = sessionService.createSession("Retry Test", facilitatorId);

        // Then
        verify(sessionRepository, times(2)).existsBySessionCode(anyString());
        assertThat(session.getSessionCode()).isNotNull();
    }

    @Test
    @DisplayName("세션 조회 성공: 존재하는 세션 코드로 조회하면 세션을 반환한다")
    void getSessionByCode_success() {
        // Given
        String code = "ABCDEF";
        Session mockSession = Session.builder().sessionCode(code).build();
        when(sessionRepository.findBySessionCode(code)).thenReturn(Optional.of(mockSession));

        // When
        Session foundSession = sessionService.getSessionByCode(code);

        // Then
        assertThat(foundSession).isEqualTo(mockSession);
        assertThat(foundSession.getSessionCode()).isEqualTo(code);
    }

    @Test
    @DisplayName("세션 조회 실패: 존재하지 않는 코드면 예외가 발생한다")
    void getSessionByCode_notFound() {
        // Given
        String code = "INVALID";
        when(sessionRepository.findBySessionCode(code)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sessionService.getSessionByCode(code))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("존재하지 않는 세션 코드입니다");
    }
}

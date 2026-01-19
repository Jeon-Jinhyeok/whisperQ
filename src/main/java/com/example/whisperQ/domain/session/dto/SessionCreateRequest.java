package com.example.whisperQ.domain.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SessionCreateRequest(
    
    @NotBlank(message = "세션 제목은 필수입니다.")
    @Size(max=50, message="세션 제목은 50자 이내여야 합니다.")
    String title,

    @NotNull(message = "진행자 ID는 필수입니다.")
    Long facilitatorId
) {}

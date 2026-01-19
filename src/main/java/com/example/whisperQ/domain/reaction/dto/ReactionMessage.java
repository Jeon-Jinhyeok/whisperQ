package com.example.whisperQ.domain.reaction.dto;

public record ReactionMessage(
    String sessionId,
    String type // "confused" or "more"
) {}

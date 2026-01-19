package com.example.whisperQ.domain.reaction.dto;

import java.util.Map;

public record ReactionUpdateEvent(
    String sessionId,
    Map<String, Long> reactionCounts
) {}

package com.jameselner.convo.dto;

public record UserStatusDTO(
    Long userId,
    String username,
    String status
) {}

package com.jameselner.convo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String senderUsername;
    private LocalDateTime timestamp;
    private String content;
    private MessageType type;
    private boolean edited;
    private int readByCount;
    private OracleMetadata oracleMetadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OracleMetadata {
        private int messagesAnalyzed;
        private int uniqueTokens;
        private int chainOrder;
    }

    public enum MessageType {
        CHAT, JOIN, LEAVE, TYPING, STOP_TYPING, ORACLE, SYSTEM
    }
}
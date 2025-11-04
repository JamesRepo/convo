package com.jameselner.convo.dto;

import com.jameselner.convo.model.ChatRoom;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ChatRoomDTO {
    private Long id;
    private String name;
    private String description;
    private String type;
    private UserDTO createdBy;
    private LocalDateTime createdAt;
    private int memberCount;
    private ChatMessageDTO lastMessage;

    public ChatRoomDTO(ChatRoom room) {
        this.id = room.getId();
        this.name = room.getName();
        this.description = room.getDescription();
        this.type = room.getRoomType().toString();
        this.createdBy = new UserDTO(room.getCreatedBy());
        this.createdAt = room.getCreatedAt();
    }
}

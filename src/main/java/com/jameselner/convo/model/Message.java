package com.jameselner.convo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "message")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.TEXT;

    @ElementCollection
    @CollectionTable(
            name = "message_reader",
            joinColumns = @JoinColumn(name = "message_id")
    )
    @Column(name = "user_id")
    private Set<Long> readByUserIds = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    private boolean edited = false;

    private LocalDateTime editedAt;

    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        FILE,
        SYSTEM,
        TYPING_INDICATOR
    }

}

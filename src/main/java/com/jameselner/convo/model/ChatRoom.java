package com.jameselner.convo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_room")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private RoomType roomType = RoomType.PUBLIC;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();

    public enum RoomType {
        PUBLIC,
        PRIVATE,
        PROTECTED
    }
}

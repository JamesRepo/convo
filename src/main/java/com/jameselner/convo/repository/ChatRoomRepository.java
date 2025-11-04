package com.jameselner.convo.repository;

import com.jameselner.convo.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByRoomType(ChatRoom.RoomType type);

    @Query("SELECT cr FROM ChatRoom cr JOIN cr.messages m " +
            "WHERE cr.id = :roomId ORDER BY m.timestamp DESC")
    ChatRoom findByIdWithLastMessage(Long roomId);
}

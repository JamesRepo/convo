package com.jameselner.convo.repository;

import com.jameselner.convo.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatRoomIdOrderByTimestampAsc(Long chatRoomId);

    Page<Message> findByChatRoomIdOrderByTimestampDesc(Long chatRoomId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = ?1 " +
            "AND LOWER(m.content) LIKE LOWER(CONCAT('%', ?2, '%')) " +
            "ORDER BY m.timestamp DESC")
    List<Message> searchInChatRoom(Long chatRoomId, String keyword);

    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = ?1 " +
            "ORDER BY m.timestamp DESC")
    List<Message> findLatestMessages(Long chatRoomId);

    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = ?1 " +
            "ORDER BY m.timestamp DESC")
    List<Message> findTopByChatRoomId(Long chatRoomId, Pageable pageable);
}

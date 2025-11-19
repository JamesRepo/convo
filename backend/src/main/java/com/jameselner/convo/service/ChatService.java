package com.jameselner.convo.service;

import com.jameselner.convo.dto.ChatMessageDTO;
import com.jameselner.convo.dto.ChatRoomDTO;
import com.jameselner.convo.model.ChatRoom;
import com.jameselner.convo.model.Message;
import com.jameselner.convo.model.User;
import com.jameselner.convo.repository.ChatRoomRepository;
import com.jameselner.convo.repository.MessageRepository;
import com.jameselner.convo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public Message saveMessage(final String username, final Long chatRoomId, final String content) {
        return saveMessage(username, chatRoomId, content, Message.MessageType.TEXT);
    }

    public Message saveMessage(
            final String username,
            final Long chatRoomId,
            final String content,
            final Message.MessageType messageType
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + chatRoomId));

        Message message = Message.builder()
                .sender(user)
                .chatRoom(chatRoom)
                .content(content)
                .timestamp(LocalDateTime.now())
                .type(messageType)
                .build();

        return messageRepository.save(message);
    }

    public Page<ChatMessageDTO> getChatRoomMessages(final Long chatRoomId, final int page, final int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Message> messages = messageRepository.findByChatRoomIdOrderByTimestampDesc(
                chatRoomId, pageable
        );

        return messages.map(this::convertToDTO);
    }

    public List<ChatMessageDTO> searchMessages(final Long chatRoomId, final String keyword) {
        List<Message> messages = messageRepository.searchInChatRoom(chatRoomId, keyword);
        return messages.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public void markMessageAsRead(final Long messageId, final Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));

        message.getReadByUserIds().add(userId);
        messageRepository.save(message);
    }

    @Transactional
    public ChatRoomDTO createChatRoom(final String name, final String description, final String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        ChatRoom chatRoom = ChatRoom.builder()
                .name(name)
                .description(description)
                .createdBy(creator)
                .roomType(ChatRoom.RoomType.PUBLIC)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        return new ChatRoomDTO(savedRoom);
    }

    @Transactional
    public ChatRoomDTO updateChatRoom(final Long roomId, final String name, final String description) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + roomId));

        chatRoom.setName(name);
        chatRoom.setDescription(description);

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        return new ChatRoomDTO(savedRoom);
    }

    @Transactional
    public void deleteChatRoom(final Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + roomId));
        
        chatRoomRepository.delete(chatRoom);
    }

    public List<ChatRoomDTO> getAllPublicRooms() {
        List<ChatRoom> rooms = chatRoomRepository.findByRoomType(ChatRoom.RoomType.PUBLIC);
        return rooms.stream()
                .map(ChatRoomDTO::new)
                .toList();
    }

    public ChatRoomDTO getChatRoomById(final Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + roomId));
        return new ChatRoomDTO(room);
    }

    public ChatMessageDTO convertToDTO(final Message message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setSenderId(message.getSender().getId());
        dto.setChatRoomId(message.getChatRoom().getId());
        dto.setContent(message.getContent());
        dto.setType(mapMessageType(message.getType()));
        dto.setTimestamp(message.getTimestamp());
        dto.setEdited(message.isEdited());
        dto.setReadByCount(message.getReadByUserIds() != null ? message.getReadByUserIds().size() : 0);
        return dto;
    }

    private ChatMessageDTO.MessageType mapMessageType(final Message.MessageType messageType) {
        if (messageType == null) {
            return ChatMessageDTO.MessageType.CHAT;
        }

        return switch (messageType) {
            case TYPING_INDICATOR -> ChatMessageDTO.MessageType.TYPING;
            case SYSTEM -> ChatMessageDTO.MessageType.SYSTEM;
            case ORACLE -> ChatMessageDTO.MessageType.ORACLE;
            default -> ChatMessageDTO.MessageType.CHAT;
        };
    }
}

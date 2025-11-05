package com.jameselner.convo.service;

import com.jameselner.convo.dto.ChatMessageDTO;
import com.jameselner.convo.model.ChatRoom;
import com.jameselner.convo.model.Message;
import com.jameselner.convo.model.User;
import com.jameselner.convo.repository.ChatRoomRepository;
import com.jameselner.convo.repository.MessageRepository;
import com.jameselner.convo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService service;

    @Test
    void saveMessage_persistsMessageWithExpectedFields() {
        // Arrange
        String username = "alice";
        Long roomId = 42L;
        String content = "Hello world";

        User user = new User();
        user.setUsername(username);

        ChatRoom room = new ChatRoom();
        room.setId(roomId);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime before = LocalDateTime.now();

        // Act
        Message saved = service.saveMessage(username, roomId, content);

        LocalDateTime after = LocalDateTime.now();

        // Assert
        assertNotNull(saved, "Saved message should not be null");
        assertEquals(user, saved.getSender(), "Sender should be the resolved user");
        assertEquals(room, saved.getChatRoom(), "Chat room should be the resolved room");
        assertEquals(content, saved.getContent(), "Content should be preserved");
        assertEquals(Message.MessageType.TEXT, saved.getType(), "Type should be TEXT by default");
        assertNotNull(saved.getTimestamp(), "Timestamp should be set");
        assertFalse(saved.getTimestamp().isBefore(before), "Timestamp should be within call window");
        assertFalse(saved.getTimestamp().isAfter(after), "Timestamp should be within call window");

        verify(userRepository, times(1)).findByUsername(username);
        verify(chatRoomRepository, times(1)).findById(roomId);
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, times(1)).save(captor.capture());
        assertEquals(content, captor.getValue().getContent());
    }

    @Test
    void saveMessage_throwsWhenUserNotFound() {
        // Arrange
        String username = "missing";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.saveMessage(username, 1L, "hi"));
        assertTrue(ex.getMessage().contains("User not found"));
        verify(chatRoomRepository, never()).findById(anyLong());
        verify(messageRepository, never()).save(any());
    }

    @Test
    void saveMessage_throwsWhenChatRoomNotFound() {
        // Arrange
        String username = "alice";
        Long roomId = 99L;

        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.saveMessage(username, roomId, "hi"));
        assertTrue(ex.getMessage().contains("Chat room not found"));
        verify(messageRepository, never()).save(any());
    }

    @Test
    void getChatRoomMessages_usesPagingAndSort_andMapsToDTOs() {
        // Arrange
        Long roomId = 7L;
        int page = 1;
        int size = 3;

        User u = new User();
        u.setUsername("bob");

        ChatRoom room = new ChatRoom();
        room.setId(roomId);

        Message m1 = Message.builder()
                .sender(u).chatRoom(room).content("A")
                .timestamp(LocalDateTime.now()).type(Message.MessageType.TEXT).build();
        Message m2 = Message.builder()
                .sender(u).chatRoom(room).content("B")
                .timestamp(LocalDateTime.now().minusMinutes(1)).type(Message.MessageType.TEXT).build();

        Page<Message> repoPage = new PageImpl<>(List.of(m1, m2));
        when(messageRepository.findByChatRoomIdOrderByTimestampDesc(eq(roomId), any(Pageable.class)))
                .thenReturn(repoPage);

        // Act
        Page<ChatMessageDTO> result = service.getChatRoomMessages(roomId, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size(), "Should map all messages to DTOs");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository, times(1))
                .findByChatRoomIdOrderByTimestampDesc(eq(roomId), pageableCaptor.capture());

        Pageable used = pageableCaptor.getValue();
        assertEquals(page, used.getPageNumber(), "Page index should match");
        assertEquals(size, used.getPageSize(), "Page size should match");
        assertNotNull(used.getSort().getOrderFor("timestamp"), "Should sort by timestamp");
        assertTrue(used.getSort().getOrderFor("timestamp").isDescending(), "Sort should be DESC");
    }

    @Test
    void searchMessages_mapsEntitiesToDTOs() {
        // Arrange
        Long roomId = 5L;
        String keyword = "hello";

        User u = new User();
        u.setUsername("carol");

        ChatRoom room = new ChatRoom();
        room.setId(roomId);

        List<Message> messages = List.of(
                Message.builder().sender(u).chatRoom(room).content("hello there")
                        .timestamp(LocalDateTime.now()).type(Message.MessageType.TEXT).build(),
                Message.builder().sender(u).chatRoom(room).content("well, hello again")
                        .timestamp(LocalDateTime.now()).type(Message.MessageType.TEXT).build()
        );

        when(messageRepository.searchInChatRoom(roomId, keyword)).thenReturn(messages);

        // Act
        var result = service.searchMessages(roomId, keyword);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size(), "Should map all search results to DTOs");
        verify(messageRepository, times(1)).searchInChatRoom(roomId, keyword);
    }
}

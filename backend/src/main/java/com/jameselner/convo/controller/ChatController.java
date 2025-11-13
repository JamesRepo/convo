package com.jameselner.convo.controller;

import com.jameselner.convo.dto.ChatMessageDTO;
import com.jameselner.convo.dto.ChatRoomDTO;
import com.jameselner.convo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/room")
    public ResponseEntity<ChatRoomDTO> createChatRoom(
            @RequestBody final Map<String, String> request,
            final Authentication authentication
    ) {
        ChatRoomDTO room = chatService.createChatRoom(
                request.get("name"),
                request.get("description"),
                authentication.getName()
        );
        return ResponseEntity.ok(room);
    }

    @PutMapping("/room/{roomId}")
    public ResponseEntity<ChatRoomDTO> updateChatRoom(
            @PathVariable final Long roomId,
            @RequestBody final Map<String, String> request
    ) {
        ChatRoomDTO room = chatService.updateChatRoom(
                roomId,
                request.get("name"),
                request.get("description")
        );
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable final Long roomId) {
        chatService.deleteChatRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getAllRooms() {
        List<ChatRoomDTO> rooms = chatService.getAllPublicRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ChatRoomDTO> getChatRoom(@PathVariable final Long roomId) {
        ChatRoomDTO chatRoom = chatService.getChatRoomById(roomId);
        return ResponseEntity.ok(chatRoom);
    }

    @GetMapping("/room/{roomId}/messages")
    public ResponseEntity<Page<ChatMessageDTO>> getRoomMessages(
            @PathVariable final Long roomId,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "50") final int size
    ) {
        Page<ChatMessageDTO> chatRoomMessages = chatService.getChatRoomMessages(roomId, page, size);
        return ResponseEntity.ok(chatRoomMessages);
    }

    @GetMapping("/room/{roomId}/search")
    public ResponseEntity<List<ChatMessageDTO>> searchMessages(
            @PathVariable final Long roomId,
            @RequestParam final String keyword
    ) {
        List<ChatMessageDTO> messages = chatService.searchMessages(roomId, keyword);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable final Long messageId,
            final Authentication authentication
    ) {
        // Implement getting user ID from authentication
        return ResponseEntity.ok().build();
    }
}

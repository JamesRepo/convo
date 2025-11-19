package com.jameselner.convo.websocket;

import com.jameselner.convo.dto.ChatMessageDTO;
import com.jameselner.convo.dto.UserStatusDTO;
import com.jameselner.convo.model.Message;
import com.jameselner.convo.service.ChatService;
import com.jameselner.convo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming chat messages
     * Clients send to: /app/chat/{roomId}
     * Server broadcasts to: /topic/room/{roomId}
     */
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessageDTO sendMessage(
            @DestinationVariable final Long roomId,
            @Payload final ChatMessageDTO messageDTO,
            final Principal principal
    ) {

        log.info("Received message from {} in room {}", principal.getName(), roomId);

        // Save a message to a database
        Message savedMessage = chatService.saveMessage(
                principal.getName(),
                roomId,
                messageDTO.getContent()
        );

        // Convert to DTO and broadcast
        return chatService.convertToDTO(savedMessage);
    }

    /**
     * Handle typing indicators
     * Clients send to: /app/typing/{roomId}
     * Server broadcasts to: /topic/typing/{roomId}
     */
    @MessageMapping("/typing/{roomId}")
    @SendTo("/topic/typing/{roomId}")
    public ChatMessageDTO handleTyping(
            @DestinationVariable final Long roomId,
            @Payload final ChatMessageDTO messageDTO,
            final Principal principal
    ) {
        messageDTO.setSenderUsername(principal.getName());
        messageDTO.setType(ChatMessageDTO.MessageType.TYPING);
        return messageDTO;
    }

    /**
     * Handle user joining a chat room
     */
    @MessageMapping("/join/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessageDTO userJoin(
            @DestinationVariable final Long roomId,
            final Principal principal
    ) {

        log.info("User {} joined room {}", principal.getName(), roomId);

        userService.updateUserStatus(principal.getName(), "ONLINE");

        return ChatMessageDTO.builder()
                .senderUsername(principal.getName())
                .chatRoomId(roomId)
                .content(principal.getName() + " joined the chat")
                .type(ChatMessageDTO.MessageType.JOIN)
                .build();
    }

    /**
     * Handle user leaving a chat room
     */
    @MessageMapping("/leave/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessageDTO userLeave(
            @DestinationVariable final Long roomId,
            final Principal principal
    ) {

        log.info("User {} left room {}", principal.getName(), roomId);

        return ChatMessageDTO.builder()
                .senderUsername(principal.getName())
                .chatRoomId(roomId)
                .content(principal.getName() + " left the chat")
                .type(ChatMessageDTO.MessageType.LEAVE)
                .build();
    }

    /**
     * Broadcast user status changes
     */
    public void broadcastUserStatus(final UserStatusDTO statusDTO) {
        messagingTemplate.convertAndSend("/topic/user-status", statusDTO);
    }
}
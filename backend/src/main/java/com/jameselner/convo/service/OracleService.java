package com.jameselner.convo.service;

import com.jameselner.convo.model.ChatRoom;
import com.jameselner.convo.model.Message;
import com.jameselner.convo.model.User;
import com.jameselner.convo.repository.ChatRoomRepository;
import com.jameselner.convo.repository.MessageRepository;
import com.jameselner.convo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OracleService {

    private static final int MAX_HISTORY = 300;
    private static final int MAX_TOKENS = 40;
    private static final String ORACLE_USERNAME = "Oracle";
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\w']+|[.,!?;:]");

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    @Transactional
    public Message askOracle(final Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + roomId));

        List<Message> recentMessages = loadRecentMessages(roomId);
        List<String> tokens = tokenizeMessages(recentMessages);

        String prophecy = tokens.isEmpty()
                ? "The oracle is silent until more words are spoken."
                : generateProphecy(tokens);

        Message oracleMessage = Message.builder()
                .sender(getOracleUser())
                .chatRoom(chatRoom)
                .content(prophecy)
                .timestamp(LocalDateTime.now())
                .type(Message.MessageType.ORACLE)
                .build();

        return messageRepository.save(oracleMessage);
    }

    private List<Message> loadRecentMessages(final Long roomId) {
        Pageable pageable = PageRequest.of(0, MAX_HISTORY);
        List<Message> messages = messageRepository.findTopByChatRoomId(roomId, pageable);
        // Reverse to chronological order for smoother chains
        Collections.reverse(messages);
        return messages;
    }

    private List<String> tokenizeMessages(final List<Message> messages) {
        List<String> tokens = new ArrayList<>();
        for (Message message : messages) {
            if (message.getContent() == null || message.getContent().isBlank()) {
                continue;
            }
            Matcher matcher = TOKEN_PATTERN.matcher(message.getContent());
            while (matcher.find()) {
                tokens.add(matcher.group());
            }
        }
        return tokens;
    }

    private String generateProphecy(final List<String> tokens) {
        if (tokens.size() == 1) {
            return tokens.get(0);
        }

        Map<String, List<String>> transitions = buildTransitions(tokens);

        String current = pickStartingToken(tokens);
        List<String> generated = new ArrayList<>();
        generated.add(current);

        for (int i = 1; i < MAX_TOKENS; i++) {
            List<String> nextTokens = transitions.getOrDefault(current, Collections.emptyList());
            if (nextTokens.isEmpty()) {
                break;
            }
            current = nextTokens.get(random.nextInt(nextTokens.size()));
            generated.add(current);

            if (isTerminalToken(current)) {
                break;
            }
        }

        return joinTokens(generated);
    }

    private Map<String, List<String>> buildTransitions(final List<String> tokens) {
        Map<String, List<String>> transitions = new HashMap<>();
        for (int i = 0; i < tokens.size() - 1; i++) {
            String key = tokens.get(i);
            String next = tokens.get(i + 1);
            transitions.computeIfAbsent(key, k -> new ArrayList<>()).add(next);
        }
        return transitions;
    }

    private String pickStartingToken(final List<String> tokens) {
        String token = tokens.get(random.nextInt(tokens.size()));
        int safety = 0;
        while (isTerminalToken(token) && safety < 5) {
            token = tokens.get(random.nextInt(tokens.size()));
            safety++;
        }
        return token;
    }

    private boolean isTerminalToken(final String token) {
        return ".".equals(token) || "!".equals(token) || "?".equals(token);
    }

    private String joinTokens(final List<String> tokens) {
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (builder.length() == 0) {
                builder.append(capitalize(token));
                continue;
            }

            if (isTerminalToken(token) || ",".equals(token) || ";".equals(token) || ":".equals(token)) {
                builder.append(token);
            } else {
                builder.append(' ').append(token);
            }
        }

        if (builder.length() > 0 && !isTerminalToken(tokens.get(tokens.size() - 1))) {
            builder.append('.');
        }

        return builder.toString();
    }

    private String capitalize(final String token) {
        if (token.isEmpty()) {
            return token;
        }
        return token.substring(0, 1).toUpperCase() + token.substring(1);
    }

    private User getOracleUser() {
        return userRepository.findByUsername(ORACLE_USERNAME)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username(ORACLE_USERNAME)
                                .email("oracle@convo.system")
                                .password(passwordEncoder.encode("oracle-secret"))
                                .status(User.UserStatus.OFFLINE)
                                .build()
                ));
    }
}

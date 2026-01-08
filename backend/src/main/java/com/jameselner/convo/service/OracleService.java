package com.jameselner.convo.service;

import com.jameselner.convo.dto.ChatMessageDTO;
import com.jameselner.convo.model.ChatRoom;
import com.jameselner.convo.model.Message;
import com.jameselner.convo.model.User;
import com.jameselner.convo.repository.ChatRoomRepository;
import com.jameselner.convo.repository.MessageRepository;
import com.jameselner.convo.repository.UserRepository;
import lombok.Getter;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OracleService {

    private static final int MAX_HISTORY = 300;
    private static final int MAX_TOKENS = 40;
    private static final int DEFAULT_CHAIN_ORDER = 2;
    private static final String ORACLE_USERNAME = "Oracle";
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\w']+|[.,!?;:]");

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    @Getter
    @RequiredArgsConstructor
    public static class OracleResult {
        private final Message message;
        private final ChatMessageDTO.OracleMetadata metadata;
    }

    @Transactional
    public OracleResult askOracle(final Long roomId) {
        return askOracle(roomId, DEFAULT_CHAIN_ORDER);
    }

    @Transactional
    public OracleResult askOracle(final Long roomId, final int chainOrder) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + roomId));

        List<Message> recentMessages = loadRecentMessages(roomId);
        List<String> tokens = tokenizeMessages(recentMessages);
        Set<String> uniqueTokens = new HashSet<>(tokens);

        int effectiveOrder = Math.min(chainOrder, Math.max(1, tokens.size() - 1));

        String prophecy = tokens.isEmpty()
                ? "The oracle is silent until more words are spoken."
                : generateProphecy(tokens, effectiveOrder);

        Message oracleMessage = Message.builder()
                .sender(getOracleUser())
                .chatRoom(chatRoom)
                .content(prophecy)
                .timestamp(LocalDateTime.now())
                .type(Message.MessageType.ORACLE)
                .build();

        Message savedMessage = messageRepository.save(oracleMessage);

        ChatMessageDTO.OracleMetadata metadata = ChatMessageDTO.OracleMetadata.builder()
                .messagesAnalyzed(recentMessages.size())
                .uniqueTokens(uniqueTokens.size())
                .chainOrder(effectiveOrder)
                .build();

        return new OracleResult(savedMessage, metadata);
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

    private String generateProphecy(final List<String> tokens, final int order) {
        if (tokens.size() <= order) {
            return String.join(" ", tokens);
        }

        Map<String, List<String>> transitions = buildTransitions(tokens, order);

        List<String> currentState = pickStartingState(tokens, order);
        List<String> generated = new ArrayList<>(currentState);

        for (int i = 0; i < MAX_TOKENS - order; i++) {
            String key = buildKey(currentState);
            List<String> nextTokens = transitions.getOrDefault(key, Collections.emptyList());
            if (nextTokens.isEmpty()) {
                break;
            }
            String next = nextTokens.get(random.nextInt(nextTokens.size()));
            generated.add(next);

            currentState = new ArrayList<>(currentState.subList(1, currentState.size()));
            currentState.add(next);

            if (isTerminalToken(next)) {
                break;
            }
        }

        return joinTokens(generated);
    }

    private Map<String, List<String>> buildTransitions(final List<String> tokens, final int order) {
        Map<String, List<String>> transitions = new HashMap<>();
        for (int i = 0; i <= tokens.size() - order - 1; i++) {
            List<String> state = tokens.subList(i, i + order);
            String key = buildKey(state);
            String next = tokens.get(i + order);
            transitions.computeIfAbsent(key, k -> new ArrayList<>()).add(next);
        }
        return transitions;
    }

    private String buildKey(final List<String> state) {
        return String.join("\u0000", state);
    }

    private List<String> pickStartingState(final List<String> tokens, final int order) {
        int maxStart = tokens.size() - order;
        int startIndex = random.nextInt(maxStart + 1);

        List<String> state = new ArrayList<>(tokens.subList(startIndex, startIndex + order));

        int safety = 0;
        while (safety < 10 && state.stream().anyMatch(this::isTerminalToken)) {
            startIndex = random.nextInt(maxStart + 1);
            state = new ArrayList<>(tokens.subList(startIndex, startIndex + order));
            safety++;
        }
        return state;
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

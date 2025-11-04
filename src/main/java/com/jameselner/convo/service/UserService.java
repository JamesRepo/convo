package com.jameselner.convo.service;

import com.jameselner.convo.dto.UserDTO;
import com.jameselner.convo.model.User;
import com.jameselner.convo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.factory.PasswordEncoderFactories;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Transactional
    public User registerUser(final String username, final String email, final String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists - " + username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists - " + email);
        }

        User user = User.builder()
                .username(username)
                .email(email)
//                .password(passwordEncoder.encode(password))
                .password(password)
                .status(User.UserStatus.OFFLINE)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void updateUserStatus(final String username, final String status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        user.setStatus(User.UserStatus.valueOf(status));
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
    }

    public UserDTO getUserByUsername(final String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return new UserDTO(user);
    }

    public List<UserDTO> getOnlineUsers() {
        List<User> users = userRepository.findByStatus(User.UserStatus.ONLINE);
        return users.stream()
                .map(UserDTO::new)
                .toList();
    }

    public Optional<User> findByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

//    @Transactional
//    public UserDTO updateProfile(String username, UserDTO userDTO) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if (userDTO.getFirstName() != null) {
//            user.setFirstName(userDTO.getFirstName());
//        }
//        if (userDTO.getLastName() != null) {
//            user.setLastName(userDTO.getLastName());
//        }
//        if (userDTO.getAvatarUrl() != null) {
//            user.setAvatarUrl(userDTO.getAvatarUrl());
//        }
//
//        userRepository.save(user);
//        return new UserDTO(user);
//    }
}
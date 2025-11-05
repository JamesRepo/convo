package com.jameselner.convo.service;

import com.jameselner.convo.dto.authentication.AuthenticationRequest;
import com.jameselner.convo.dto.authentication.AuthenticationResponse;
import com.jameselner.convo.dto.authentication.RegisterRequest;
import com.jameselner.convo.model.User;
import com.jameselner.convo.repository.UserRepository;
import com.jameselner.convo.security.CustomUserDetailsService;
import com.jameselner.convo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthenticationResponse register(final RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(User.UserStatus.OFFLINE)
                .build();

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthenticationResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    public AuthenticationResponse authenticate(final AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUsername()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthenticationResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
package com.jameselner.convo.service;

import com.jameselner.convo.dto.authentication.AuthenticationRequest;
import com.jameselner.convo.dto.authentication.AuthenticationResponse;
import com.jameselner.convo.dto.authentication.RegisterRequest;
import com.jameselner.convo.model.User;
import com.jameselner.convo.repository.UserRepository;
import com.jameselner.convo.security.CustomUserDetailsService;
import com.jameselner.convo.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService service;

    @Test
    void register_success_createsUser_encodesPassword_generatesToken() {
        // Arrange
        String username = "alice";
        String email = "alice@example.com";
        String rawPassword = "S3cret!";
        String encodedPassword = "{enc}S3cret!";
        String token = "mock-jwt-token";

        RegisterRequest request = mock(RegisterRequest.class);
        when(request.getUsername()).thenReturn(username);
        when(request.getEmail()).thenReturn(email);
        when(request.getPassword()).thenReturn(rawPassword);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // Simulate JPA assigning id on save
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(123L);
            return u;
        });

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username, encodedPassword, Collections.emptyList());
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(token);

        // Act
        AuthenticationResponse response = service.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(123L, response.getUserId());
        assertEquals(username, response.getUsername());
        assertEquals(email, response.getEmail());

        // Verify persistence and fields on saved user
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(username, saved.getUsername());
        assertEquals(email, saved.getEmail());
        assertEquals(encodedPassword, saved.getPassword());
        assertEquals(User.UserStatus.OFFLINE, saved.getStatus());

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(rawPassword);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void register_throws_whenUsernameExists() {
        // Arrange
        RegisterRequest request = mock(RegisterRequest.class);
        when(request.getUsername()).thenReturn("taken");
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.register(request));
        assertTrue(ex.getMessage().contains("Username already exists"));

        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void register_throws_whenEmailExists() {
        // Arrange
        RegisterRequest request = mock(RegisterRequest.class);
        when(request.getUsername()).thenReturn("newuser");
        when(request.getEmail()).thenReturn("dup@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.register(request));
        assertTrue(ex.getMessage().contains("Email already exists"));

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void authenticate_success_authenticates_loadsUser_generatesToken() {
        // Arrange
        String username = "bob";
        String rawPassword = "p@ss";
        String email = "bob@example.com";
        String token = "jwt-123";

        AuthenticationRequest request = mock(AuthenticationRequest.class);
        when(request.getUsername()).thenReturn(username);
        when(request.getPassword()).thenReturn(rawPassword);

        // AuthenticationManager returns an Authentication (value is ignored by service)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        User user = new User();
        user.setId(456L);
        user.setUsername(username);
        user.setEmail(email);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username, "ignored", Collections.emptyList());
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(token);

        // Act
        AuthenticationResponse response = service.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(456L, response.getUserId());
        assertEquals(username, response.getUsername());
        assertEquals(email, response.getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername(username);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void authenticate_throws_whenUserNotFoundAfterAuth() {
        // Arrange
        String username = "ghost";

        AuthenticationRequest request = mock(AuthenticationRequest.class);
        when(request.getUsername()).thenReturn(username);
        when(request.getPassword()).thenReturn("irrelevant");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.authenticate(request));
        assertTrue(ex.getMessage().contains("User not found"));

        verify(jwtUtil, never()).generateToken(any());
    }
}

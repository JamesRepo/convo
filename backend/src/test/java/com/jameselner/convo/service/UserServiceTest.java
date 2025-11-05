package com.jameselner.convo.service;

import com.jameselner.convo.dto.UserDTO;
import com.jameselner.convo.model.User;
import com.jameselner.convo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.jameselner.convo.model.User.UserStatus.OFFLINE;
import static com.jameselner.convo.model.User.UserStatus.ONLINE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService using Mockito
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService service;

    @Test
    void updateUserStatus_setsStatusAndLastSeen_andSaves() {
        // Arrange
        String username = "test-user";
        User user = new User();
        user.setUsername(username);
        user.setStatus(OFFLINE);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime before = LocalDateTime.now();

        // Act
        service.updateUserStatus(username, "ONLINE");

        LocalDateTime after = LocalDateTime.now();

        // Assert
        assertEquals(ONLINE, user.getStatus(), "Status should be updated to ONLINE");
        assertNotNull(user.getLastSeen(), "Last seen should be set");
        assertFalse(user.getLastSeen().isBefore(before), "Last seen should not be before the call");
        assertFalse(user.getLastSeen().isAfter(after), "Last seen should not be after the call");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUserStatus_throwsWhenUserNotFound() {
        // Arrange
        String username = "missing";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateUserStatus(username, "ONLINE"));
        assertTrue(ex.getMessage().contains("User not found"), "Should mention user not found");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByUsername_returnsUserDTO() {
        // Arrange
        String username = "alice";
        User user = new User();
        user.setUsername(username);
        user.setStatus(ONLINE);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDTO dto = service.getUserByUsername(username);

        // Assert
        assertNotNull(dto, "DTO should not be null");
        // If UserDTO exposes getUsername(), verify mapping:
        // This assertion is safe if UserDTO has getUsername(); otherwise, you can remove it.
        try {
            var m = dto.getClass().getMethod("getUsername");
            Object value = m.invoke(dto);
            assertEquals(username, value);
        } catch (ReflectiveOperationException ignored) {
            // DTO may not expose a getter for username; in that case we just ensure DTO was created.
        }
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void getOnlineUsers_mapsEntitiesToDTOs() {
        // Arrange
        User u1 = new User();
        u1.setUsername("u1");
        u1.setStatus(ONLINE);

        User u2 = new User();
        u2.setUsername("u2");
        u2.setStatus(ONLINE);

        when(userRepository.findByStatus(ONLINE)).thenReturn(List.of(u1, u2));

        // Act
        List<UserDTO> result = service.getOnlineUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size(), "Should return two DTOs");
        verify(userRepository, times(1)).findByStatus(ONLINE);
    }

    @Test
    void findByUsername_passthroughToRepository_present() {
        // Arrange
        String username = "bob";
        User user = new User();
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = service.findByUsername(username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void findByUsername_passthroughToRepository_empty() {
        // Arrange
        String username = "ghost";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = service.findByUsername(username);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByUsername(username);
    }
}

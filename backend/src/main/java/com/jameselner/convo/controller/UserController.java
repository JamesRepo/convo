package com.jameselner.convo.controller;

import com.jameselner.convo.dto.UserDTO;
import com.jameselner.convo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        UserDTO user = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/online")
    public ResponseEntity<List<UserDTO>> getOnlineUsers() {
        List<UserDTO> onlineUsers = userService.getOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }

//    @PutMapping("/me")
//    public ResponseEntity<UserDTO> updateProfile(
//            @RequestBody UserDTO userDTO,
//            Authentication authentication) {
//        UserDTO updated = userService.updateProfile(authentication.getName(), userDTO);
//        return ResponseEntity.ok(updated);
//    }

    @PutMapping("/status")
    public ResponseEntity<?> updateStatus(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        userService.updateUserStatus(authentication.getName(), request.get("status"));
        return ResponseEntity.ok().build();
    }
}
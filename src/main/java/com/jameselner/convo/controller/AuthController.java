package com.jameselner.convo.controller;


import com.jameselner.convo.dto.authentication.AuthenticationRequest;
import com.jameselner.convo.dto.authentication.RegisterRequest;
import com.jameselner.convo.model.User;
import com.jameselner.convo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );
            return ResponseEntity.ok("User registered successfully: " + request.getUsername());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequest request) {
        // Login logic handled by Spring Security
        return ResponseEntity.ok("Login successful");
    }



//    private final AuthenticationService authenticationService;
//
//    @PostMapping("/register")
//    public ResponseEntity<AuthenticationResponse> register(
//            @Valid @RequestBody RegisterRequest request) {
//        try {
//            AuthenticationResponse response = authenticationService.register(request);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<AuthenticationResponse> login(
//            @Valid @RequestBody AuthenticationRequest request) {
//        try {
//            AuthenticationResponse response = authenticationService.authenticate(request);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
}
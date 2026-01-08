package com.jameselner.convo.controller;

import com.jameselner.convo.dto.authentication.AuthenticationRequest;
import com.jameselner.convo.dto.authentication.AuthenticationResponse;
import com.jameselner.convo.dto.authentication.RegisterRequest;
import com.jameselner.convo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody final RegisterRequest request
    ) {
        AuthenticationResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody final AuthenticationRequest request
    ) {
        AuthenticationResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}
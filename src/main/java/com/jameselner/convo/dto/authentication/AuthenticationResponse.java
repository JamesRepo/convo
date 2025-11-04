package com.jameselner.convo.dto.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponse {

    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
}

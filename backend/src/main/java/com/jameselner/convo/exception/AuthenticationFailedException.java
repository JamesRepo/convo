package com.jameselner.convo.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationFailedException extends ConvoException {

    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;
    private static final String ERROR_CODE = "AUTHENTICATION_FAILED";

    public AuthenticationFailedException() {
        super("Invalid username or password", STATUS, ERROR_CODE);
    }
}

package com.jameselner.convo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ConvoException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    protected ConvoException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}

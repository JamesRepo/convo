package com.jameselner.convo.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ConvoException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String ERROR_CODE = "DUPLICATE_RESOURCE";

    public DuplicateResourceException(String resourceType, String field, String value) {
        super(
            String.format("%s with %s '%s' already exists", resourceType, field, value),
            STATUS,
            ERROR_CODE
        );
    }
}

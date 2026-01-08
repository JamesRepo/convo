package com.jameselner.convo.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ConvoException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String resourceType, Long id) {
        super(
            String.format("%s not found with ID: %d", resourceType, id),
            STATUS,
            ERROR_CODE
        );
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(
            String.format("%s not found: %s", resourceType, identifier),
            STATUS,
            ERROR_CODE
        );
    }
}

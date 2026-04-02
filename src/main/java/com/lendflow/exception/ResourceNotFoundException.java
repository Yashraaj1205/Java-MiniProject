package com.lendflow.exception;

public class ResourceNotFoundException extends LendFlowException {
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: [%s]", resource, field, value));
    }
}


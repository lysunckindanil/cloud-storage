package org.example.cloudstorage.exception;

import org.springframework.security.authentication.AuthenticationServiceException;

public class AuthenticationValidationException extends AuthenticationServiceException {
    public AuthenticationValidationException(String message) {
        super(message);
    }
}

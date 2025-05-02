package org.example.cloudstorage.exception;

import org.springframework.security.authentication.AuthenticationServiceException;

public class CustomAuthenticationValidationException extends AuthenticationServiceException {
    public CustomAuthenticationValidationException(String message) {
        super(message);
    }
}

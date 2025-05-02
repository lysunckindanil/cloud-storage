package org.example.cloudstorage.exception;

import org.springframework.security.authentication.AuthenticationServiceException;

public class UserWithThisNameAlreadyExistsException extends AuthenticationServiceException {
    public UserWithThisNameAlreadyExistsException(String message) {
        super(message);
    }
}

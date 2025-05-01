package org.example.cloudstorage.exception;

public class UserWithThisNameAlreadyExistsException extends RuntimeException {
    public UserWithThisNameAlreadyExistsException(String message) {
        super(message);
    }

}

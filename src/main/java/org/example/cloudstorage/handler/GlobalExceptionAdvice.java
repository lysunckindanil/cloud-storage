package org.example.cloudstorage.handler;

import org.example.cloudstorage.exception.UserWithThisNameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionAdvice {
    @ExceptionHandler(UserWithThisNameAlreadyExistsException.class)
    public ProblemDetail handleUserWithThisNameAlreadyExistsException(UserWithThisNameAlreadyExistsException e) {
        return wrapToProblemDetail(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BindException.class)
    public ProblemDetail handleBindException(BindException e) {
        return wrapToProblemDetail(
                e.getFieldErrors()
                        .stream()
                        .map(error -> "%s %s".formatted(error.getField(), error.getDefaultMessage()))
                        .collect(Collectors.joining(", ")),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleInternalServerError(Exception e) {
        return wrapToProblemDetail("Internal Server Error: " + e.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static ProblemDetail wrapToProblemDetail(String message, HttpStatus status) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setProperty("message", message);
        return problemDetail;
    }

}

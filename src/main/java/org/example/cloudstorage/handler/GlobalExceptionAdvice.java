package org.example.cloudstorage.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(MinioException.class)
    public ProblemDetail handleMinioException(MinioException e) {
        if (e instanceof InvalidPathMinioException) {
            return wrapToProblemDetail(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        log.error(e.getMessage(), e);
        return wrapToProblemDetail("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
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

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFoundException() {
        return wrapToProblemDetail("Resource Not Found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleInternalServerError(Exception e) {
        log.error(e.getMessage(), e);
        return wrapToProblemDetail("Internal Server Error: " + e.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static ProblemDetail wrapToProblemDetail(String message, HttpStatus status) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}

package org.example.cloudstorage.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.exception.minio.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(InvalidPathMinioException.class)
    public ProblemDetail handleInvalidPathMinioException(InvalidPathMinioException e) {
        log.debug("InvalidPathMinioException", e);
        return wrapToProblemDetail(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFileMinioException.class)
    public ProblemDetail handleInvalidFileMinioException(InvalidFileMinioException e) {
        log.debug("InvalidFileMinioException", e);
        return wrapToProblemDetail(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PartialDeletionMinioException.class)
    public ProblemDetail handlePartialDeletionMinioException(PartialDeletionMinioException e) {
        log.error("PartialDeletionMinioException.", e.getCause());
        return wrapToProblemDetail(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundMinioException.class)
    public ProblemDetail handleResourceNotFoundMinioException(ResourceNotFoundMinioException e) {
        log.debug("ResourceNotFoundMinioException", e);
        return wrapToProblemDetail(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsMinioException.class)
    public ProblemDetail handleResourceAlreadyExistsMinioException(ResourceAlreadyExistsMinioException e) {
        log.debug("ResourceAlreadyExistsMinioException", e);
        return wrapToProblemDetail(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MinioException.class)
    public ProblemDetail handleMinioException(MinioException e) {
        log.error("MinIO operation failed: {}", e.getMessage(), e);
        return wrapToProblemDetail("Internal server error with storage happened", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException e) {
        log.debug("ConstraintViolationException", e);
        return wrapToProblemDetail(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameterException(MissingServletRequestParameterException e) {
        log.debug("MissingServletRequestParameterException", e);
        return wrapToProblemDetail("Request parameter is missing: %s".formatted(e.getParameterName()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFoundException(NoResourceFoundException e) {
        log.debug("NoResourceFoundException", e);
        return wrapToProblemDetail("Resource was not found: %s".formatted(e.getResourcePath()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleInternalServerError(Exception e) {
        log.error("Undefined exception occurred", e);
        return wrapToProblemDetail("Internal server error happened: %s".formatted(e.getClass()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static ProblemDetail wrapToProblemDetail(String message, HttpStatus status) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}

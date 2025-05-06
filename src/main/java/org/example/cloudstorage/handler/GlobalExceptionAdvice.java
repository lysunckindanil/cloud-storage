package org.example.cloudstorage.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.exception.InvalidFilenameMinioException;
import org.example.cloudstorage.exception.MinioException;
import org.example.cloudstorage.exception.ResourceAlreadyExistsMinioException;
import org.example.cloudstorage.exception.ResourceNotFoundMinioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(InvalidFilenameMinioException.class)
    public ProblemDetail handleInvalidFilenameMinioException(InvalidFilenameMinioException e) {
        log.debug("MinioException", e);
        return wrapToProblemDetail(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundMinioException.class)
    public ProblemDetail handleResourceNotFoundMinioException(ResourceNotFoundMinioException e) {
        log.debug("MinioException", e);
        return wrapToProblemDetail("Provided path does not relate to any resource", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsMinioException.class)
    public ProblemDetail handleResourceAlreadyExistsMinioException(ResourceAlreadyExistsMinioException e) {
        log.debug("MinioException", e);
        return wrapToProblemDetail(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MinioException.class)
    public ProblemDetail handleMinioException(MinioException e) {
        log.error("MinIO operation failed: {}", e.getMessage());
        log.debug("Full error details", e);
        return wrapToProblemDetail("Internal Server Error (Minio)", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException e) {
        return wrapToProblemDetail(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameterException(MissingServletRequestParameterException e) {
        return wrapToProblemDetail("Request parameter is missing: %s".formatted(e.getParameterName()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFoundException(NoResourceFoundException e) {
        return wrapToProblemDetail("Resource Not Found: %s".formatted(e.getResourcePath()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleInternalServerError(Exception e) {
        log.error("Undefined exception occurred: {}", e.getMessage());
        log.debug("Full error details", e);
        return wrapToProblemDetail("Internal Server Error: %s".formatted(e.getClass()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static ProblemDetail wrapToProblemDetail(String message, HttpStatus status) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}

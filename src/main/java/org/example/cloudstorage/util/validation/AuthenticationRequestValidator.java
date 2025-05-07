package org.example.cloudstorage.util.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.example.cloudstorage.exception.AuthenticationValidationException;

import java.util.Set;
import java.util.stream.Collectors;

public class AuthenticationRequestValidator {
    public static <T> void validate(T request) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<T>> violations = validator.validate(request);
            if (!violations.isEmpty())
                throw new AuthenticationValidationException(
                        violations.stream()
                                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                                .collect(Collectors.joining(", ")));
        }
    }
}

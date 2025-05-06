package org.example.cloudstorage.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PathValidator implements ConstraintValidator<Path, String> {
    @Override
    public boolean isValid(String path, ConstraintValidatorContext constraintValidatorContext) {
        return PathUtils.isPathValid(path);
    }
}

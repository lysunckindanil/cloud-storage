package org.example.cloudstorage.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.cloudstorage.util.PathUtils;

public class PathValidator implements ConstraintValidator<Path, String> {
    @Override
    public boolean isValid(String path, ConstraintValidatorContext constraintValidatorContext) {
        return PathUtils.isPathValid(path);
    }
}

package org.example.cloudstorage.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PathValidator implements ConstraintValidator<Path, String> {
    @Override
    public boolean isValid(String key, ConstraintValidatorContext constraintValidatorContext) {
        if (key.isEmpty())
            return true;

        if (key.length() > 1024) {
            return false;
        }

        Pattern validPathPattern = Pattern.compile("^[a-zA-Z0-9!\\-_.*'()/]+$");

        if (!validPathPattern.matcher(key).matches()) {
            return false;
        }

        if (key.contains("//")) {
            return false;
        }

        return true;
    }
}

package com.intuit.task.manager.validation;

import javax.validation.*;
import java.util.Set;
import java.util.stream.*;

public class ValueInEnumValidator implements ConstraintValidator<ValueInEnum, String> {

    private Set<String> availableTypes;

    @Override
    public void initialize(ValueInEnum constraintAnnotation) {
        Class<? extends Enum<?>> enumSelected = constraintAnnotation.enumType();
        availableTypes = Stream.of(enumSelected.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return availableTypes.contains(value.toUpperCase());
    }
}

package com.intuit.task.manager.validation;

import javax.validation.*;
import java.util.Set;
import java.util.stream.*;

/**
 * Validator for custom annotation @ValueInEnum
 * @see ValueInEnum
 * Validator is an input String argument element of the specified enumeration.
 */
public class ValueInEnumValidator implements ConstraintValidator<ValueInEnum, String> {

    /**
     * A set that stores a list of all the values of the enum being checked
     */
    private Set<String> availableTypes;

    /**
     * Init method
     * Gets a list of all enum values and saves it to a set for later validation.
     *
     * @param constraintAnnotation is the type of the enum being checked
     */
    @Override
    public void initialize(ValueInEnum constraintAnnotation) {
        Class<? extends Enum<?>> enumSelected = constraintAnnotation.enumType();
        availableTypes = Stream.of(enumSelected.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    /**
     * Validation method
     * Check whether the current parameter is in the list of valid values.
     *
     * @param value is a checked value
     * @return the status of validation
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return availableTypes.contains(value.toUpperCase());
    }
}

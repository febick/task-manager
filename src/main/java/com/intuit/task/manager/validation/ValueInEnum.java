package com.intuit.task.manager.validation;

import javax.validation.*;
import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A validated annotation to check if a value is in an enumerable list
 */
@Documented
@Constraint(validatedBy = ValueInEnumValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface ValueInEnum {

    Class<? extends Enum<?>> enumType();
    String message() default "This value is not supported";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}

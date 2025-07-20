package io.github.soulcodingmatt.equilibrium.annotations.dto.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Type-safe wrapper for Jakarta Bean Validation @Min annotation.
 * Used within @ValidateDto to ensure compile-time validation.
 */
@Target({}) // Only used as parameter in other annotations
@Retention(RetentionPolicy.SOURCE)
public @interface Min {
    /**
     * The minimum value (inclusive).
     * @return the minimum value
     */
    long value();
    
    /**
     * The error message to be interpolated at the time of validation.
     * @return the error message
     */
    String message() default "must be greater than or equal to {value}";
}

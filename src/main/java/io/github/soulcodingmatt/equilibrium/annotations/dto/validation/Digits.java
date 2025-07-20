package io.github.soulcodingmatt.equilibrium.annotations.dto.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Type-safe wrapper for Jakarta Bean Validation @Digits annotation.
 * Used within @ValidateDto to ensure compile-time validation.
 */
@Target({}) // Only used as parameter in other annotations
@Retention(RetentionPolicy.SOURCE)
public @interface Digits {
    /**
     * The maximum number of integral digits accepted for this number.
     * @return the maximum number of integral digits
     */
    int integer();
    
    /**
     * The maximum number of fractional digits accepted for this number.
     * @return the maximum number of fractional digits
     */
    int fraction();
    
    /**
     * The error message to be interpolated at the time of validation.
     * @return the error message
     */
    String message() default "numeric value out of bounds (<{integer} digits>.<{fraction} digits> expected)";
}

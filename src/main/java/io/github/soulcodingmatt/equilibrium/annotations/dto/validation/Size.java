package io.github.soulcodingmatt.equilibrium.annotations.dto.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Type-safe wrapper for Jakarta Bean Validation @Size annotation.
 * Used within @ValidateDto to ensure compile-time validation.
 */
@Target({}) // Only used as parameter in other annotations
@Retention(RetentionPolicy.SOURCE)
public @interface Size {
    /**
     * The minimum size of the element.
     * @return the minimum size
     */
    int min() default 0;
    
    /**
     * The maximum size of the element.
     * @return the maximum size
     */
    int max() default Integer.MAX_VALUE;
    
    /**
     * The error message to be interpolated at the time of validation.
     * @return the error message
     */
    String message() default "size must be between {min} and {max}";
}

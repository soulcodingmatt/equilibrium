package io.github.soulcodingmatt.equilibrium.annotations.dto.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Type-safe wrapper for Jakarta Bean Validation @Email annotation.
 * Used within @ValidateDto to ensure compile-time validation.
 */
@Target({}) // Only used as parameter in other annotations
@Retention(RetentionPolicy.SOURCE)
public @interface Email {
    /**
     * The regular expression to match. Defaults to the built-in email pattern.
     * @return the regular expression
     */
    String regexp() default ".*";
    
    /**
     * Array of {@code Flag}s considered when resolving the regular expression.
     * @return the flags
     */
    String[] flags() default {};
    
    /**
     * The error message to be interpolated at the time of validation.
     * @return the error message
     */
    String message() default "must be a well-formed email address";
}

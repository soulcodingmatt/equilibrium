package io.github.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for repeatable {@code @ValidateDto} annotations.
 * This allows multiple {@code @ValidateDto} annotations to be applied to the same field,
 * each targeting different DTO generation IDs.
 * 
 * This annotation is used internally by the Java compiler when multiple
 * {@code @ValidateDto} annotations are applied to the same field. Users should not
 * use this annotation directly.
 * 
 * Usage example:
 * <pre>
 * {@code
 * @ValidateDto(
 *     notNull = @NotNull(message = "Name cannot be null"),
 *     ids = {1}
 * )
 * @ValidateDto(
 *     size = @Size(min = 5, max = 50, message = "Admin name must be between 5 and 50 characters"),
 *     ids = {2}
 * )
 * private String name;
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface ValidateDtos {
    /**
     * Array of ValidateDto annotations.
     * @return the array of ValidateDto annotations
     */
    ValidateDto[] value();
} 
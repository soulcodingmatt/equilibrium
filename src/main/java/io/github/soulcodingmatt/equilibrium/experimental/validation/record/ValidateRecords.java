package io.github.soulcodingmatt.equilibrium.experimental.validation.record;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for repeatable {@code @ValidateRecord} annotations. This allows multiple
 * {@code @ValidateRecord} annotations to be applied to the same field, each targeting different
 * Record generation IDs.
 *
 * <p>This annotation is used internally by the Java compiler when multiple {@code @ValidateRecord}
 * annotations are applied to the same field. Users should not use this annotation directly.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * @ValidateRecord(
 *     notNull = @NotNull(message = "Name cannot be null"),
 *     ids = {1}
 * )
 * @ValidateRecord(
 *     size = @Size(min = 5, max = 50, message = "Admin name must be between 5 and 50 characters"),
 *     ids = {2}
 * )
 * private String name;
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface ValidateRecords {
  /**
   * Array of ValidateRecord annotations.
   *
   * @return the array of ValidateRecord annotations
   */
  ValidateRecord[] value();
}

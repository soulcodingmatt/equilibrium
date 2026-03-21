package io.github.soulcodingmatt.equilibrium.annotations.vo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for repeatable {@code @ValidateVo} annotations. This allows multiple
 * {@code @ValidateVo} annotations to be applied to the same field, each targeting different VO
 * generation IDs.
 *
 * <p>This annotation is used internally by the Java compiler when multiple {@code @ValidateVo}
 * annotations are applied to the same field. Users should not use this annotation directly.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * @ValidateVo(
 *     notNull = @NotNull(message = "Name cannot be null"),
 *     ids = {1}
 * )
 * @ValidateVo(
 *     size = @Size(min = 5, max = 50, message = "Admin name must be between 5 and 50 characters"),
 *     ids = {2}
 * )
 * private String name;
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface ValidateVos {
  /**
   * Array of ValidateVo annotations.
   *
   * @return the array of ValidateVo annotations
   */
  ValidateVo[] value();
}

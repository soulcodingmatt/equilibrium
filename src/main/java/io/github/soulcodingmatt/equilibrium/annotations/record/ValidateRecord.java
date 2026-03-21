package io.github.soulcodingmatt.equilibrium.annotations.record;

import io.github.soulcodingmatt.equilibrium.annotations.dto.validation.*;
import java.lang.annotation.*;

/**
 * Annotation to add Jakarta Bean Validation annotations to the corresponding component in the
 * generated Record class. The validation annotations specified here will be copied to the generated
 * Record component with compile-time type safety.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * @ValidateRecord(
 *     notNull = @NotNull(message = "Name cannot be null"),
 *     size = @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
 * )
 * private String name;
 *
 * @ValidateRecord(
 *     min = @Min(value = 18, message = "Age must be at least 18"),
 *     max = @Max(value = 120, message = "Age must be at most 120")
 * )
 * private Integer age;
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(ValidateRecords.class)
public @interface ValidateRecord {
  /**
   * Adds @NotNull validation to the component.
   *
   * @return the NotNull validation configuration
   */
  NotNull notNull() default @NotNull(message = "");

  /**
   * Adds @NotBlank validation to the component.
   *
   * @return the NotBlank validation configuration
   */
  NotBlank notBlank() default @NotBlank(message = "");

  /**
   * Adds @NotEmpty validation to the component.
   *
   * @return the NotEmpty validation configuration
   */
  NotEmpty notEmpty() default @NotEmpty(message = "");

  /**
   * Adds @Size validation to the component.
   *
   * @return the Size validation configuration
   */
  Size size() default @Size(min = -1, max = -1, message = "");

  /**
   * Adds @Min validation to the component.
   *
   * @return the Min validation configuration
   */
  Min min() default @Min(value = Long.MIN_VALUE, message = "");

  /**
   * Adds @Max validation to the component.
   *
   * @return the Max validation configuration
   */
  Max max() default @Max(value = Long.MAX_VALUE, message = "");

  /**
   * Adds @Email validation to the component.
   *
   * @return the Email validation configuration
   */
  Email email() default @Email(message = "");

  /**
   * Adds @Pattern validation to the component.
   *
   * @return the Pattern validation configuration
   */
  Pattern pattern() default @Pattern(regexp = "", message = "");

  /**
   * Adds @Positive validation to the component.
   *
   * @return the Positive validation configuration
   */
  Positive positive() default @Positive(message = "");

  /**
   * Adds @PositiveOrZero validation to the component.
   *
   * @return the PositiveOrZero validation configuration
   */
  PositiveOrZero positiveOrZero() default @PositiveOrZero(message = "");

  /**
   * Adds @Negative validation to the component.
   *
   * @return the Negative validation configuration
   */
  Negative negative() default @Negative(message = "");

  /**
   * Adds @NegativeOrZero validation to the component.
   *
   * @return the NegativeOrZero validation configuration
   */
  NegativeOrZero negativeOrZero() default @NegativeOrZero(message = "");

  /**
   * Adds @Digits validation to the component.
   *
   * @return the Digits validation configuration
   */
  Digits digits() default @Digits(integer = -1, fraction = -1, message = "");

  /**
   * Adds @Past validation to the component.
   *
   * @return the Past validation configuration
   */
  Past past() default @Past(message = "");

  /**
   * Adds @Future validation to the component.
   *
   * @return the Future validation configuration
   */
  Future future() default @Future(message = "");

  /**
   * Adds @PastOrPresent validation to the component.
   *
   * @return the PastOrPresent validation configuration
   */
  PastOrPresent pastOrPresent() default @PastOrPresent(message = "");

  /**
   * Adds @FutureOrPresent validation to the component.
   *
   * @return the FutureOrPresent validation configuration
   */
  FutureOrPresent futureOrPresent() default @FutureOrPresent(message = "");

  /**
   * Array of validation annotation strings for advanced cases not covered by type-safe options. Use
   * the type-safe parameters above when possible.
   *
   * @return array of validation annotation strings
   */
  String[] value() default {};

  /**
   * Optional list of @GenerateRecord IDs for which this validation should be applied. If not
   * specified, the validation will be applied to ALL generated Records. If specified, the
   * validation will only be applied to Records whose @GenerateRecord annotation has an ID that is
   * present in this list.
   *
   * @return array of Record generation IDs for which to apply this validation
   */
  int[] ids() default {};
}

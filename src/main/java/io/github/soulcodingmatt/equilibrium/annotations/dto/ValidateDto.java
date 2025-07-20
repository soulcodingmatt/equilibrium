package io.github.soulcodingmatt.equilibrium.annotations.dto;

import io.github.soulcodingmatt.equilibrium.annotations.dto.validation.*;

import java.lang.annotation.*;

/**
 * Annotation to add Jakarta Bean Validation annotations to the corresponding field
 * in the generated DTO class. The validation annotations specified here will be
 * copied to the generated DTO field with compile-time type safety.
 * 
 * Usage example:
 * <pre>
 * {@code
 * @ValidateDto(
 *     notNull = @NotNull(message = "Name cannot be null"),
 *     size = @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
 * )
 * private String name;
 * 
 * @ValidateDto(
 *     min = @Min(value = 18, message = "Age must be at least 18"),
 *     max = @Max(value = 120, message = "Age must be at most 120")
 * )
 * private Integer age;
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(ValidateDtos.class)
public @interface ValidateDto {
    /**
     * Adds @NotNull validation to the field.
     * @return the NotNull validation configuration
     */
    NotNull notNull() default @NotNull(message = "");
    
    /**
     * Adds @NotBlank validation to the field.
     * @return the NotBlank validation configuration
     */
    NotBlank notBlank() default @NotBlank(message = "");
    
    /**
     * Adds @NotEmpty validation to the field.
     * @return the NotEmpty validation configuration
     */
    NotEmpty notEmpty() default @NotEmpty(message = "");
    
    /**
     * Adds @Size validation to the field.
     * @return the Size validation configuration
     */
    Size size() default @Size(min = -1, max = -1, message = "");
    
    /**
     * Adds @Min validation to the field.
     * @return the Min validation configuration
     */
    Min min() default @Min(value = Long.MIN_VALUE, message = "");
    
    /**
     * Adds @Max validation to the field.
     * @return the Max validation configuration
     */
    Max max() default @Max(value = Long.MAX_VALUE, message = "");
    
    /**
     * Adds @Email validation to the field.
     * @return the Email validation configuration
     */
    Email email() default @Email(message = "");
    
    /**
     * Adds @Pattern validation to the field.
     * @return the Pattern validation configuration
     */
    Pattern pattern() default @Pattern(regexp = "", message = "");
    
    /**
     * Adds @Positive validation to the field.
     * @return the Positive validation configuration
     */
    Positive positive() default @Positive(message = "");
    
    /**
     * Adds @PositiveOrZero validation to the field.
     * @return the PositiveOrZero validation configuration
     */
    PositiveOrZero positiveOrZero() default @PositiveOrZero(message = "");
    
    /**
     * Adds @Negative validation to the field.
     * @return the Negative validation configuration
     */
    Negative negative() default @Negative(message = "");
    
    /**
     * Adds @NegativeOrZero validation to the field.
     * @return the NegativeOrZero validation configuration
     */
    NegativeOrZero negativeOrZero() default @NegativeOrZero(message = "");
    
    /**
     * Adds @Digits validation to the field.
     * @return the Digits validation configuration
     */
    Digits digits() default @Digits(integer = -1, fraction = -1, message = "");
    
    /**
     * Adds @Past validation to the field.
     * @return the Past validation configuration
     */
    Past past() default @Past(message = "");
    
    /**
     * Adds @Future validation to the field.
     * @return the Future validation configuration
     */
    Future future() default @Future(message = "");
    
    /**
     * Adds @PastOrPresent validation to the field.
     * @return the PastOrPresent validation configuration
     */
    PastOrPresent pastOrPresent() default @PastOrPresent(message = "");
    
    /**
     * Adds @FutureOrPresent validation to the field.
     * @return the FutureOrPresent validation configuration
     */
    FutureOrPresent futureOrPresent() default @FutureOrPresent(message = "");
    
    /**
     * Array of validation annotation strings for advanced cases not covered by type-safe options.
     * Use the type-safe parameters above when possible.
     * 
     * @return array of validation annotation strings
     */
    String[] value() default {};
    
    /**
     * Optional list of @GenerateDto IDs for which this validation should be applied.
     * If not specified, the validation will be applied to ALL generated DTOs.
     * If specified, the validation will only be applied to DTOs whose @GenerateDto annotation
     * has an ID that is present in this list.
     * 
     * @return array of DTO generation IDs for which to apply this validation
     */
    int[] ids() default {};
} 

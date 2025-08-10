package io.github.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.*;

/**
 * Annotation to specify default values for fields in generated DTOs when using the builder pattern.
 * This annotation only has effect when the {@code @GenerateDto} annotation has {@code builder = true}.
 *
 * <p>When applied to a field, the generated DTO will include a {@code @Builder.Default}
 * annotation with the specified default value.</p>
 *
 * <p>Type-specific parameters (recommended) provide compile-time validation and direct values:</p>
 *
 * <p><b>Primitive types (direct values, no quotes):</b></p>
 * <ul>
 *   <li>{@code @DtoBuilderDefault(intValue = 42)} → {@code private int count = 42;}</li>
 *   <li>{@code @DtoBuilderDefault(booleanValue = true)} → {@code private boolean active = true;}</li>
 *   <li>{@code @DtoBuilderDefault(doubleValue = 3.14)} → {@code private double pi = 3.14;}</li>
 *   <li>{@code @DtoBuilderDefault(charValue = 'A')} → {@code private char grade = 'A';}</li>
 *   <li>{@code @DtoBuilderDefault(longValue = 100L)} → {@code private long timestamp = 100L;}</li>
 *   <li>{@code @DtoBuilderDefault(floatValue = 1.5f)} → {@code private float ratio = 1.5f;}</li>
 *   <li>{@code @DtoBuilderDefault(byteValue = 127)} → {@code private byte flags = 127;}</li>
 *   <li>{@code @DtoBuilderDefault(shortValue = 32767)} → {@code private short port = 32767;}</li>
 * </ul>
 *
 * <p><b>String fields (quotes added automatically):</b></p>
 * <ul>
 *   <li>{@code @DtoBuilderDefault(stringValue = "Hello")} generates {@code private String name = "Hello";}</li>
 *   <li>{@code @DtoBuilderDefault(stringValue = "\"Quoted\"")} generates {@code private String text = "\"Quoted\"";}</li>
 * </ul>
 *
 * <p><b>Enum fields:</b> use {@link #enumValue()} with either {@code "EnumName.CONSTANT"}
 * or just {@code "CONSTANT"}. The processor validates that the constant exists and matches
 * the field's enum type. Example: {@code @DtoBuilderDefault(enumValue = "Status.ACTIVE")} →
 * {@code private Status status = Status.ACTIVE;}</p>
 *
 * <p><b>Collections and Optional:</b> when used without parameters on supported types, defaults are applied:</p>
 * <ul>
 *   <li>{@code List} → {@code new ArrayList<>()}</li>
 *   <li>{@code Set} → {@code new HashSet<>()}</li>
 *   <li>{@code Map} → {@code new HashMap<>()}</li>
 *   <li>{@code Optional} → {@code Optional.empty()}</li>
 * </ul>
 *
 * <p><b>Legacy escape hatch:</b> if no type-specific parameter applies, the {@link #value()}
 * parameter can be used as a raw Java initializer for unsupported types or complex expressions
 * (e.g., {@code BigDecimal}, {@code UUID}, {@code java.time}, arrays, non-empty collections/optionals).
 * The expression is emitted as-is in the generated code.</p>
 *
 * <p><b>Errors:</b> For non-collection, non-Optional fields, if no applicable parameter is provided
 * (and {@code value()} is empty), an error is reported during processing.</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface DtoBuilderDefault {
    /**
     * Controls whether an existing Lombok {@code @Builder.Default} initializer on the base field
     * should be inherited into the generated DTO.
     *
     * <p>If set to {@code false}, the processor will NOT carry over the base-class default for this field.
     * If no other parameters are specified to provide an explicit default, the generated DTO field will have
     * no builder default initializer and will not receive a {@code @Builder.Default} annotation.</p>
     *
     * <p>Default is {@code true} for backward compatibility.</p>
     */
    boolean inherit() default true;
    /**
     * String default value (quotes added automatically).
     * Use for String fields or when you need string literals.
     * 
     * @return the string default value
     */
    String stringValue() default "";
    
    /**
     * Integer default value.
     * Use for int and Integer fields.
     * 
     * @return the integer default value
     */
    int intValue() default Integer.MIN_VALUE;
    
    /**
     * Long default value.
     * Use for long and Long fields.
     * 
     * @return the long default value
     */
    long longValue() default Long.MIN_VALUE;
    
    /**
     * Short default value.
     * Use for short and Short fields.
     * 
     * @return the short default value
     */
    short shortValue() default Short.MIN_VALUE;
    
    /**
     * Byte default value.
     * Use for byte and Byte fields.
     * 
     * @return the byte default value
     */
    byte byteValue() default Byte.MIN_VALUE;
    
    /**
     * Float default value.
     * Use for float and Float fields.
     * 
     * @return the float default value
     */
    float floatValue() default Float.MIN_VALUE;
    
    /**
     * Double default value.
     * Use for double and Double fields.
     * 
     * @return the double default value
     */
    double doubleValue() default Double.MIN_VALUE;
    
    /**
     * Boolean default value.
     * Use for boolean and Boolean fields.
     * 
     * @return the boolean default value
     */
    boolean booleanValue() default false;
    
    /**
     * Character default value.
     * Use for char and Character fields.
     * 
     * @return the character default value
     */
    char charValue() default '\0';
    
    /**
     * Enum default value. Use for enum fields.
     * <p>Accepts either {@code "CONSTANT"} or {@code "EnumName.CONSTANT"}. The processor validates
     * that the constant exists and belongs to the field's enum type. Example:
     * {@code @DtoBuilderDefault(enumValue = "Status.ACTIVE")}.</p>
     *
     * @return the enum default value
     */
    String enumValue() default "";
    
    /**
     * Legacy string-based initializer that is emitted as-is into the generated field initializer.
     * <p>Use as an escape hatch for unsupported types or complex initializers (e.g.,
     * {@code new java.math.BigDecimal("12.34")}, {@code java.util.UUID.fromString("...")},
     * {@code java.time.LocalDate.of(2025, 1, 1)}, {@code Optional.of(...)}, collections with contents,
     * or to force an explicit boolean {@code false}).</p>
     *
     * @return the legacy string value (raw Java expression)
     */
    String value() default "";
} 

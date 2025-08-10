package io.github.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.*;

/**
 * Annotation to specify default values for fields in generated DTOs when using the builder pattern.
 * This annotation only has effect when the @GenerateDto annotation has builder = true.
 * 
 * When applied to a field, the generated DTO will include a @Builder.Default annotation
 * with the specified default value.
 * 
 * Type-safe default values with direct primitive and enum support:
 * 
 * Primitive types (direct values, no quotes):
 * - @DtoBuilderDefault(intValue = 42) for int fields → private int count = 42;
 * - @DtoBuilderDefault(booleanValue = true) for boolean fields → private boolean active = true;
 * - @DtoBuilderDefault(doubleValue = 3.14) for double fields → private double pi = 3.14;
 * - @DtoBuilderDefault(charValue = 'A') for char fields → private char grade = 'A';
 * - @DtoBuilderDefault(longValue = 100L) for long fields → private long timestamp = 100L;
 * - @DtoBuilderDefault(floatValue = 1.5f) for float fields → private float ratio = 1.5f;
 * - @DtoBuilderDefault(byteValue = 127) for byte fields → private byte flags = 127;
 * - @DtoBuilderDefault(shortValue = 32767) for short fields → private short port = 32767;
 * 
 * String fields (quotes added automatically):
 * - @DtoBuilderDefault(stringValue = "Hello") generates: private String name = "Hello";
 * - @DtoBuilderDefault(stringValue = "\"Quoted\"") generates: private String text = "\"Quoted\"";
 * 
 * Enum fields (type-safe with static final variables):
 * - @DtoBuilderDefault(enumValue = "Status.ACTIVE") generates: private Status status = Status.ACTIVE;
 * - @DtoBuilderDefault(enumValue = "Status.INACTIVE") generates: private Status status = Status.INACTIVE;
 * 
 * Note: For type safety, use the full enum reference like "Status.ACTIVE" in the enumValue parameter.
 * 
 * Compile-time validation ensures type safety and provides clear error messages
 * for invalid values or type mismatches.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface DtoBuilderDefault {
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
     * Enum default value.
     * Use for enum fields. The enum constant will be used directly.
     * For type-safe enum usage, use the full enum reference:
     * &#64;DtoBuilderDefault(enumValue = "Status.ACTIVE")
     * 
     * @return the enum default value
     */
    String enumValue() default "";
    
    /**
     * Legacy string value parameter for backward compatibility.
     * For enums, use the full reference like "Status.ACTIVE" for type safety.
     * @deprecated Use the type-specific parameters instead for better type safety.
     * @return the legacy string value
     */
    @Deprecated
    String value() default "";
} 
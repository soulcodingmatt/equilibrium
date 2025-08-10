# @DtoBuilderDefault Improvements

## Problem

The original `@DtoBuilderDefault` annotation only supported string-based values, requiring users to write:
```java
@DtoBuilderDefault("42")  // String instead of int
@DtoBuilderDefault("true") // String instead of boolean
@DtoBuilderDefault("ACTIVE") // String instead of enum constant
```

This was not intuitive and lacked type safety.

## Solution: Type-Specific Parameters

We introduced multiple type-specific parameters to allow direct value input:

```java
@DtoBuilderDefault(
    intValue = 42,           // Direct int value
    booleanValue = true,     // Direct boolean value
    doubleValue = 3.14,      // Direct double value
    charValue = 'A',         // Direct char value
    longValue = 100L,        // Direct long value
    floatValue = 1.5f,       // Direct float value
    byteValue = 127,         // Direct byte value
    shortValue = 32767,      // Direct short value
    stringValue = "Hello",   // String value (quotes added automatically)
    enumValue = "Status.ACTIVE"  // Type-safe enum constant
)
```

## Technical Implementation

### Annotation Definition

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface DtoBuilderDefault {
    String stringValue() default "";
    int intValue() default Integer.MIN_VALUE;
    long longValue() default Long.MIN_VALUE;
    short shortValue() default Short.MIN_VALUE;
    byte byteValue() default Byte.MIN_VALUE;
    float floatValue() default Float.MIN_VALUE;
    double doubleValue() default Double.MIN_VALUE;
    boolean booleanValue() default false;
    char charValue() default '\0';
    String enumValue() default "";        // String-based enum reference
    @Deprecated
    String value() default "";            // Legacy support
}
```

### Processing Logic

The annotation processor prioritizes type-specific parameters:

1. **Type Detection**: Analyzes field type to determine which parameter to use
2. **Value Validation**: Validates values against field types at compile time
3. **Fallback**: Falls back to legacy `value()` parameter for backward compatibility
4. **Special Handling**: Collections and Optional get default constructors

### Key Features

- **Type Safety**: Compile-time validation prevents type mismatches
- **Direct Values**: Use `42` instead of `"42"`
- **Enum Support**: Type-safe enum defaults using enumValue parameter:
  - `enumValue = "Status.ACTIVE"` (type-safe, validates constant exists)
- **Backward Compatibility**: Legacy string-based approach still works
- **Error Reporting**: Clear error messages for invalid values

## Usage Examples

### Before (Legacy)
```java
@DtoBuilderDefault("42")
private int age;

@DtoBuilderDefault("true")
private boolean active;

@DtoBuilderDefault("ACTIVE")
private Status status;
```

### After (New)
```java
@DtoBuilderDefault(intValue = 42)
private int age;

@DtoBuilderDefault(booleanValue = true)
private boolean active;

@DtoBuilderDefault(enumValue = "Status.ACTIVE")
private Status status;
```

## Generated Output

```java
@SuperBuilder
public class UserDto {
    @Builder.Default
    private int age = 42;
    
    @Builder.Default
    private boolean active = true;
    
    @Builder.Default
    private Status status = Status.ACTIVE;
}
```

## Benefits

✅ **Direct Values**: No more string literals for numbers and booleans  
✅ **Type Safety**: Compile-time validation prevents errors  
✅ **IDE Support**: Better autocomplete and refactoring  
✅ **Performance**: No runtime string parsing  
✅ **Backward Compatible**: Existing code continues to work  
✅ **Enum Validation**: Ensures enum constants exist  
✅ **Type-Safe Enums**: enumValue parameter provides compile-time enum validation  

## Migration Guide

1. **Primitive Types**: Replace `@DtoBuilderDefault("42")` with `@DtoBuilderDefault(intValue = 42)`
2. **Enums**: Replace `@DtoBuilderDefault("ACTIVE")` with `@DtoBuilderDefault(enumValue = "Status.ACTIVE")`
3. **Strings**: Replace `@DtoBuilderDefault("Hello")` with `@DtoBuilderDefault(stringValue = "Hello")`
4. **Collections**: Use `@DtoBuilderDefault` without parameters for default constructors

## Enum Type Safety

For enums, we now provide a type-safe enumValue parameter approach:

```java
// ✅ Type-safe: Use enumValue parameter with full enum reference
@DtoBuilderDefault(enumValue = "Status.ACTIVE")
private Status status;

@DtoBuilderDefault(enumValue = "Status.INACTIVE")
private Status otherStatus;
```

The enumValue parameter:
- Takes a string representation of the enum constant (e.g., "Status.ACTIVE")
- Validates that the enum constant exists at compile time
- Provides clear error messages if the constant doesn't exist
- Works exactly like the legacy value() parameter but with better validation

This solution provides the exact functionality requested - direct input of values like `42` and type-safe enum references! 🚀 
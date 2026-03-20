# @DtoBuilderDefault Usage Examples

## New Type-Specific Parameters (Recommended)

Now you can use direct values without quotes! 🎉

### Primitive Types

```java
@GenerateDto(builder = true)
public class User {
    @DtoBuilderDefault(intValue = 42)
    private int age;
    
    @DtoBuilderDefault(booleanValue = true)
    private boolean active;
    
    @DtoBuilderDefault(doubleValue = 3.14)
    private double pi;
    
    @DtoBuilderDefault(charValue = 'A')
    private char grade;
    
    @DtoBuilderDefault(longValue = 100L)
    private long timestamp;
    
    @DtoBuilderDefault(floatValue = 1.5f)
    private float ratio;
    
    @DtoBuilderDefault(byteValue = 127)
    private byte flags;
    
    @DtoBuilderDefault(shortValue = 32767)
    private short port;
}
```

### Wrapper Types

```java
@DtoBuilderDefault(intValue = 42)
private Integer age;

@DtoBuilderDefault(booleanValue = true)
private Boolean active;

@DtoBuilderDefault(doubleValue = 3.14)
private Double pi;
```

### String Types

```java
@DtoBuilderDefault(stringValue = "Hello World")
private String message;

@DtoBuilderDefault(stringValue = "\"Already quoted\"")
private String preQuoted;
```

### Enum Types

```java
public enum Status {
    ACTIVE, INACTIVE, PENDING
}

// ✅ Type-safe approach: Use enumValue parameter with enum constant
@DtoBuilderDefault(enumValue = "Status.ACTIVE")
private Status status;

@DtoBuilderDefault(enumValue = "Status.INACTIVE")
private Status otherStatus;
```

## Generated Output

The annotation processor will generate:

```java
@SuperBuilder
public class UserDto {
    @Builder.Default
    private int age = 42;
    
    @Builder.Default
    private boolean active = true;
    
    @Builder.Default
    private double pi = 3.14;
    
    @Builder.Default
    private char grade = 'A';
    
    @Builder.Default
    private long timestamp = 100L;
    
    @Builder.Default
    private float ratio = 1.5f;
    
    @Builder.Default
    private byte flags = 127;
    
    @Builder.Default
    private short port = 32767;
    
    @Builder.Default
    private String message = "Hello World";
    
    @Builder.Default
    private Status status = Status.ACTIVE;
}
```

## Legacy String-Based Approach (Still Supported)

For backward compatibility, the old string-based approach still works:

```java
@DtoBuilderDefault(value = "42")
private int age;

@DtoBuilderDefault(value = "true")
private boolean active;

@DtoBuilderDefault(value = "ACTIVE")
private Status status;
```

## Collections and Optional

For collections and Optional types, you can use the annotation without any parameters:

```java
@DtoBuilderDefault
private List<String> tags;  // defaults to new ArrayList<>()

@DtoBuilderDefault
private Set<Integer> numbers;  // defaults to new HashSet<>()

@DtoBuilderDefault
private Map<String, Object> metadata;  // defaults to new HashMap<>()

@DtoBuilderDefault
private Optional<String> description;  // defaults to Optional.empty()
```

## Using existing @Builder.Default on the base class (safe carry-over)

When you use `@GenerateDto(builder = true)`, the processor will mirror an existing Lombok `@Builder.Default` from the base class and, for a safe subset of initializer forms, it will copy the initializer into the DTO.

### What is automatically carried over

- Literals: numbers (including negatives), booleans, chars, strings, null
- Enum constants: emitted as `EnumSimpleName.CONSTANT`
- No-arg constructors: `new ArrayList<>()`, `new HashSet<>()`, `new HashMap<>()`
- Empties: `Optional.empty()`, `Collections.emptyList()`, `Collections.emptySet()`, `Collections.emptyMap()`

The processor also auto-adds required imports (e.g., `List`, `ArrayList`, `Optional`, `Collections`).

### Example (base → generated DTO)

Base class:

```java
public class User {
    @Builder.Default
    private int height = -10;

    @Builder.Default
    private java.util.List<String> tags = new java.util.ArrayList<>();

    @Builder.Default
    private Status status = Status.ACTIVE;

    @Builder.Default
    private java.util.Optional<String> note = java.util.Optional.empty();

    @Builder.Default
    private java.util.Map<String, Integer> scores = new java.util.HashMap<>();
}
```

Generated DTO (excerpt):

```java
@SuperBuilder
public class UserDto {
    @Builder.Default
    private int height = -10;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private Status status = Status.ACTIVE;

    @Builder.Default
    private Optional<String> note = Optional.empty();

    @Builder.Default
    private Map<String, Integer> scores = new HashMap<>();
}
```

### Not carried over (use `@DtoBuilderDefault` instead)

- Method calls beyond the whitelisted empties: `Optional.of(...)`, `List/Set/Map.of(...)`, `BigDecimal.valueOf(...)`, `LocalDate.of(...)`, `UUID.fromString(...)`, etc.
- Constructors other than the allowed no-arg `ArrayList/HashSet/HashMap`, or any constructor with arguments
- Static non-enum constants: `BigDecimal.TEN`, `Duration.ZERO`, `Math.PI`, etc.
- Arrays and collections with contents: `new int[]{...}`, `Arrays.asList(...)`, unmodifiable wrappers
- Expressions: arithmetic/concatenation, ternaries, casts, chains, lambdas, method references

For these cases, specify the initializer via `@DtoBuilderDefault` (type-specific parameters or `value()` escape hatch).

## Opting out of inherited defaults (inherit = false)

You can prevent a base-class `@Builder.Default` initializer from being used in the DTO by attaching `@DtoBuilderDefault(inherit = false)` to the same field in the base class.

Base class:

```java
@GenerateDto(builder = true)
public class User {
    // Base default you do NOT want in the DTO
    @Builder.Default
    @DtoBuilderDefault(inherit = false)
    private int height = -10;
}
```

Generated DTO (excerpt):

```java
@SuperBuilder
public class UserDto {
    // No @Builder.Default and no initializer
    private int height;
}
```

You can also override the inherited default with an explicit one:

```java
@GenerateDto(builder = true)
public class User {
    @Builder.Default
    // Provide a new default for the DTO instead of inheriting the base one
    @DtoBuilderDefault(intValue = 5)
    private int height = -10;
}
```

Generated DTO (excerpt):

```java
@SuperBuilder
public class UserDto {
    @Builder.Default
    private int height = 5;
}
```

## Error Handling

The annotation processor provides clear error messages:

```java
// ❌ Error: Invalid enum reference format (must be "EnumName.CONSTANT" or just "CONSTANT")
@DtoBuilderDefault(enumValue = "com.example.Status")
private Status status;

// ❌ Error: Enum reference name does not match the field's enum type
@DtoBuilderDefault(enumValue = "OtherEnum.ACTIVE")
private Status status;

// ❌ Error: Enum constant does not exist in enum Status
@DtoBuilderDefault(enumValue = "Status.INVALID")
private Status status;

// ❌ Error: No value specified for non-collection, non-Optional field
@DtoBuilderDefault
private String name;
```

## Migration from Legacy to New

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

## Benefits

✅ **Direct Values**: Use `42` instead of `"42"`  
✅ **Type Safety**: Compile-time validation  
✅ **IDE Support**: Autocomplete and refactoring  
✅ **Performance**: No string parsing at runtime  
✅ **Backward Compatible**: Legacy code still works

This solution gives you exactly what you wanted - direct input of values like `42` and `MyEnum.VALUE1` without quotes! 🚀

## Advanced: Using value() for unsupported types and complex initializers

While `value()` is deprecated in the API, it remains supported as an escape hatch for cases not covered by the type-specific parameters. The value string is emitted as a raw Java initializer.

Examples:

```java
// BigDecimal
@DtoBuilderDefault(value = "new java.math.BigDecimal(\"12.34\")")
private java.math.BigDecimal price;

// UUID
@DtoBuilderDefault(value = "java.util.UUID.fromString(\"123e4567-e89b-12d3-a456-426614174000\")")
private java.util.UUID id;

// java.time
@DtoBuilderDefault(value = "java.time.LocalDate.of(2025, 1, 1)")
private java.time.LocalDate startDate;

// Optional (non-empty)
@DtoBuilderDefault(value = "java.util.Optional.of(\"Hello\")")
private java.util.Optional<String> greeting;

// Collections with contents (no @NestedMapping)
@DtoBuilderDefault(value = "new java.util.ArrayList<>(java.util.List.of(new Address(\"x\")))")
private java.util.List<Address> addresses;

// Collections with contents (with @NestedMapping to a DTO)
@NestedMapping(dtoClass = AddressDto.class)
@DtoBuilderDefault(value = "new java.util.ArrayList<>(java.util.List.of(new AddressDto(\"x\")))")
private java.util.List<Address> addresses;

// Maps
@DtoBuilderDefault(value = "new java.util.HashMap<>(java.util.Map.of(\"k\", 1))")
private java.util.Map<String, Integer> data;

// Arrays
@DtoBuilderDefault(value = "new int[]{1, 2, 3}")
private int[] numbers;

// Explicit boolean false (edge case)
// Use value() to force false when no other parameters are set
@DtoBuilderDefault(value = "false")
private boolean disabled;
```

Notes:
- The processor auto-imports field types and standard collection types when needed. If your initializer references additional classes, either fully qualify them (as above) or ensure they are available via imports.
- Prefer type-specific parameters when possible; use `value()` for unsupported types or complex initializers.

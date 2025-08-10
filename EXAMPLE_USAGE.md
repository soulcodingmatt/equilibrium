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

// ✅ Type-safe approach: Use enumValue parameter with full enum reference
@DtoBuilderDefault(enumValue = "Status.ACTIVE")
private Status status;

@DtoBuilderDefault(enumValue = "Status.INACTIVE")
private Status otherStatus;

// ⚠️ Limited approach: Use enumValue parameter (uses first enum constant)
@DtoBuilderDefault(enumValue = "com.example.Status")
private Status limitedStatus;
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

## Error Handling

The annotation processor provides clear error messages:

```java
// ❌ Error: Invalid enum constant 'INVALID' does not exist in enum Status
@DtoBuilderDefault(enumConstant = "INVALID")
private Status status;

// ❌ Error: Multiple parameters specified for field: age
@DtoBuilderDefault(intValue = 42, stringValue = "42")
private int age;

// ❌ Error: No value specified for non-collection field: name
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
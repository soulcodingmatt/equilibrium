# Annotations reference

## @GenerateDto

Annotate your domain classes with `@GenerateDto`:

```java
@GenerateDto
public class User {
    private String name;
    private int age;
    
    @IgnoreDto
    private String internalId;
    
    // getters and setters
}
```

### Arguments for @GenerateDto

`id`

- Usage: `@GenerateDto(id=1)` or `@GenerateDto(id=2)`
- Default: unset. When set, identifies this generation so `@IgnoreDto`, `@NestedDtoMapping`, and validation annotations can target **this** DTO variant when several `@GenerateDto` annotations exist on the same class.

`ignore`

- Usage: `@GenerateDto(ignore="fieldname")` or `@GenerateDto(ignore={"field1", "field2"})`
- Default: unset. Excludes the listed fields from this DTO generation (alternative to `@IgnoreDto` on each field).

`pkg`

- Usage: `@GenerateDto(pkg="org.thisisanexample.dto")`
- Default: from compiler options for DTOs (see [Configuration](configuration.md)).

`name`

- Usage: `@GenerateDto(name="MyCustomDto")`
- Default: from compiler options; if those are absent, the postfix defaults to `Dto`.

`builder`

- Usage: `@GenerateDto(builder=true)`
- Default: `false`. When `true`, adds Lombok’s `@SuperBuilder` to the generated class. **Lombok must be on the annotation processor path.**

## @DtoBuilderDefault

Use `@DtoBuilderDefault` on domain fields when **`@GenerateDto(builder=true)`** is set. It drives **`@Builder.Default`** on the **generated** DTO: **type-specific parameters** (for example `intValue`, `stringValue`, `enumValue`), **empty defaults** for `List`, `Set`, `Map`, and `Optional` when you use `@DtoBuilderDefault` alone, optional **inheritance** of Lombok **`@Builder.Default`** initializers from the source class (with an **`inherit = false`** opt-out), and a **`value()`** escape hatch for expressions the typed API does not cover (see the annotation Javadoc).

For setup (Lombok), generated shape (`@SuperBuilder`), and practical guidance, see **[DTO builder pattern](dto-builder-pattern.md)**.

## @GenerateRecord

### Arguments for @GenerateRecord

`id`

- Usage: `@GenerateRecord(id=1)` or `@GenerateRecord(id=2)`
- Default: unset. Used with `@IgnoreRecord` and `@ValidateRecord` to target specific record generations.

`ignore`

- Usage: `@GenerateRecord(ignore="fieldname")` or `@GenerateRecord(ignore={"field1", "field2"})`
- Default: unset. Same idea as for DTOs.

`pkg`

- Usage: `@GenerateRecord(pkg="org.thisisanexample.record")`
- Default: compiler options for records.

`name`

- Usage: `@GenerateRecord(name="MyCustomRecord")`
- Default: compiler options; if absent, postfix defaults to `Record`.

## @GenerateVo

### Arguments for @GenerateVo

`id`

- Usage: `@GenerateVo(id=1)` or `@GenerateVo(id=2)`
- Default: unset. Used with `@IgnoreVo` and `@ValidateVo` for selective behavior.

`ignore`

- Usage: `@GenerateVo(ignore="fieldname")` or `@GenerateVo(ignore={"field1", "field2"})`
- Default: unset. Value objects often omit identity fields; use ignores accordingly.

`pkg`

- Usage: `@GenerateVo(pkg="org.thisisanexample.vo")`
- Default: compiler options for VOs.

`name`

- Usage: `@GenerateVo(name="MyCustomVo")`
- Default: compiler options; if absent, postfix defaults to `Vo`.

`setters`

- Usage: `@GenerateVo(setters=true)`
- Default: `false`. By default, VOs are **immutable** (`final` fields, no setters), which matches common value-object usage. Mutable transfer types are usually better modeled with **`@GenerateDto`**; `setters=true` is for exceptional cases.

## @IgnoreDto, @IgnoreRecord, @IgnoreVo, @IgnoreAll

`@IgnoreDto`, `@IgnoreRecord`, and `@IgnoreVo` exclude fields from the matching generated type. `@IgnoreAll` excludes a field from **all** generated DTO, record, and VO outputs.

### Selective exclusion with `ids`

- `@IgnoreDto(ids={1, 2})` — exclude only from DTO generations with those `@GenerateDto` ids
- `@IgnoreRecord(ids={1, 2})` — same for records
- `@IgnoreVo(ids={1, 2})` — same for VOs

If `ids` is omitted, the ignore applies to every generation of that kind.

## Multiple `@Generate*` annotations on one class

You can repeat `@GenerateDto`, `@GenerateVo`, and `@GenerateRecord` on the same class to produce several variants (different packages, postfixes, or ids).

**Rule:** each variant must produce a **unique** output path (package + generated simple name). If two configurations resolve to the same file, compilation fails.

**Example:**

```java
@GenerateDto(id=1, pkg="com.example.dto.api", name="UserApiDto")
@GenerateDto(id=2, pkg="com.example.dto.internal", name="UserInternalDto")
@GenerateVo(id=1, pkg="com.example.vo", name="UserVo")
@GenerateVo(id=2, pkg="com.example.vo", name="UserValueObject")
public class User {
    private String name;
    private int age;
    
    @IgnoreDto(ids={1})  // Only excluded from ApiDto, included in InternalDto
    private String internalId;
    
    @IgnoreVo(ids={2})   // Only excluded from UserValueObject, included in UserVo
    private String temporaryField;
}
```

This yields `UserApiDto`, `UserInternalDto`, `UserVo`, and `UserValueObject` in the packages you set.

## @NestedMapping

`@NestedMapping` picks **one** DTO type for a field and applies it to **all** DTO generations that share the same nested shape, using a `Class<?>` reference.

| Parameter  |    Type    |                   Description                   |
|------------|------------|-------------------------------------------------|
| `dtoClass` | `Class<?>` | DTO class used for this field in generated DTOs |

Use **at most one `@NestedMapping` per field** (it applies across all generated DTOs for that field).

```java
public class User {
    @NestedMapping(dtoClass = VoiceDto.class)
    private Voice voice;
    
    @NestedMapping(dtoClass = AddressDto.class)
    private Address address;
    
    // Other fields...
}
```

## @NestedDtoMapping

When you have **several `@GenerateDto` ids** and need **different nested DTO types per id**, use **`@NestedDtoMapping`** (repeatable) with **`dtoClassName`** (fully qualified name) and optional **`ids`**. Empty `ids` means the mapping applies to all DTO generations. This avoids limitations around multiple `Class` references on the same field.

## Experimental: `@ValidateDto`, `@ValidateRecord`, `@ValidateVo`

These annotations add **Jakarta Bean Validation** constraints on **generated** DTO, record, or VO members using **type-safe** parameters in `io.github.soulcodingmatt.equilibrium.experimental.validation` (`…validation.dto`, `…validation.record`, `…validation.vo`, shared constraint types in `…validation.common`). The API may change between releases.

**Details and full parameter list:** **[Experimental validation](experimental-validation.md)** (classpath, `ids`, `value()` escape hatch, packages).

### Behavior notes

- **Validation is analyzed before generation.** Invalid combinations (for example contradictory constraints, `@NotNull` on a primitive, or `@NotBlank` on a non-`String` field) are **compile errors** on your domain class, not silent fixes in generated code.
- **Standard Jakarta annotations** on your sources are recognized in the **same compilation** as Project Equilibrium, so mixed usage is fine.
- Use the **`ids`** parameter to limit constraints to specific `@GenerateDto` / `@GenerateRecord` / `@GenerateVo` ids; if omitted, validation applies to all generations of that kind.

### Example (`@ValidateDto`)

```java
import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;

public class User {
    @ValidateDto(
        notNull = @NotNull(message = "Name cannot be null"),
        size = @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    )
    private String name;
    
    @ValidateDto(
        min = @Min(value = 18, message = "Age must be at least 18"),
        max = @Max(value = 120, message = "Age must be at most 120")
    )
    private Integer age;
    
    @ValidateDto(
        email = @Email(message = "Invalid email format"),
        ids = {1, 2}
    )
    private String email;
}
```

Generated types receive the matching Jakarta annotations on their fields or components as appropriate.

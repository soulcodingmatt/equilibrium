# Experimental validation (Jakarta Bean Validation)

Project Equilibrium can emit **Jakarta Bean Validation** constraints onto **generated** DTO fields, record components, and VO fields. You declare rules on your **source** class using type-safe annotations; the processor copies the matching **`jakarta.validation.constraints.*`** annotations into the generated type.

This feature lives under **`io.github.soulcodingmatt.equilibrium.experimental.validation`**. Packages may change between releases—treat this API as **experimental**.

## What you need on the classpath

- **Annotation API:** `jakarta.validation:jakarta.validation-api` (so the generated Jakarta constraint annotations resolve at compile time).
- **Runtime validation** (REST endpoints, manual validation) still requires a **Bean Validation implementation** (for example Hibernate Validator) in the runtime you use to validate instances—not specific to Project Equilibrium, but required if you actually run validation.

## Packages

|              Role               |                                  Package                                  |
|---------------------------------|---------------------------------------------------------------------------|
| DTO field validation            | `…experimental.validation.dto` (`@ValidateDto`, `@ValidateDtos`)          |
| Record component validation     | `…experimental.validation.record` (`@ValidateRecord`, `@ValidateRecords`) |
| VO field validation             | `…experimental.validation.vo` (`@ValidateVo`, `@ValidateVos`)             |
| Type-safe constraint parameters | `…experimental.validation.common` (`@NotNull`, `@Size`, …)                |

Import the **`common`** constraint types when you write `@ValidateDto(notNull = @NotNull(...))` and similar—they are **Project Equilibrium’s compile-time wrappers**, not the Jakarta classes you import on the generated side.

## Basic example (DTO)

```java
import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;

@GenerateDto
public class Person {

    @ValidateDto(
        notNull = @NotNull(message = "Name cannot be null"),
        notBlank = @NotBlank(message = "Name cannot be blank"),
        size = @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    )
    private String name;

    @ValidateDto(
        notNull = @NotNull(message = "Age cannot be null"),
        min = @Min(value = 0, message = "Age must be at least 0"),
        max = @Max(value = 150, message = "Age must be at most 150")
    )
    private Integer age;
}
```

`@ValidateRecord` and `@ValidateVo` use the same **`common`** constraint types and mirror the same parameters for their generated targets.

## Scoping with `ids`

When you have **several** `@GenerateDto`, `@GenerateRecord`, or `@GenerateVo` annotations on one class (different `id` values), use **`ids`** on `@Validate*` so constraints apply only to the matching generations.

Omit **`ids`** to apply the rule to **every** generation of that kind.

Example with multiple DTOs (each output type needs a **distinct** generated name—here via **`name`**):

```java
@GenerateDto(id = 1, name = "UserCreateDto")
@GenerateDto(id = 2, name = "UserUpdateDto")
@GenerateDto(id = 3, name = "UserViewDto")
public class User {

    @ValidateDto(
        notNull = @NotNull(message = "Username cannot be null"),
        notBlank = @NotBlank(message = "Username cannot be blank"),
        size = @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters"),
        pattern = @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Invalid username"),
        ids = {1, 2}
    )
    private String username;

    @ValidateDto(
        notNull = @NotNull(message = "Password cannot be null"),
        notBlank = @NotBlank(message = "Password cannot be blank"),
        size = @Size(min = 8, max = 100, message = "Password length"),
        ids = {1}
    )
    private String password;

    @ValidateDto(
        min = @Min(value = 13, message = "Must be at least 13"),
        max = @Max(value = 120, message = "Must be realistic")
    )
    private Integer age;
}
```

## Type-safe parameters (maps to Jakarta)

These wrappers are supported on `@ValidateDto` / `@ValidateRecord` / `@ValidateVo` (see library Javadoc for exact defaults and edge cases):

`notNull`, `notBlank`, `notEmpty`, `size`, `min`, `max`, `email`, `pattern`, `positive`, `positiveOrZero`, `negative`, `negativeOrZero`, `digits`, `past`, `future`, `pastOrPresent`, `futureOrPresent`.

They correspond to the **`jakarta.validation.constraints`** types of the same names.

## String `value()` escape hatch

Each `@Validate*` annotation defines **`String[] value()`** for **additional** constraint snippets not covered by the type-safe parameters—for example constraints that do not have a dedicated wrapper in this release. Prefer type-safe parameters when they exist; use **`value`** only when you need something outside that set.

## Type-constraint compatibility rules

The processor enforces these rules and reports **compile errors** when they are violated:

|                         Constraint                         |                         Allowed field types                          |
|------------------------------------------------------------|----------------------------------------------------------------------|
| `notNull`                                                  | Reference types only — **not** primitives (`int`, `boolean`, etc.)   |
| `notBlank`                                                 | `String` only                                                        |
| `notEmpty`                                                 | `String`, `Collection`, `Map`, or array                              |
| `size`                                                     | `String`, `Collection`, `Map`, or array; `min` and `max` must be ≥ 0 |
| `min`, `max`                                               | Numeric types (`int`, `long`, `Integer`, `Long`, etc.)               |
| `positive`, `positiveOrZero`, `negative`, `negativeOrZero` | Numeric types                                                        |
| `digits`                                                   | Numeric types                                                        |
| `email`, `pattern`                                         | `String` only                                                        |
| `past`, `future`, `pastOrPresent`, `futureOrPresent`       | Temporal types (`LocalDate`, `LocalDateTime`, `ZonedDateTime`, etc.) |

## Processing rules (short)

- Validation metadata is **checked before generation**. Violations of the compatibility rules above produce **compile errors** on the source class, not silent fixes in generated code.
- You can also use **standard Jakarta constraint annotations** elsewhere in the same compilation; the processor registers those types for processing as documented in the main README.

## See also

- Main README: [Experimental: `@ValidateDto`, `@ValidateRecord`, `@ValidateVo`](../README.md) — overview and parameter table.
- Javadoc: `ValidateDto`, `ValidateRecord`, `ValidateVo`, and types under `experimental.validation.common`.


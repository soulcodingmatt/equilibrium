# DTO builder pattern (`@GenerateDto(builder = true)`)

Use this when you want **Lombok’s builder** on generated DTOs. Project Equilibrium adds **`@SuperBuilder`** to the generated class and can emit **`@Builder.Default`** on fields so the builder starts from sensible values.

## Prerequisites

- **`@GenerateDto(builder = true)`** on your domain (or API) class.
- **Lombok** on your classpath and on the **annotation processor path** (see the main README). Project Equilibrium does not replace Lombok; it generates code that Lombok then expands.

If you do not need builders, omit `builder` and use constructors and setters instead.

## Specifying defaults with `@DtoBuilderDefault`

`@DtoBuilderDefault` is only meaningful when **`builder = true`**. It tells the processor what **initializer** to put on the generated field together with Lombok’s `@Builder.Default`.

### Type-specific parameters (recommended)

Use the parameter that matches the field type so values are checked at compile time:

|          Use          |                                            Annotation (examples)                                             |
|-----------------------|--------------------------------------------------------------------------------------------------------------|
| Primitives / wrappers | `intValue`, `longValue`, `booleanValue`, `doubleValue`, `floatValue`, `byteValue`, `shortValue`, `charValue` |
| `String`              | `stringValue = "..."` (quotes in source; the processor emits a valid Java string literal)                    |
| Enum                  | `enumValue = "Status.ACTIVE"` or `"ACTIVE"` (must match the field’s enum type)                               |

### Collections and `Optional`

For **`List`**, **`Set`**, **`Map`**, and **`Optional`**, you can use **`@DtoBuilderDefault` with no parameters** on the field. The processor applies empty defaults (for example `new ArrayList<>()`, `Optional.empty()`), as described in the `DtoBuilderDefault` Javadoc.

For **non-empty** collections or `Optional.of(...)`, use the **`value()`** escape hatch (below).

### Legacy / escape hatch: `value()`

The **`value()`** attribute accepts a **raw Java expression** copied into the generated initializer. Use it for types that have no dedicated parameter (`BigDecimal`, `UUID`, `java.time`, arrays, populated collections, explicit `false` when you cannot use `booleanValue`, and so on).

Example:

```java
@DtoBuilderDefault(value = "new java.math.BigDecimal(\"12.34\")")
private java.math.BigDecimal price;
```

You can also use **`value = "42"`**-style strings for simple cases; type-specific parameters are preferred when available.

```java
@DtoBuilderDefault(value = "42")
private int age; // prefer: intValue = 42
```

## Copying defaults from the domain class (`@Builder.Default`)

If the **source field** already has Lombok’s **`@Builder.Default`** with an initializer the processor understands, that initializer can be **copied** into the generated DTO (including imports where needed).

**Typically carried over:** literals; enum constants; `new ArrayList<>()` / `new HashSet<>()` / `new HashMap<>()` with no arguments; `Optional.empty()`; `Collections.emptyList()` / `emptySet()` / `emptyMap()`.

**Not carried over:** method calls beyond those patterns, static constants like `BigDecimal.TEN`, filled collections, arithmetic, ternaries, lambdas, and similar. For those, set the default with **`@DtoBuilderDefault`** (typed parameters or `value()`).

### Opt out: `inherit = false`

Set **`@DtoBuilderDefault(inherit = false)`** on the domain field to **stop** copying the base `@Builder.Default` into the DTO. The generated field then has **no** `@Builder.Default` unless you add another `@DtoBuilderDefault` with explicit values.

### Override an inherited default

Put **`@DtoBuilderDefault(intValue = 5)`** (or another parameter) on the field: the generated DTO uses **your** default, not the copied initializer from the domain.

## Generated shape (conceptual)

The processor aims for generated code along these lines:

```java
@SuperBuilder
public class ExampleDto {
    @Builder.Default
    private int age = 42;
    // ...
}
```

Exact names and imports depend on your class and packages.

## Invalid combinations

The processor reports errors for cases such as: **`enumValue`** that does not name a constant of the field’s enum; **`@DtoBuilderDefault` with no parameters** on a **non-collection, non-Optional** field where no default can be inferred; and other validation rules described in compiler messages. Prefer the **typed** parameters and the Javadoc on **`DtoBuilderDefault`** for the full contract.

## See also

- Main README: [Lombok (builders)](../README.md#lombok-builders) — add Lombok and the annotation processor path.
- Library Javadoc: `io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault` and `GenerateDto`.


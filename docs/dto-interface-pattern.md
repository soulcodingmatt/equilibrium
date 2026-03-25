# DTOs and interfaces (wrapper pattern)

Sometimes you need generated DTOs to participate in APIs that require interfaces—`Serializable`, `Comparable`, or your own contracts. Project Equilibrium focuses on **structure and mapping** for generated types; it does **not** add arbitrary `implements` clauses or generate meaningful `compareTo` / custom behavior for you.

`@GenerateDto` today exposes `id`, `pkg`, `name`, `ignore`, and `builder` only. There is **no** processor option to list interfaces on the generated class. That is intentional: interface methods are usually **domain decisions**, not something to guess from field names.

## Approach

Use a **thin wrapper** in your project that **contains** the generated DTO and implements the interfaces you care about.

1. Generate a plain DTO with `@GenerateDto` (default name like `BirdDto`, or set `name` explicitly).
2. Add a hand-written class that holds `private final BirdDto dto`, delegates getters (and setters if you need them), and implements `Comparable`, `Serializable`, or other types with **your** rules.

```java
@GenerateDto
public class Bird {
    private String species;
    private boolean canFly;
    private int wingspan;
    // accessors…
}
```

```java
import java.io.Serializable;
import java.util.Objects;

public final class ComparableBird implements Comparable<ComparableBird>, Serializable {

    private static final long serialVersionUID = 1L;
    private final BirdDto dto;

    public ComparableBird(BirdDto dto) {
        this.dto = Objects.requireNonNull(dto);
    }

    public String getSpecies() {
        return dto.getSpecies();
    }

    public int getWingspan() {
        return dto.getWingspan();
    }

    @Override
    public int compareTo(ComparableBird other) {
        int bySpecies = getSpecies().compareTo(other.getSpecies());
        if (bySpecies != 0) {
            return bySpecies;
        }
        return Integer.compare(getWingspan(), other.getWingspan());
    }
}
```

Prefer **composition** (field holding the DTO) over **subclassing** the generated class: generated types are meant to be stable carriers; your wrapper owns the behavioral contract.

## Builders

If you use `@GenerateDto(builder = true)` and Lombok’s builder on the generated DTO, build the inner DTO first, then construct the wrapper:

```java
BirdDto inner = BirdDto.builder().species("Falcon").canFly(true).build();
ComparableBird view = new ComparableBird(inner);
```

## Multiple roles

You can define different wrappers for different concerns (serialization-only, sorting, validation facades) without changing the generated DTO.

## Summary

|     Idea      |                                        Detail                                         |
|---------------|---------------------------------------------------------------------------------------|
| Generated DTO | Data shape, accessors, `equals` / `hashCode` / `toString`, optional Lombok builder    |
| Wrapper       | Implements interfaces, encapsulates behavior, delegates to the DTO                    |
| Why           | Keeps generated code simple and keeps **business rules** in code you control and test |

For custom **fields or methods** on top of a generated DTO, the usual approach is still to **subclass** the generated class in your codebase; that is separate from implementing interfaces. See [Custom fields and methods on generated DTOs](custom-fields-and-methods.md).

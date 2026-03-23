# Custom fields on generated DTOs

Project Equilibrium generates DTOs by **mirroring** fields from your annotated source class (respecting ignores, nested mapping, validation, and so on). It does **not** invent extra fields that do not exist on the source model.

If you need **additional** properties on the DTO side (computed values, UI flags, internal metadata, or anything not modeled on the domain class), you add them in **your own** code. The usual approach is to **subclass** the generated DTO and declare the new fields there. Your subclass lives under your control and is **not** overwritten when the processor regenerates the base type.

## Prerequisites

- A generated DTO from `@GenerateDto` (for example `UserDto`).
- The generated type is a normal **`public class`** with a **public all-args constructor**, getters, setters, and `equals` / `hashCode` / `toString` based only on the generated fields (see `DtoClassWriter` / `DtoGenerator` in this project).

## Basic pattern: subclass and add fields

**Domain (source for generation):**

```java
@GenerateDto
public class User {
    private String name;
    private int age;
    // accessors…
}
```

**Generated (conceptual):** `UserDto` with `name`, `age`, constructor, accessors, `equals`, `hashCode`, `toString`.

**Your hand-written extension:**

```java
import java.util.Objects;

public final class UserDtoWithTrace extends UserDto {

    private String requestId;

    public UserDtoWithTrace(String name, int age, String requestId) {
        super(name, age);
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
```

Regeneration updates `UserDto`; **`UserDtoWithTrace` stays in your source tree** as long as the base constructor signature still matches (if you add fields to the domain, the generated constructor may gain parameters—then update your subclass constructor accordingly).

## `equals` and `hashCode`: what goes wrong

The processor emits `equals` and `hashCode` that only know about **generated** fields. Roughly:

- **`equals`** uses **`getClass() != o.getClass()`**, so an instance of a **subclass** is never equal to an instance of the **base** generated class, even if all inherited fields match.
- **`equals`** and **`hashCode`** do **not** include fields you add on a subclass. If you do not override them, two `UserDtoWithTrace` instances with the same `name` and `age` but different `requestId` can still compare **equal**—which is usually wrong.

So for any subclass with **state beyond** the generated DTO, you should treat `equals` / `hashCode` as **your responsibility**, not inherited behavior.

### Why you cannot rely on `super.equals(o)` for two subclass instances

The generated `UserDto.equals(Object o)` ends with a `getClass()` check against **`UserDto`**. For two operands of type `UserDtoWithTrace`, that check passes on the subclass type, but the inherited method still only compares **fields declared on `UserDto`**. It does **not** see `requestId`. Calling `super.equals(o)` from a well-written subclass `equals` is also subtle: the parent implementation is written for the parent type and will reject or mis-handle mixed types. In practice, **override `equals` and `hashCode` on the subclass** with a contract you define.

### A sound override (illustrative)

Compare **every** field that matters for equality—both those from the parent (via getters) and your extra fields:

```java
@Override
public boolean equals(Object o) {
    if (this == o) {
        return true;
    }
    if (o == null || getClass() != o.getClass()) {
        return false;
    }
    UserDtoWithTrace that = (UserDtoWithTrace) o;
    return Objects.equals(getName(), that.getName())
            && getAge() == that.getAge()
            && Objects.equals(requestId, that.requestId);
}

@Override
public int hashCode() {
    return Objects.hash(getName(), getAge(), requestId);
}
```

**Rule of thumb:** whenever you add fields, **`hashCode` must use the same set of fields as `equals`**, modulo the usual `Objects.hash` / `Objects.equals` patterns.

### If you need equality with the base DTO type

The generated `equals` is **not** designed for “same data, different class” (for example `UserDto` vs `UserDtoWithTrace`). If you need that, options include:

- **Compare manually** using getters on both sides in application code.
- Prefer **composition** (hold a `UserDto` inside another object) when you need a stable value type and clear equality—see [DTOs and interfaces (wrapper pattern)](dto-interface-pattern.md) for the same structural idea applied to interfaces.

### `toString`

The generated `toString` only lists generated fields. Subclasses should **`@Override` `toString`** if logs or debugging should include custom state.

## Builders (`@GenerateDto(builder = true)`)

If the generated DTO uses Lombok **`@SuperBuilder`**, subclassing for extra fields interacts with Lombok’s generated builder hierarchy. That is easy to get wrong. For builder-heavy DTOs, **composition** or a dedicated hand-written type is often simpler than inheritance. See [DTO builder pattern](dto-builder-pattern.md).

## Summary

| Topic | Guidance |
|-------|-----------|
| Where to put extra fields | A **subclass** of the generated DTO (or a **wrapper** if you prefer composition) |
| Regeneration | Base class is regenerated; your subclass remains, but may need constructor updates if the base API changes |
| `equals` / `hashCode` | **Override** on the subclass when you add state; do not assume inherited equality is correct |
| Cross-type equality | Not supported out of the box; compare explicitly or use composition |

## See also

- [DTOs and interfaces (wrapper pattern)](dto-interface-pattern.md) — composition instead of inheritance.
- [DTO builder pattern](dto-builder-pattern.md) — when `builder = true` complicates subclassing.

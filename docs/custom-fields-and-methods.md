# Custom fields and methods on generated DTOs

A generated DTO is just a mirror of your annotated source class (with ignores, nesting, validation, and so on applied): those fields, plus getters/setters, `equals`, `hashCode`, `toString`, and optionally Lombok builder code. **Extra fields and your own methods are not generated**—you add them in code you maintain, almost always by **subclassing** the generated type or using a **wrapper**.

**Why not drop generation and write a “real” DTO class from scratch?** You would be duplicating the model: every new field, removal, type change, nested mapping, validation tweak, or builder default is something you edit twice and try to keep aligned. The usual approach is the opposite—let the processor **refresh the base class** from the annotated source on every compile, and put only your **extras** on a subclass. Your subclass **inherits** that updated API (accessors, constructor, builders, and so on) instead of you retyping the whole type whenever the domain moves. Only the generated file is overwritten; yours stays. You may still adjust `**super(...)`** or `**equals` / `hashCode**` when the base API shifts (covered below)—but you are not maintaining a second full copy of the DTO shape by hand.

Most teams still keep DTOs **thin**: data plus accessors, with heavier logic in services or the domain. When you need a bit more on the type—extra properties or helpers—a subclass (or wrapper) is where that lives.

You need a normal `@GenerateDto` output: a `public class` with a public all-args constructor and the usual generated accessors and `Object` methods (see `DtoClassWriter` / `DtoGenerator` in this repo if you want the exact shape).

## Subclass example

**Source model:**

```java
@GenerateDto
public class User {
    private String name;
    private int age;
    // accessors…
}
```

**Generated:** something like `UserDto` with `name`, `age`, constructor, accessors, `equals`, `hashCode`, `toString`.

**Your extension:**

```java
import java.util.Objects;

public final class UserDtoWithTrace extends UserDto {

    private String requestId;

    public UserDtoWithTrace(String name, int age, String requestId) {
        super(name, age);
        this.requestId = requestId;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
```

After `mvn compile`, `UserDto` is regenerated; `**UserDtoWithTrace` is yours** and stays put. When the domain changes, `**UserDto` reflects it first**; your subclass picks up the new/changed base API and you fix `**super(...)`** (and sometimes `**equals` / `hashCode**`) as needed—see the next sections.

## `equals` and `hashCode`

Generated `equals` / `hashCode` only consider **generated** fields.

- `**equals` uses `getClass()`**, so a subclass instance is not equal to a base `UserDto` with the same values.
- **Subclass-only fields are ignored** by the inherited methods. Two `UserDtoWithTrace` objects with the same `name`/`age` but different `requestId` would still test equal if you don’t override—usually a bug.

**Fix:** override both on the subclass and include every field that matters (parent fields via getters + your fields). Don’t assume `super.equals(o)` is enough: the parent logic still ignores `requestId`.

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
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

Same fields in `equals` and `hashCode`, as usual.

`**UserDto` vs `UserDtoWithTrace`:** the generator does not treat them as interchangeable value types. Compare fields manually in app code, or prefer a **wrapper** holding a plain `UserDto`—see [DTOs and interfaces (wrapper pattern)](dto-interface-pattern.md).

**Methods only:** if you add methods that don’t introduce new state, inherited `equals` / `hashCode` are unchanged. New **fields** mean you override.

## `toString`

Generated `toString` only prints generated fields. Override if you want logs to show subclass state.

## Lombok builders (`@GenerateDto(builder = true)`)

Subclassing gets awkward with `@SuperBuilder` hierarchies. Prefer **composition** or a hand-written type when builders are central—details in [DTO builder pattern](dto-builder-pattern.md).

## Quick reference

|            Topic             |                                                                What to do                                                                 |
|------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| Extra fields / methods       | Subclass (or wrapper)                                                                                                                     |
| What updates automatically   | The **generated base** picks up domain changes (fields, types, mapping, validation, builder defaults, …); your subclass **inherits** that |
| Regeneration                 | Base class only; tweak subclass (`super(...)`, sometimes `equals` / `hashCode`) when the base API changes                                 |
| `equals` / `hashCode`        | Override when the subclass has **extra state**                                                                                            |
| Base vs subclass “same data” | Not built-in; compare explicitly or compose                                                                                               |

## Adding methods to DTOs (optional)

Some codebases keep DTOs as **pure POJOs**; others add small **instance or static methods**—formatting, `isEmpty()`-style checks, tiny factories. That’s a style choice. **We’d still keep DTOs shallow** and avoid real domain logic there, but the generator doesn’t stop you: put those methods on the same **subclass** (or wrapper) as extra fields. Regeneration never touches them.

If a method only uses inherited getters, equality behavior stays as above. If you add **new fields** the method depends on for identity, override `equals` / `hashCode` like any other subclass with extra state.

## See also

- [DTOs and interfaces (wrapper pattern)](dto-interface-pattern.md)
- [DTO builder pattern](dto-builder-pattern.md)


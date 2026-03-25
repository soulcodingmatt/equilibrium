# Custom fields and methods on generated DTOs

A generated DTO contains the fields from your annotated source class (after ignores, nesting, and validation are applied), plus getters/setters, `equals`, `hashCode`, `toString`, and optionally Lombok builder code. That is the whole contract. Extra fields and custom methods are not generated — you add those yourself, almost always by subclassing the generated type or wrapping it.

## Why not just write the DTO by hand?

You could. But then you own two copies of the same field list: the domain class and the DTO. Add a field to one, forget the other, and the bug waits for you in production. The generated base refreshes on every compile; your subclass just inherits whatever changed. You may need to fix a `super(...)` call when the constructor gains a parameter — but you are not transcribing the whole field list every time the domain moves.

Most teams keep DTOs thin — data plus accessors — with real logic in services or the domain layer. When you need a bit more on the type (an extra computed field, a helper method, a trace ID), a subclass is where that lives.

The generated class is a `public class` with a public all-args constructor and the usual `Object` methods. The exact shape is in `DtoClassWriter` / `DtoGenerator` in this repo if you want the details.

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

After `mvn compile`, `UserDto` is regenerated; **`UserDtoWithTrace` is yours** and stays put. When the domain changes, **`UserDto` reflects it first**; your subclass picks up the new base API and you fix `super(...)` (and sometimes `equals` / `hashCode`) as needed — see the next sections.

## `equals` and `hashCode`

Generated `equals` / `hashCode` only consider generated fields.

- **`equals` uses `getClass()`**, so a subclass instance is not equal to a base `UserDto` with the same values.
- **Subclass-only fields are ignored** by the inherited methods. Two `UserDtoWithTrace` objects with the same `name`/`age` but different `requestId` would still test equal if you don't override — usually a bug.

**Fix:** override both on the subclass and include every field that matters (parent fields via getters + your own fields). Don't assume `super.equals(o)` is enough: the parent logic still ignores `requestId`.

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

**`UserDto` vs `UserDtoWithTrace`:** the generator does not treat them as interchangeable value types. Compare fields manually in app code, or prefer a wrapper holding a plain `UserDto` — see [DTOs and interfaces (wrapper pattern)](dto-interface-pattern.md).

**Methods only:** if you add methods that don't introduce new state, the inherited `equals` / `hashCode` are unchanged. New fields mean you override.

## `toString`

Generated `toString` only prints generated fields. Override if you want logs to show subclass state.

## Lombok builders (`@GenerateDto(builder = true)`)

Subclassing gets awkward with `@SuperBuilder` hierarchies. Prefer composition or a hand-written outer type when builders are central — details in [DTO builder pattern](dto-builder-pattern.md).

## Quick reference

|            Topic             |                                                                What to do                                                                 |
|------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| Extra fields / methods       | Subclass (or wrapper)                                                                                                                     |
| What updates automatically   | The generated base picks up domain changes (fields, types, mapping, validation, builder defaults, …); your subclass inherits that        |
| Regeneration                 | Base class only; tweak subclass (`super(...)`, sometimes `equals` / `hashCode`) when the base API changes                                |
| `equals` / `hashCode`        | Override when the subclass has extra state                                                                                                |
| Base vs subclass "same data" | Not built-in; compare explicitly or compose                                                                                               |

## Adding methods to DTOs (optional)

Some codebases keep DTOs as pure POJOs; others add small instance or static methods — formatting, `isEmpty()`-style checks, tiny factories. That is a style choice. Keep DTOs shallow and avoid real domain logic there, but the generator does not stop you: put those methods on the same subclass (or wrapper) as extra fields. Regeneration never touches them.

If a method only uses inherited getters, equality behavior stays as above. If you add new fields the method depends on for identity, override `equals` / `hashCode` like any other subclass with extra state.

## See also

- [DTOs and interfaces (wrapper pattern)](dto-interface-pattern.md)
- [DTO builder pattern](dto-builder-pattern.md)

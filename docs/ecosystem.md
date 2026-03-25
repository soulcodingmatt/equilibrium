# Ecosystem — works well with other tools

Project Equilibrium is not a “do everything” mapping stack. It **generates** DTO-like types from your model. Many teams combine it with other libraries:

## MapStruct

Use [MapStruct](https://mapstruct.org/) `@Mapper` interfaces to map between domain (or API) types and **Project Equilibrium–generated** DTOs, records, or VOs. Generated code follows patterns MapStruct already supports (getters/setters, record components, builders where used, nested paths).

- **Build:** Add the Project Equilibrium processor and `mapstruct-processor` on the annotation processor path, with generated sources on the compile classpath so both processors see each other’s output in one compilation.
- **Mappings:** Align names or use `@Mapping` where they differ. For types built with **builders**, enable MapStruct’s builder support when needed.
- **VOs:** Default VOs are **immutable** (no setters). MapStruct can still map **to** them via the generated **constructor**. Use `@GenerateVo(setters=true)` only if you intentionally want a mutable VO and setter-based mapping.

## When to combine

- **Project Equilibrium** — keep transport/value types in sync with the domain **at compile time**
- **MapStruct (or similar)** — express **field-to-field** mappings between types that already exist

Different modules can standardize on different pieces: for example, generated DTOs in an API module and MapStruct mappers in an adapter layer.

## Related guides

- [DTO builder pattern](dto-builder-pattern.md) when using Lombok `@SuperBuilder` on generated DTOs
- [Custom fields and methods on generated DTOs](custom-fields-and-methods.md) when the DTO needs members or behavior not on the domain type


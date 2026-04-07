# Troubleshooting and common pitfalls

Short catalog of **unhappy paths** people hit with Project Equilibrium—setup, configuration, and usage—and what usually fixes them. For step-by-step Maven and IDE setup, see [Build tooling](build-tooling.md) first.

---

## Build and IDE: “nothing generates” or red squiggles on generated types

**Symptom:** `@GenerateDto` is on your class, compile “succeeds” or the IDE shows errors on `…Dto` types that never appear.

**Typical causes**

- The processor is only a **compile** dependency but **not** on **`annotationProcessorPaths`** (Maven), so `javac` never runs Equilibrium.
- **IntelliJ** is compiling with its own pipeline while generated sources live under `target/generated-sources/…` and are not on the source path the editor uses.

**What to try**

- Confirm **`maven-compiler-plugin`** lists Equilibrium under **`annotationProcessorPaths`** with the same version as the dependency (see [Build tooling](build-tooling.md)).
- Run **`mvn clean compile`** from the command line. If that produces generated types but the IDE does not, enable **Delegate IDE build/run actions to Maven** and/or **Enable annotation processing** as described under *IntelliJ IDEA* in [Build tooling](build-tooling.md).
- After **`pom.xml`** changes, **reload the Maven project** in the IDE.

---

## Duplicate output: two `@Generate*` variants target the same file

**Symptom:** Compiler error about clashing generated paths (same package + simple name for two variants).

**Cause:** Each `@GenerateDto` / `@GenerateRecord` / `@GenerateVo` must resolve to a **unique** output file. Two annotations that end up with the same package and class name will fail.

**What to do**

- Give distinct **`pkg`** and/or **`name`** (or rely on distinct global postfixes) so no two variants write the same path.
- Re-read the rule in [Annotations reference](annotations-reference.md) under *Multiple `@Generate*` annotations on one class*.

---

## Ignore annotations and `ids`: field still appears (or disappears everywhere)

**Symptom:** `@IgnoreDto(ids = { 1 })` does not exclude a field from the DTO you care about, or `@IgnoreDto` without `ids` excludes from every variant when you only wanted one.

**How it works**

- **`ids` omitted or empty** on `@IgnoreDto` / `@IgnoreRecord` / `@IgnoreVo` → ignore applies to **every** generation of that **kind**.
- **`ids` non-empty** → ignore only when the current run’s **`@Generate*` `id`** is in the list.
- **`@Generate*` `id` defaults to `-1`** when unset. `@IgnoreDto(ids = { 1 })` does **not** match a lone `@GenerateDto` with default id; use **`@IgnoreDto`** without `ids`, or put **`-1`** in `ids` if you intentionally target the default-id generation, or set explicit positive ids on `@GenerateDto` and align `ids`.

**`@IgnoreAll`** has **no** `ids`: it always drops the field from **all** DTO, record, and VO outputs. To scope per kind and per variant, use **`@IgnoreDto` / `@IgnoreRecord` / `@IgnoreVo`** with `ids` instead.

Details: [Annotations reference](annotations-reference.md) (*Selective exclusion with `ids`*).

---

## Nested types: wrong DTO per variant or compile error on `@NestedMapping`

**Symptom:** Multiple `@GenerateDto` ids need **different** nested DTO types for the same field; one `@NestedMapping(dtoClass = …)` cannot express that.

**What to do**

- Use **`@NestedDtoMapping`** (repeatable) with **`dtoClassName`** and optional **`ids`** so each DTO id can map to its own nested type. See [Annotations reference](annotations-reference.md).

**Symptom:** Error resolving nested DTO class (unresolved type, bad classpath round).

**What to do**

- Ensure the nested DTO type is visible in the **same compilation** (generated or source). Fix order or module boundaries so the processor can resolve the name.

---

## Lombok builders: processor order or confusing generated code

**Symptom:** `@GenerateDto(builder = true)` but no builder, or Lombok and Equilibrium fight during compile.

**What to do**

- Put **Lombok** on **`annotationProcessorPaths`** (often **before** Equilibrium). Add the Lombok **dependency** as needed.
- Expect **`$`** in **Lombok-generated** builder internals; that is normal for Lombok, not something Equilibrium strips. See [DTO builder pattern](dto-builder-pattern.md) and the Lombok note in [Build tooling](build-tooling.md).

**Symptom:** Subclassing a builder-heavy generated DTO is painful.

**What to do**

- Prefer **composition** or a hand-written outer type; builder hierarchies and inheritance are easy to get wrong. Same guide as above.

---

## MapStruct (or another processor) and Equilibrium together

**Symptom:** One processor runs, the other does not see types, or duplicate-class issues.

**What to do**

- Put **both** processors on **`annotationProcessorPaths`** in an order your toolchain accepts (often **Lombok first**, then others—project-dependent).
- Ensure **generated sources** from the previous round are on the compile classpath for the round that needs them. See [Ecosystem](ecosystem.md).

---

## Subclassing generated DTOs: wrong equality or broken constructor after refactor

**Symptom:** Two instances compare equal when they should not, or subclass no longer compiles after a domain change.

**Causes**

- Generated **`equals` / `hashCode`** only know **generated** fields; subclass fields need **your** overrides.
- Domain changes change the generated **all-args constructor**; **`super(...)`** in your subclass must be updated.

**What to do**

- Follow [Custom fields and methods on generated DTOs](custom-fields-and-methods.md) for `equals`, `hashCode`, and `toString`.

---

## Experimental Jakarta validation

**Symptom:** Constraints missing, compile errors on `@ValidateDto`, or upgrade broke imports.

**What to do**

- Validation lives under **`…experimental.validation`**; APIs may change between releases—treat as **experimental**.
- Invalid constraint combinations are **compile errors** on the **source** type (by design). See [Experimental validation](experimental-validation.md).

---

## Kotlin, Gradle-only, or older Java

**Symptom:** Annotation processing never runs the way you expect.

**Facts**

- Equilibrium targets **Java** sources; **Kotlin** / **`kapt`** is **not** supported or tested here.
- This repo is validated with **Maven**; **Gradle** can work in principle but you must wire the processor path and **`-Aequilibrium.*`** yourself—see [Build tooling](build-tooling.md) (*Installation (Gradle)*).
- **Java 17+** is required for the language level this processor assumes.

---

## GPL and “can we use it at work?”

**Symptom:** Legal uncertainty.

**Short pointer**

- Typical **compile-only** use (processor on the build path, not shipped in your app artifact) is the usual pattern discussed in the [README](../README.md) (*Commercial use (GPL)*). For binding advice, use your own counsel; the README states the project intent in plain language.

---

## Still stuck?

- Re-run **`mvn -X compile`** (or enable compiler **verbose** / **showWarnings**) and read processor messages; with the default banner, Equilibrium also prints a short **summary** (see [Configuration](configuration.md)).
- **Open an issue** on the repository with a **minimal** `pom.xml` snippet and source class if you believe the processor misbehaves.


# Why Project Equilibrium?

Every Java project with a layered architecture eventually has the same conversation: "Who added `middleName` to `User` and forgot to update `UserDto`?" The answer is always "nobody, on purpose" — and the result is a null somewhere at runtime, a confused colleague, or a three-hour debugging session you did not budget for.

The root cause is that DTOs, records, and value objects are written by hand and maintained separately from the domain classes they mirror. They start aligned. They drift. The compiler does not complain because both sides compiled fine — the shapes just no longer match.

Project Equilibrium generates those types at compile time from your annotated domain classes. Add a field to `User`, recompile — the DTO gains it. Remove a field — the DTO loses it. The generated type is never out of sync because it is never written twice.

## What you get

- **No reflection** — generation is pure source code emitted during `javac`, readable and debuggable like anything you wrote yourself
- **No runtime overhead** for creating the type shapes — you still choose how to copy data (constructors, MapStruct, hand-written code)
- **Compile-time failure for bad configuration** — duplicate output paths, invalid validation combinations, bad package names all fail the build, not a user in production
- **Consistent naming by default** — global package and postfix rules, per-annotation overrides, and multiple variants (`id`) from a single source class

## When it fits

- You have a domain model you annotate once and one or more layers (API, persistence, messaging) that need derived types from it
- You want fast, predictable builds with plain generated Java sources you can read
- You are comfortable with annotation-driven configuration and Java 21+

## When to reach for something else

- You need schemas or mappings decided only at runtime — there is no compile-time model to generate from
- Your domain is Kotlin-first — this processor targets Java sources
- You need field-to-field mapping logic — Project Equilibrium generates the *shapes*; pair it with [MapStruct](https://mapstruct.org/) or similar for the data movement (see [Ecosystem](ecosystem.md))

For a minimal first project, see [Quickstart](quickstart.md).

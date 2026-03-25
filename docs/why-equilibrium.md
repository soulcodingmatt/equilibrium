# Why Project Equilibrium?

Keeping **DTOs**, **records**, and **value objects** aligned with domain models is a constant source of boilerplate in Java:

- Verbose hand-written types that drift out of sync with the source model
- Mistakes that surface only at runtime when mapping or serializing
- Refactors that require touching many files

**Project Equilibrium generates those types at compile time** from your annotated domain classes. You stay in one place for field lists and structure; the processor writes the transfer types for you.

## What you get

- **No reflection** — generation is pure source code emitted during compilation
- **No runtime overhead** from a mapping framework for *creating* the DTO shapes themselves (you still choose how to copy data — e.g. constructors, MapStruct, or manual code)
- **Compile-time checks** — bad packages, duplicate outputs, or invalid validation combinations fail the build
- **Consistent naming** — global defaults and per-annotation overrides for packages, postfixes, and multiple variants (`id`) from one class

## When Project Equilibrium fits well

- You want **generated** DTO / record / VO types that **mirror** domain fields with controlled excludes and nesting
- You care about **fast builds** and **predictable** output (plain Java sources)
- You are fine with **annotation-driven** configuration and Java **21+**

## When to pick something else (or add another tool)

- You need **dynamic** schemas or mappings decided only at runtime with no compile-time model
- Your stack is **Kotlin-first** — this processor targets **Java** sources
- You need **heavy runtime mapping** — Project Equilibrium focuses on **generating** types; pairing with [MapStruct](https://mapstruct.org/) or similar is a common next step (see [Ecosystem](ecosystem.md))

For a minimal first project, see [Quickstart](quickstart.md).

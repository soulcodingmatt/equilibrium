# How it works

Project Equilibrium is an **annotation processor** registered on the Java compiler. During compilation it reads your annotated domain classes and **writes new source files** (DTOs, records, value objects) into the generated-sources output directory.

That design implies:

- **Generated code is ordinary Java** — you can read it, diff it, and debug it like hand-written code
- **No reflection** is required for the processor to decide field membership; it uses the same static model the compiler already has
- **Failures are compiler errors** — conflicting configurations, invalid validation combinations, or duplicate output paths surface when you build, not when the app runs
- **Runtime** stays simple: your application works with regular classes. How you **fill** those types (constructors, builders, MapStruct, etc.) is up to you

Optional **compile-time banner** output (version, summary) is controlled with `-Aequilibrium.banner` and related flags — see [Configuration](configuration.md).

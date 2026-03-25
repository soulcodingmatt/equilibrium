# How it works

Project Equilibrium is a standard Java annotation processor — a program that runs inside `javac` during your normal build. It reads your annotated domain classes, decides what to generate, and writes plain `.java` source files into the generated-sources output directory. Those files are compiled as part of the same build pass. From your application's perspective, the generated classes are just classes.

## The compilation lifecycle

1. You run `mvn compile` (or your build triggers a compile).
2. `javac` picks up annotation processors listed on `annotationProcessorPaths`.
3. The processor reads each class annotated with `@GenerateDto`, `@GenerateRecord`, or `@GenerateVo`.
4. For each annotation it finds, it writes a new `.java` source file.
5. `javac` compiles those generated files in the same pass.

No separate Maven goals, no code-generation plugins, no extra tool invocations. Standard annotation processing.

## What the generated code looks like

Generated types are plain Java — no framework annotations, no reflection, nothing unusual. Open `target/generated-sources/annotations/` and you will find readable source files with the exact field list, constructor, accessors, `equals`, `hashCode`, and `toString` that your configuration produces. If something looks wrong, you can read the output directly and reason about it.

Failures are compiler errors, not runtime surprises. A bad package name, a duplicate output path, an invalid validation combination — the build fails with a message pointing at your source class.

## Field collection rules

When deciding which fields appear in a generated type, the processor:

1. Walks the entire class hierarchy up to (but not including) `java.lang.Object`, collecting fields declared on the annotated class first, then appending fields from each superclass in turn.
2. Skips `static` and `transient` fields unconditionally.
3. Applies `@Ignore*` / `@IgnoreAll` rules and any `ignore` list from the `@Generate*` annotation.

Field order matters: the annotated class's own fields come first in the generated constructor, accessors, `equals`, `hashCode`, and `toString`; superclass fields follow.

Optional compile-time banner output (version, summary) is controlled with `-Aequilibrium.banner` and related flags — see [Configuration](configuration.md).

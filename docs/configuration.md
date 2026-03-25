# Configuration

Compiler flags, default precedence, and optional compile-time banner output.

## Compiler options

All options use the `-A` prefix (for example in `compilerArgs`).

### Generated type defaults

|             Option             |                   Purpose                   |
|--------------------------------|---------------------------------------------|
| `-Aequilibrium.dto.package`    | Default package for generated DTOs          |
| `-Aequilibrium.dto.postfix`    | Class name suffix for DTOs (default: `Dto`) |
| `-Aequilibrium.record.package` | Default package for generated records       |
| `-Aequilibrium.record.postfix` | Suffix for records (default: `Record`)      |
| `-Aequilibrium.vo.package`     | Default package for generated VOs           |
| `-Aequilibrium.vo.postfix`     | Suffix for VOs (default: `Vo`)              |

### Project identity (optional)

|           Option           |                      Purpose                       |
|----------------------------|----------------------------------------------------|
| `-Aequilibrium.groupId`    | Maven-style group id used in generated metadata    |
| `-Aequilibrium.artifactId` | Maven-style artifact id used in generated metadata |

If you omit these, the processor attempts to **infer** `groupId` and `artifactId` from a **`pom.xml`** on the compile classpath. If inference also fails, it falls back to `io.github.soulcodingmatt` / `equilibrium`. Explicit `-A` values always take precedence.

### Compile banner and colors

|            Option            |                                                  Purpose                                                  |
|------------------------------|-----------------------------------------------------------------------------------------------------------|
| `-Aequilibrium.banner`       | Set to `false` to disable the build banner and generation summary. If omitted, the banner is **on**.      |
| `-Aequilibrium.banner.color` | Set to `false` for plain-text output (no ANSI escape codes). If omitted, **color is on** where supported. |

## Default precedence

For packages, names, and postfixes, more specific settings win over global ones. In general:

1. **Annotation parameters** on `@GenerateDto` / `@GenerateRecord` / `@GenerateVo` (for example `pkg`, `name`, postfix-related settings where applicable)
2. **Compiler options** (`-Aequilibrium.*`)
3. **Inferred values** from `pom.xml` where the processor supports inference (such as coordinates)
4. **Built-in fallbacks** (for example default suffixes like `Dto`)

Invalid package or postfix values are rejected with compiler errors so misconfiguration fails fast.

## Compile output

When the banner is enabled, compilation prints a **header** (including the processor version) and, when generation or diagnostics ran, a **short summary** of what was processed and whether the run succeeded. With colors enabled, status highlights use ANSI sequences; with `-Aequilibrium.banner.color=false`, output stays plain text. This is informational only and does not change generated code.

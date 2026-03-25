# Project Equilibrium — documentation

Guides in this folder go deeper than the [root README](../README.md). Start there for the overview and quickstart; use these pages when you need full detail.

**Working example project:** [equilibrium-test](https://github.com/soulcodingmatt/equilibrium-test) — clone and run to see the annotations in action.

|                                    Guide                                    |                                        What it covers                                         |
|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| [Why Project Equilibrium?](why-equilibrium.md)                              | Benefits, when to use it, when to pick something else                                         |
| [Quickstart](quickstart.md)                                                 | Minimal Maven setup and first generated DTO                                                   |
| [How it works](how-it-works.md)                                             | Annotation processing, compile-time safety, no reflection                                     |
| [Build tooling](build-tooling.md)                                           | Full Maven compiler snippet, IDE notes, Gradle orientation                                    |
| [Configuration](configuration.md)                                           | `-Aequilibrium.*` options, precedence, compile banner                                         |
| [Annotations reference](annotations-reference.md)                           | `@GenerateDto` / `@GenerateRecord` / `@GenerateVo`, ignores, nesting, experimental validation |
| [Ecosystem](ecosystem.md)                                                   | MapStruct and other complementary tools                                                       |
| [DTO builder pattern](dto-builder-pattern.md)                               | `@GenerateDto(builder=true)`, `@DtoBuilderDefault`, Lombok                                    |
| [Experimental validation](experimental-validation.md)                       | `@ValidateDto` / `@ValidateRecord` / `@ValidateVo`                                            |
| [DTOs and interfaces (wrapper pattern)](dto-interface-pattern.md)           | `Serializable`, `Comparable`, etc.                                                            |
| [Custom fields and methods on generated DTOs](custom-fields-and-methods.md) | Subclassing, helpers, `equals` / `hashCode`                                                   |
| [Troubleshooting and pitfalls](troubleshooting-and-pitfalls.md)             | Common failure paths, IDE/build issues, ignores, nesting, processors, GPL pointer             |


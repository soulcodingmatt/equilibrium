# Project Equilibrium

A Java annotation processor for generating DTOs and other value container classes.

![Project Equilibrium summary output](docs/images/equilibrium-logo-v1.0.0.png)

//TODO: Add short description here - waht is it all about? Benefits? Why use it?


If you find this project useful, consider supporting me ☕  
[![Buy Me a Coffee](https://img.shields.io/badge/-Buy%20me%20a%20coffee-orange?logo=buy-me-a-coffee&logoColor=white)](https://www.buymeacoffee.com/soulcodingmatt)


## Overview

Project Equilibrium is a Java annotation processor that helps you keep domain classes and their **Data Transfer Objects (DTOs)**, **Java records**, and **value objects (VOs)** in sync. It generates and updates those types from your source classes so naming, packages, and structure stay consistent across your project.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.soulcodingmatt/equilibrium.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.soulcodingmatt/equilibrium)


## Table of contents

Short overview here; deeper guides are linked from [Documentation in this repository](#documentation-in-this-repository) (Markdown files under the `docs` directory). Same pattern many open-source projects use: README for orientation and copy-paste setup, separate files for full explanations.

- [Commercial use (GPL)](#commercial-use-gpl)
- [Requirements](#requirements)
- [Features](#features)
- [Consuming vs building this project](#consuming-vs-building-this-project)
- [Documentation in this repository](#documentation-in-this-repository)
- [Getting started (Maven)](#getting-started-maven)
- [Configuration](#configuration)
- [IDE and optional tooling](#ide-and-optional-tooling)
- [Installation (Gradle)](#installation-gradle)
- [Usage](#usage)
- [Using with MapStruct](#using-with-mapstruct)
- [Adding custom fields to generated DTOs](#adding-custom-fields-to-generated-dtos)
- [DTO interface pattern](#dto-interface-pattern)
- [Changelog](#changelog)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)
- [Third-party software](#third-party-software)


## Commercial use (GPL)

You can use this annotation processor in **commercial or closed-source projects** when it runs only at **compile time** and is not shipped inside your artifacts. In that typical setup, **GPL-3.0 does not require you to open-source your own application code**.

## Requirements

- **Java 21** or higher (the processor targets the Java 21 language level)
- A build tool that can **resolve dependencies** from Maven Central (or your repository) and run **annotation processing** (for example Maven 3.x or Gradle 8.x)

Older tool versions may work but are not validated. **Building the Equilibrium library itself** uses Maven in this repository; consuming projects do not need to run this project’s `pom.xml`.

## Features

- Generation of DTOs, records, and VOs with `@GenerateDto`, `@GenerateRecord`, and `@GenerateVo` (including **multiple generations per source class** via `id`, `pkg`, and `name`)
- **Global defaults** via compiler options (`-Aequilibrium.*`), with **per-annotation overrides** where supported
- Field exclusion with `@IgnoreDto`, `@IgnoreRecord`, `@IgnoreVo`, and `@IgnoreAll`, including **selective exclusion** with `ids` when you generate several variants from one class
- **Nested DTO wiring** with `@NestedMapping` (class reference) and **`@NestedDtoMapping`** (string class names, including **per–DTO-id** mappings when you use multiple `@GenerateDto` ids)
- Optional **Lombok `@SuperBuilder`** on generated DTOs (`@GenerateDto(builder=true)`), plus **`@DtoBuilderDefault`** for builder default values where applicable
- Field type preservation (including generics) and **inheritance**: generated types include fields from superclasses
- Optional **experimental** compile-time validation helpers (`@ValidateDto`, `@ValidateRecord`, `@ValidateVo`) that emit Jakarta Bean Validation constraints on generated members
- **Compile-time summary output**: optional banner and statistics (see [Compile output](#compile-output))

## Consuming vs building this project

// TODO:
**In your application:** add the published dependency and configure the compiler. You only need a repository that can download the JAR (Maven, Gradle, or any compatible tool).

// TODO:
**From this repository:** the library is built with **Maven** (including version stamping in the JAR). Reproducing the same artifact with another build tool would require adapting those steps.

## Documentation in this repository

| Guide | What it covers |
|-------|----------------|
| [DTO builder pattern](docs/dto-builder-pattern.md) | `@GenerateDto(builder=true)`, `@DtoBuilderDefault`, Lombok `@SuperBuilder`, defaults |
| [Experimental validation](docs/experimental-validation.md) | `@ValidateDto` / `@ValidateRecord` / `@ValidateVo`, packages, `ids`, `value()` |
| [DTOs and interfaces (wrapper pattern)](docs/dto-interface-pattern.md) | `Serializable`, `Comparable`, etc., without generating `implements` on the DTO |
| [Custom fields on generated DTOs](docs/custom-fields-on-dtos.md) | Subclassing generated DTOs, `equals` / `hashCode` caveats |

## Getting started (Maven)

**1. Add the dependency to your `pom.xml`:**

```xml
<dependency>
    <groupId>io.github.soulcodingmatt</groupId>
    <artifactId>equilibrium</artifactId>
    <version><!-- insert latest version here --></version>
</dependency>
```

**2. Configure the annotation processor in your `pom.xml`:**

```xml
<build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version><!-- insert latest version here --></version>
        <configuration>
          <release><!-- insert your java version number here, e.g. 21 --></release>
          <showWarnings>true</showWarnings>
          <compilerArgs>
            <arg>-Xlint:all</arg>
            <arg>-Aequilibrium.dto.package=com.example.dto</arg>
            <arg>-Aequilibrium.dto.postfix=Dto</arg>
            <arg>-Aequilibrium.record.package=com.example.record</arg>
            <arg>-Aequilibrium.record.postfix=Record</arg>
            <arg>-Aequilibrium.vo.package=com.example.vo</arg>
            <arg>-Aequilibrium.vo.postfix=Vo</arg>
          </compilerArgs>
          <annotationProcessorPaths>
            <path>
                <groupId>io.github.soulcodingmatt</groupId>
                <artifactId>equilibrium</artifactId>
                <version><!-- insert latest version here --></version>
            </path>
          </annotationProcessorPaths>
        </configuration>
      </plugin>
    </plugins>
</build>
```

## Configuration

Compiler flags, default precedence, and optional compile-time banner output.

### Compiler options

All options use the `-A` prefix (for example in `compilerArgs`).

**Generated type defaults**

| Option | Purpose |
|--------|---------|
| `-Aequilibrium.dto.package` | Default package for generated DTOs |
| `-Aequilibrium.dto.postfix` | Class name suffix for DTOs (default: `Dto`) |
| `-Aequilibrium.record.package` | Default package for generated records |
| `-Aequilibrium.record.postfix` | Suffix for records (default: `Record`) |
| `-Aequilibrium.vo.package` | Default package for generated VOs |
| `-Aequilibrium.vo.postfix` | Suffix for VOs (default: `Vo`) |

**Project identity (optional)**

| Option | Purpose |
|--------|---------|
| `-Aequilibrium.groupId` | Maven-style group id used in generated metadata |
| `-Aequilibrium.artifactId` | Maven-style artifact id used in generated metadata |

If you omit these, the processor may **infer** `groupId` and `artifactId` from a **`pom.xml`** on the compile classpath when present. Explicit `-A` values override inferred ones.

**Compile banner and colors**

| Option | Purpose |
|--------|---------|
| `-Aequilibrium.banner` | Set to `false` to disable the build banner and generation summary. If omitted, the banner is **on**. |
| `-Aequilibrium.banner.color` | Set to `false` for plain-text output (no ANSI escape codes). If omitted, **color is on** where supported. |

### Default precedence

For packages, names, and postfixes, more specific settings win over global ones. In general:

1. **Annotation parameters** on `@GenerateDto` / `@GenerateRecord` / `@GenerateVo` (for example `pkg`, `name`, postfix-related settings where applicable)
2. **Compiler options** (`-Aequilibrium.*`)
3. **Inferred values** from `pom.xml` where the processor supports inference (such as coordinates)
4. **Built-in fallbacks** (for example default suffixes like `Dto`)

Invalid package or postfix values are rejected with compiler errors so misconfiguration fails fast.

### Compile output

When the banner is enabled, compilation prints a **header** (including the processor version) and, when generation or diagnostics ran, a **short summary** of what was processed and whether the run succeeded. With colors enabled, status highlights use ANSI sequences; with `-Aequilibrium.banner.color=false`, output stays plain text. This is informational only and does not change generated code.

## IDE and optional tooling

### IntelliJ IDEA (Maven users)

If generated types are missing in the editor when you run or test code that references them, enable **Delegate IDE build/run actions to Maven**:

`File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Maven` → `Runner` → check **Delegate IDE build/run actions to Maven**.

That ties the IDE to the same compile and annotation processing as the command line. The trade-off is that running a simple `main()` may trigger a fuller Maven run. Menu paths can differ slightly in other IDE versions.

### Lombok (builders)

For `@GenerateDto(builder=true)`, add **Lombok** as a dependency and list it on **`annotationProcessorPaths`** before or alongside Equilibrium (order may matter for your setup; Lombok is usually first).

```xml
<project>
<!-- ... --> 
    <dependencies>
        <!-- ... -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version><!-- insert latest version here --></version>
        </dependency>
    </dependencies>
    <!-- ... -->
    
    <build>
        <plugins>    
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version><!-- insert latest version here --></version>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                          <groupId>org.projectlombok</groupId>
                          <artifactId>lombok</artifactId>
                          <version><!-- insert latest version here --></version>
                        </path>
                        <!-- ... -->
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Style note (Lombok builders and `$` in names):** With `@GenerateDto(builder=true)`, Lombok may generate identifiers containing `$` in generated builder plumbing. That follows [Lombok’s conventions](https://projectlombok.org/), not the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) rules for hand-written names. Equilibrium-generated source that this processor writes avoids `$` in its own declarations; symbols with `$` appear in **Lombok-generated** code. To avoid builder internals entirely, use `@GenerateDto` without `builder=true` and rely on constructors and setters.

## Installation (Gradle)

Using Project Equilibrium from **Gradle** is **supported in principle** for **Java** projects: add the same dependency from Maven Central, register the processor on the **annotation processor classpath**, and pass the same **`-Aequilibrium.*`** compiler arguments as in Maven (packages, postfixes, banner options, and so on).

Equilibrium is a **Java** annotation processor: annotated **domain types must be Java** classes. **Kotlin** sources (or Kotlin-first tooling such as `kapt`) are **not** supported or tested here. That is separate from Gradle’s optional **Kotlin DSL** for build scripts (`build.gradle.kts`): you could still author the build in Kotlin DSL while compiling **Java** sources, but we do not document or verify that setup yet.

This repository is built and tested with **Maven**, and we do **not** yet ship a verified Gradle **`build.gradle`** (Groovy DSL) snippet or step-by-step instructions. If you already wire Java annotation processors in Gradle, Equilibrium should follow the same pattern.

**Planned:** a dedicated Gradle section with an example **Java** + Gradle build once it has been exercised and reviewed.

## Usage

### @GenerateDto

Annotate your domain classes with `@GenerateDto`:

```java
@GenerateDto
public class User {
    private String name;
    private int age;
    
    @IgnoreDto
    private String internalId;
    
    // getters and setters
}
```

**Arguments for @GenerateDto**

`id`

- Usage: `@GenerateDto(id=1)` or `@GenerateDto(id=2)`
- Default: unset. When set, identifies this generation so `@IgnoreDto`, `@NestedDtoMapping`, and validation annotations can target **this** DTO variant when several `@GenerateDto` annotations exist on the same class.

`ignore`

- Usage: `@GenerateDto(ignore="fieldname")` or `@GenerateDto(ignore={"field1", "field2"})`
- Default: unset. Excludes the listed fields from this DTO generation (alternative to `@IgnoreDto` on each field).

`pkg`

- Usage: `@GenerateDto(pkg="org.thisisanexample.dto")`
- Default: from compiler options for DTOs (see [Compiler options](#compiler-options)).

`name`

- Usage: `@GenerateDto(name="MyCustomDto")`
- Default: from compiler options; if those are absent, the postfix defaults to `Dto`.

`builder`

- Usage: `@GenerateDto(builder=true)`
- Default: `false`. When `true`, adds Lombok’s `@SuperBuilder` to the generated class. **Lombok must be on the annotation processor path.**

### @DtoBuilderDefault

Use `@DtoBuilderDefault` on domain fields when **`@GenerateDto(builder=true)`** is set. It drives **`@Builder.Default`** on the **generated** DTO: **type-specific parameters** (for example `intValue`, `stringValue`, `enumValue`), **empty defaults** for `List`, `Set`, `Map`, and `Optional` when you use `@DtoBuilderDefault` alone, optional **inheritance** of Lombok **`@Builder.Default`** initializers from the source class (with an **`inherit = false`** opt-out), and a **`value()`** escape hatch for expressions the typed API does not cover (see the annotation Javadoc).

For setup (Lombok), generated shape (`@SuperBuilder`), and practical guidance, see **[DTO builder pattern](docs/dto-builder-pattern.md)**.

### @GenerateRecord

**Arguments for @GenerateRecord**

`id`

- Usage: `@GenerateRecord(id=1)` or `@GenerateRecord(id=2)`
- Default: unset. Used with `@IgnoreRecord` and `@ValidateRecord` to target specific record generations.

`ignore`

- Usage: `@GenerateRecord(ignore="fieldname")` or `@GenerateRecord(ignore={"field1", "field2"})`
- Default: unset. Same idea as for DTOs.

`pkg`

- Usage: `@GenerateRecord(pkg="org.thisisanexample.record")`
- Default: compiler options for records.

`name`

- Usage: `@GenerateRecord(name="MyCustomRecord")`
- Default: compiler options; if absent, postfix defaults to `Record`.

### @GenerateVo

**Arguments for @GenerateVo**

`id`

- Usage: `@GenerateVo(id=1)` or `@GenerateVo(id=2)`
- Default: unset. Used with `@IgnoreVo` and `@ValidateVo` for selective behavior.

`ignore`

- Usage: `@GenerateVo(ignore="fieldname")` or `@GenerateVo(ignore={"field1", "field2"})`
- Default: unset. Value objects often omit identity fields; use ignores accordingly.

`pkg`

- Usage: `@GenerateVo(pkg="org.thisisanexample.vo")`
- Default: compiler options for VOs.

`name`

- Usage: `@GenerateVo(name="MyCustomVo")`
- Default: compiler options; if absent, postfix defaults to `Vo`.

`setters`

- Usage: `@GenerateVo(setters=true)`
- Default: `false`. By default, VOs are **immutable** (`final` fields, no setters), which matches common value-object usage. Mutable transfer types are usually better modeled with **`@GenerateDto`**; `setters=true` is for exceptional cases.

### @IgnoreDto, @IgnoreRecord, @IgnoreVo, @IgnoreAll

`@IgnoreDto`, `@IgnoreRecord`, and `@IgnoreVo` exclude fields from the matching generated type. `@IgnoreAll` excludes a field from **all** generated DTO, record, and VO outputs.

**Selective exclusion with `ids`:**

- `@IgnoreDto(ids={1, 2})` — exclude only from DTO generations with those `@GenerateDto` ids
- `@IgnoreRecord(ids={1, 2})` — same for records
- `@IgnoreVo(ids={1, 2})` — same for VOs  

If `ids` is omitted, the ignore applies to every generation of that kind.

### Multiple `@Generate*` annotations on one class

You can repeat `@GenerateDto`, `@GenerateVo`, and `@GenerateRecord` on the same class to produce several variants (different packages, postfixes, or ids).

**Rule:** each variant must produce a **unique** output path (package + generated simple name). If two configurations resolve to the same file, compilation fails.

**Example:**

```java
@GenerateDto(id=1, pkg="com.example.dto.api", name="UserApiDto")
@GenerateDto(id=2, pkg="com.example.dto.internal", name="UserInternalDto")
@GenerateVo(id=1, pkg="com.example.vo", name="UserVo")
@GenerateVo(id=2, pkg="com.example.vo", name="UserValueObject")
public class User {
    private String name;
    private int age;
    
    @IgnoreDto(ids={1})  // Only excluded from ApiDto, included in InternalDto
    private String internalId;
    
    @IgnoreVo(ids={2})   // Only excluded from UserValueObject, included in UserVo
    private String temporaryField;
}
```

This yields `UserApiDto`, `UserInternalDto`, `UserVo`, and `UserValueObject` in the packages you set.

### @NestedMapping

`@NestedMapping` picks **one** DTO type for a field and applies it to **all** DTO generations that share the same nested shape, using a `Class<?>` reference.

| Parameter  | Type        | Description                                      |
|------------|-------------|--------------------------------------------------|
| `dtoClass` | `Class<?>` | DTO class used for this field in generated DTOs |

Use **at most one `@NestedMapping` per field** (it applies across all generated DTOs for that field).

```java
public class User {
    @NestedMapping(dtoClass = VoiceDto.class)
    private Voice voice;
    
    @NestedMapping(dtoClass = AddressDto.class)
    private Address address;
    
    // Other fields...
}
```

### @NestedDtoMapping

When you have **several `@GenerateDto` ids** and need **different nested DTO types per id**, use **`@NestedDtoMapping`** (repeatable) with **`dtoClassName`** (fully qualified name) and optional **`ids`**. Empty `ids` means the mapping applies to all DTO generations. This avoids limitations around multiple `Class` references on the same field.

### Experimental: `@ValidateDto`, `@ValidateRecord`, `@ValidateVo`

These annotations add **Jakarta Bean Validation** constraints on **generated** DTO, record, or VO members using **type-safe** parameters in `io.github.soulcodingmatt.equilibrium.experimental.validation` (`…validation.dto`, `…validation.record`, `…validation.vo`, shared constraint types in `…validation.common`). The API may change between releases.

**Details and full parameter list:** **[Experimental validation](docs/experimental-validation.md)** (classpath, `ids`, `value()` escape hatch, packages).

**Behavior notes**

- **Validation is analyzed before generation.** Invalid combinations (for example contradictory constraints, `@NotNull` on a primitive, or `@NotBlank` on a non-`String` field) are **compile errors** on your domain class, not silent fixes in generated code.
- **Standard Jakarta annotations** on your sources are recognized in the **same compilation** as Equilibrium, so mixed usage is fine.
- Use the **`ids`** parameter to limit constraints to specific `@GenerateDto` / `@GenerateRecord` / `@GenerateVo` ids; if omitted, validation applies to all generations of that kind.

**Example (`@ValidateDto`)**

```java
import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;

public class User {
    @ValidateDto(
        notNull = @NotNull(message = "Name cannot be null"),
        size = @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    )
    private String name;
    
    @ValidateDto(
        min = @Min(value = 18, message = "Age must be at least 18"),
        max = @Max(value = 120, message = "Age must be at most 120")
    )
    private Integer age;
    
    @ValidateDto(
        email = @Email(message = "Invalid email format"),
        ids = {1, 2}
    )
    private String email;
}
```

Generated types receive the matching Jakarta annotations on their fields or components as appropriate.

## Using with MapStruct

Use [MapStruct](https://mapstruct.org/) `@Mapper` interfaces to map between your domain or API types and Equilibrium-generated DTOs, records, and VOs. Generated code follows patterns MapStruct already supports (getters/setters, record components, builders where used, nested paths).

- **Build:** Add the Equilibrium processor and `mapstruct-processor` on the annotation processor path, with generated sources on the compile classpath so both processors see each other’s output in one compilation.
- **Mappings:** Align names or use `@Mapping` where they differ. For types built with **builders**, enable MapStruct’s builder support when needed.
- **VOs:** Default VOs are **immutable** (no setters). MapStruct can still map **to** them via the generated **constructor**. Use `@GenerateVo(setters=true)` only if you intentionally want a mutable VO and setter-based mapping.

## Adding custom fields to generated DTOs

Equilibrium only generates fields that come from your **annotated source model** (plus mapping and validation rules you attach). Values that exist only on the DTO side must live in **your** code.

The usual approach is to **subclass** the generated DTO, add the extra fields and accessors in that subclass, and adjust constructors when the generated all-args constructor changes. If you add subclass state, you should **override `equals` and `hashCode`** (and often `toString`): the generated methods only consider generated fields and use **`getClass()`** equality, so inheritance has sharp edges.

See **[Custom fields on generated DTOs](docs/custom-fields-on-dtos.md)** for examples, a detailed explanation of `equals` / `hashCode`, and alternatives such as composition or wrappers when inheritance is a poor fit (also related: [DTOs and interfaces (wrapper pattern)](docs/dto-interface-pattern.md)).

## DTO interface pattern

Project Equilibrium generates **plain DTO types** (accessors, `equals` / `hashCode` / `toString`, optional Lombok builders). It does **not** generate `implements` clauses for arbitrary interfaces—things like `Comparable` or `Serializable` need **your** ordering or serialization semantics.

The usual approach is a **small wrapper class** that **holds** the generated DTO and implements the interfaces you need, delegating to the DTO for data. That keeps generated code stable and keeps behavioral contracts in code you own and test.

See **[DTOs and interfaces (wrapper pattern)](docs/dto-interface-pattern.md)** for a short guide, examples, and practices.

## Changelog

See [**CHANGELOG**](CHANGELOG.md).

## Contributing

Contributions are welcome. Please open a Pull Request.

## License

This project is licensed under the GNU General Public License v3.0 — see the [LICENSE](LICENSE) file.

## Contact

For questions or issues, please use the repository’s issue tracker.

## Third-party software

Dependency names, how they are used (for example provided, test, or tooling scopes), and license families are documented in **[NOTICE](NOTICE)**. The published JAR includes the same text as **`META-INF/NOTICE`**. For exact artifact versions, see **`pom.xml`**.

---

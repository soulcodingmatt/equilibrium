# Equilibrium

A Java annotation processor for generating DTOs, transport types, and value container classes.

## Description

Equilibrium is a Java annotation processor that helps you maintain consistency between your domain classes and their corresponding Data Transfer Objects (DTOs), entities, and other value container classes. It automatically generates and updates these classes based on your source classes, preserving type information and reducing boilerplate code.

## Features

- Automatic DTO generation with `@GenerateDto` annotation
- Configurable package names and class postfixes
- Field exclusion with `@IgnoreDto` and `@IgnoreAll` annotations
- Support for different generation modes (strict and safe)
- Field type preservation, including generics

## Installation

1. Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.soulcodingmatt</groupId>
    <artifactId>equilibrium</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

2. Configure the annotation processor in your `pom.xml`:

```xml
<build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration>
          <release>${maven.compiler.release}</release>
          <showWarnings>true</showWarnings>
          <compilerArgs>
            <arg>-Xlint:all</arg>
            <arg>-Aequilibrium.dto.package=com.example.dto</arg>
            <arg>-Aequilibrium.dto.postfix=Dto</arg>
            <arg>-Aequilibrium.strict.mode=false</arg>
          </compilerArgs>
          <annotationProcessorPaths>
            <path>
              <groupId>org.soulcodingmatt</groupId>
              <artifactId>equilibrium</artifactId>
              <version>0.1.0-SNAPSHOT</version>
            </path>
          </annotationProcessorPaths>
        </configuration>
      </plugin>
    </plugins>
</build>
```

## Configuration Options

The following compiler arguments can be configured:

- `-Aequilibrium.dto.package`: Target package for generated DTOs
- `-Aequilibrium.dto.postfix`: Suffix for generated class names (default: "Dto")
- `-Aequilibrium.strict.mode`: Whether to run in strict mode (default: false)

## Usage

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

## Requirements

- Java 21 or higher
- Maven 3.x

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Contact

For any questions or concerns, please open an issue in the repository.

---
**Note:** This project is currently under development.
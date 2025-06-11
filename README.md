# Equilibrium

A Java annotation processor for generating DTOs, transport types, and value container classes.

## Description

Equilibrium is a Java annotation processor that helps you maintain consistency between your domain classes and their corresponding Data Transfer Objects (DTOs), Java Records, and other value container classes. It automatically generates and updates these classes based on your source classes, preserving type information and reducing boilerplate code.

## Features

- Automatic DTO generation with `@GenerateDto`, `@GenerateRecord`, and `@GenerateVo` annotation
- Configurable package names and class postfixes
- Field exclusion with `@IgnoreDto`, `@IgnoreRecord`, `@IgnoreVo`, and `@IgnoreAll` annotations
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
- `-Aequilibrium.record.package`: Target package for generated Java Records
- `-Aequilibrium.record.postfix`: Suffix for generated class names (default: "Record")
- `-Aequilibrium.vo.package`: Target package for generated VOs
- `-Aequilibrium.vo.postfix`: Suffix for generated class names (default: "Vo")

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

## Third Party

This project uses Lombok (https://projectlombok.org), licensed under the MIT License.

---
**Note:** This project is currently under development.

# Project Equilibrium — quickstart (~2 minutes)

## 1. Add the dependency

Use the current version from [Maven Central](https://central.sonatype.com/artifact/io.github.soulcodingmatt/equilibrium).

```xml
<dependency>
    <groupId>io.github.soulcodingmatt</groupId>
    <artifactId>equilibrium</artifactId>
    <version><!-- latest from Central --></version>
</dependency>
```

## 2. Enable the annotation processor (Maven)

Minimal `maven-compiler-plugin` setup:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version><!-- your plugin version --></version>
      <configuration>
        <release>21</release>
        <compilerArgs>
          <arg>-Aequilibrium.dto.package=com.example.dto</arg>
          <arg>-Aequilibrium.dto.postfix=Dto</arg>
        </compilerArgs>
        <annotationProcessorPaths>
          <path>
            <groupId>io.github.soulcodingmatt</groupId>
            <artifactId>equilibrium</artifactId>
            <version><!-- same as dependency --></version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```

Full options (records, VOs, banner flags) are in [Build tooling](build-tooling.md) and [Configuration](configuration.md).

## 3. Annotate a domain class

```java
import io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto;

@GenerateDto
public class User {
    private String name;
    private int age;

    // getters and setters
}
```

## 4. Compile and use the generated type

After `mvn compile`, you get a type such as `com.example.dto.UserDto` (package and postfix follow your `-A` options and any `@GenerateDto` overrides). Instantiate or map it like any other Java class.

**Done** — no hand-maintained DTO stub for that class.

**Want to see it in action?** Clone the [equilibrium-test](https://github.com/soulcodingmatt/equilibrium-test) project for a ready-to-run example that demonstrates the annotations in a real Maven build.

Next: [Annotations reference](annotations-reference.md) for ignores, multiple variants, nesting, and records/VOs.

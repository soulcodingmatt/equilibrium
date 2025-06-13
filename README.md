# Project Equilibrium

A Java annotation processor for generating DTOs and other value container classes.

**Disclaimer:** The project is currently under development.

## Description

Equilibrium is a Java annotation processor that helps you maintain consistency between your domain classes and their corresponding Data Transfer Objects (DTOs), Java Records, and other value container classes. It automatically generates and updates these classes based on your source classes, preserving type information and reducing boilerplate code.

## Features

- Automatic generation of value container classes (DTOs, Records, VOs, ...) with `@GenerateDto`, `@GenerateRecord`, and `@GenerateVo` annotation
- Configurable package names and class postfixes
- Field exclusion with `@IgnoreDto`, `@IgnoreRecord`, `@IgnoreVo`, and `@IgnoreAll` annotations
- Field type preservation, including generics
- Inheritance support: Generated DTOs, Records and VOs inherit all fields from their parent classes

## Installation (Maven)

**1. Add the dependency to your `pom.xml`:**

```xml
<dependency>
    <groupId>org.soulcodingmatt</groupId>
    <artifactId>equilibrium</artifactId>
    <version><!-- insert latest version here --></version>
</dependency>
```
**Note**: Project Equilibrium is currently in an early stage of development. To use it as a dependency, 
you must first clone the repository and build the project locally. After building the JAR file, install 
it into your local build tool's repository — for example, the .m2 directory when using Maven (`mvn install`), 
or via `publishToMavenLocal` when using Gradle, which also places the artifact into `.m2`.

This approach assumes that your Gradle project is configured to resolve dependencies from the local Maven 
repository using `mavenLocal()`. If you're not using `mavenLocal()`, you’ll need to either define a custom local 
Maven repository path or use a flat directory repository and manually place the JAR file there.

**2. Configure the annotation processor in your `pom.xml`:**

```xml
<build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version><!-- insert latest version here --></version>
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
              <version><!-- insert latest version here --></version>
            </path>
          </annotationProcessorPaths>
        </configuration>
      </plugin>
    </plugins>
</build>
```

**3. Adjust the compilation settings of your IDE (for Maven).**

In IntelliJ IDEA (IntelliJ IDEA 2025.1.2) open this menu:
```
> File 
    > Settings 
        > Build, Execution, Deployment 
            > Build Tools 
                > Maven 
                    > Runner
```
There you have to **check** the option "Delegate IDE build/run actions to Maven.
If left unchecked, you will encounter compilation errors when using generated DTOs or other generated
value container classes from a static context, like in the context of the execution **main()** method, when
the IDE starts a pre-compile process.

With the settings described above, you will not encounter the mentioned compilation error. 
However, the downside is that each time you execute your static method, a full Maven build will 
run, which takes more time than without the selected option and also produces more console output.

For the development of the current project, IntelliJ IDEA 2025.1.2 (Ultimate Edition) was used. The settings
might be located elsewhere in older or newer versions of IntelliJ IDEA. Additional configuration details for other IDEs are planned for future releases.

**4. (Optional) Lombok integration**

When making use of features like the builder feature (e.g. `@GenerateDto(builder=true)`), it is mandatory
to add Lombok dependencies to your project. If you use Maven, you have to add the Lombok dependency to
your `pom.xml` file **and** to the `annotationProcessorPaths` of the `maven-compiler-plugin` configuration.

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

## Installation (Gradle)
**TBD**

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
- Maven 3.x **or** Gradle 8.x

**Note**: Earlier versions of Java, Maven or Gradle might also work, but that wasn't tested.

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

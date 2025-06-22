# Project Equilibrium

A Java annotation processor for generating DTOs and other value container classes.

**Disclaimer:** The project is currently under development.

If you find this project useful, consider supporting me ☕  
[![Buy Me a Coffee](https://img.shields.io/badge/-Buy%20me%20a%20coffee-orange?logo=buy-me-a-coffee&logoColor=white)](https://www.buymeacoffee.com/soulcodingmatt)

## Description

Equilibrium is a Java annotation processor that helps you maintain consistency between your domain classes and their corresponding Data Transfer Objects (DTOs), Java Records, and other value container classes. It automatically generates and updates these classes based on your source classes.

## Latest version

[![Maven Central](https://img.shields.io/maven-central/v/io.github.soulcodingmatt/equilibrium.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.soulcodingmatt/equilibrium)


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
            <arg>-Aequilibrium.dto.postfix=Vo</arg>
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

**3. Adjust the compilation settings of your IDE (for Maven).**

In IntelliJ IDEA open this menu:
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

**Note**: For the development of the current project, IntelliJ IDEA 2025.1.2 (Ultimate Edition) was used. The settings
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
- Default: This parameter is **not** set by default. When specified, it assigns a unique integer identifier 
  to this specific @GenerateDto annotation instance. This identifier can then be referenced by @IgnoreDto 
  annotations to selectively exclude fields from specific DTO generations when multiple DTOs are being 
  generated from the same source class.

`ignore`
- Usage: `@GenerateDto(ignore="fieldname")` or `@GenerateDto(ignore={"field1", "field2"})`
- Default: This parameter is **not** set by default. When specified, the listed field(s) will be excluded 
  from the generated DTO. This provides an alternative to using the `@IgnoreDto` field-level annotation.
  You can specify a single field as a string or multiple fields as an array of strings.

`pkg`
- Usage: `@GenerateDto(pkg="org.thisisanexample.dto")`
- Default: Defaults to the compiler arguments for DTOs.

`postfix`
- Usage: `@GenerateDto(postfix="Dto")`
- Default: Defaults to the compiler arguments for DTOs. If the compiler arguments aren't
  set either, the default value is "Dto".

`builder`
- Usage: `@GenerateDto(builder=true)`
- Default: This parameter is set to `false` by default. If set to `true`, Lombok's `@SuperBuilder`
annotation will be added to the generated class, allowing to make use of the builder pattern for 
generated DTOs and customized DTO classes that extend the generated VOs.
- **Note**: For this feature to work, Project Lombok **must be added** to your project.

### @GenerateRecord

**Arguments for @GenerateRecord**

`id`
- Usage: `@GenerateRecord(id=1)` or `@GenerateRecord(id=2)`
- Default: This parameter is **not** set by default. When specified, it assigns a unique integer identifier 
  to this specific @GenerateRecord annotation instance. This identifier can then be referenced by @IgnoreRecord 
  annotations to selectively exclude fields from specific Record generations when multiple Records are being 
  generated from the same source class.

`ignore`
- Usage: `@GenerateRecord(ignore="fieldname")` or `@GenerateRecord(ignore={"field1", "field2"})`
- Default: This parameter is **not** set by default. When specified, the listed field(s) will be excluded 
  from the generated Record. This provides an alternative to using the `@IgnoreRecord` field-level annotation.
  You can specify a single field as a string or multiple fields as an array of strings.

`pkg`
- Usage: `@GenerateRecord(pkg="org.thisisanexample.record")`
- Default: Defaults to the compiler arguments for Java Records.

`postfix`
- Usage: `@GenerateRecord(postfix="Record")`
- Default: Defaults to the compiler arguments for Java Records. If the compiler arguments aren't
  set either, the default value is "Record".


### @GenerateVo
**Arguments for @GenerateVo**
`id`
- Usage: `@GenerateVo(id=1)` or `@GenerateVo(id=2)`
- Default: This parameter is **not** set by default. When specified, it assigns a unique integer identifier 
  to this specific @GenerateVo annotation instance. This identifier can then be referenced by @IgnoreVo 
  annotations to selectively exclude fields from specific VO generations when multiple VOs are being 
  generated from the same source class.

`ignore`
- Usage: `@GenerateVo(ignore="fieldname")` or `@GenerateVo(ignore={"field1", "field2"})`
- Default: This parameter is **not** set by default. When specified, the listed field(s) will be excluded 
  from the generated VO. This provides an alternative to using the `@IgnoreVo` field-level annotation.
  You can specify a single field as a string or multiple fields as an array of strings.
  Value objects typically do not have an identity, in contrast to domain classes.
  Therefore, in most cases, it is desirable to exclude ID fields.


`pkg`
- Usage: `@GenerateVo(pkg="org.thisisanexample.vo")`
- Default: Defaults to the compiler arguments for VOs.

`postfix`
- Usage: `@GenerateVo(postfix="Vo")`
- Default: Defaults to the compiler arguments for VOs. If the compiler arguments aren't
  set either, the default value is "Vo".

`setters`
- Usage: `@GenerateVo(setters=true)`
- Default: This parameter defaults to false. Value objects (VOs) are typically immutable, meaning all 
fields are final and there are no setters. This prevents mutation after creation.
However, if you really need setters for your VO fields (for whatever reason), you can set this parameter 
to `true`.

### @IgnoreDto, @IgnoreRecord, @IgnoreVo, @IgnoreAll
`@IgnoreDto`, `@IgnoreRecord`, and `@IgnoreVo` are field-level annotations that exclude specific fields from 
being included in their respective generated classes (DTO, Record, or Value Object). `@IgnoreAll` is a 
more general field-level annotation that excludes fields from all generated classes, regardless of their 
type. These annotations are particularly useful for excluding internal fields, sensitive data, or fields 
that shouldn't be part of the data transfer or value object representations.

**New `ids` parameter for selective exclusion:**
- `@IgnoreDto(ids={1, 2})`: Excludes the field only from DTO generations with the specified IDs
- `@IgnoreRecord(ids={1, 2})`: Excludes the field only from Record generations with the specified IDs  
- `@IgnoreVo(ids={1, 2})`: Excludes the field only from VO generations with the specified IDs

This allows you to have multiple generations of the same type (e.g., multiple DTOs) from a single source class, 
and selectively exclude fields from specific generations based on their ID. If no `ids` parameter is specified, 
the field will be excluded from all generations of that type.

## Multiple Annotations of the Same Type

It is now possible to use multiple instances of `@GenerateDto`, `@GenerateVo`, and `@GenerateRecord` annotations 
on the same class. This allows you to generate multiple DTOs, VOs, or Records from a single source class with 
different configurations.

**Important requirement:** Each annotation instance must generate a file with a **unique path**. The path is 
comprised of the package name and the class name (original class name + postfix). As long as two annotations 
do not try to create the same filename at the same path, multiple annotations will work correctly.

**Example:**
```java
@GenerateDto(id=1, pkg="com.example.dto.api", postfix="ApiDto")
@GenerateDto(id=2, pkg="com.example.dto.internal", postfix="InternalDto") 
@GenerateVo(id=1, pkg="com.example.vo", postfix="Vo")
@GenerateVo(id=2, pkg="com.example.vo", postfix="ValueObject")
public class User {
    private String name;
    private int age;
    
    @IgnoreDto(ids={1})  // Only excluded from ApiDto, included in InternalDto
    private String internalId;
    
    @IgnoreVo(ids={2})   // Only excluded from UserValueObject, included in UserVo
    private String temporaryField;
}
```

This will generate:
- `com.example.dto.api.UserApiDto`
- `com.example.dto.internal.UserInternalDto`
- `com.example.vo.UserVo`
- `com.example.vo.UserValueObject`

**Note:** The generated file names must be syntactically correct and not yield technical errors. Ensure that 
package names and postfixes follow Java naming conventions.

## Adding custom fields to generated DTOs
**TBD**

**Note**: The intended way to handle custom fields is to extend the generated DTOs and add them in your own subclass. 
This is the short explanation for now—more detailed documentation will follow.

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

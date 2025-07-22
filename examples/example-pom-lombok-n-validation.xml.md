# Example pom.xml with Jakarta Bean Validation and Lombok integration

## About
...

```🚧 Work in Progress 🚧```

...


```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>

    <groupId>YOUR_GROUP_ID_HERE</groupId>
    <artifactId>YOUR_ARTIFACT_ID_HERE</artifactId>
    <version>YOUR_PROJECT_VERSION_HERE</version>

    <properties>
        <equilibrium.version>CURRENT_VERSION_HERE</equilibrium.version>
        <lombok.version>1.18.38</lombok.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>io.github.soulcodingmatt</groupId>
            <artifactId>equilibrium</artifactId>
            <version>${equilibrium.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <scope>provided</scope>
        </dependency>


        <!-- Jakarta Validation API -->
        <!-- Has to be added, if you want to use the Jakarta Bean Validation feature! -->
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
            <version>3.1.1</version>
        </dependency>

        <!-- Hibernate Validator (actual implementation) -->
        <!-- When using Jakarta Bean validation, a suitable implementation has to be added, too. -->
        <!-- The Hibernate Validator is a common implementation and listed here as an example. -->
                <dependency>
                    <groupId>org.hibernate.validator</groupId>
                    <artifactId>hibernate-validator</artifactId>
                    <version>7.0.5.Final</version>
                </dependency>
                <dependency>
                    <groupId>org.glassfish</groupId>
                    <artifactId>jakarta.el</artifactId>
                    <version>4.0.2</version>
                </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.14.0</version>
                <configuration>
                    <release>21</release>
                    <showWarnings>true</showWarnings>
                    <compilerArgs>
                        <arg>-Xlint:all</arg>
                        <arg>-Aequilibrium.dto.package=io.github.soulcodingmatt.dto</arg>
                        <arg>-Aequilibrium.dto.postfix=Dto</arg>
                        <arg>-Aequilibrium.record.package=io.github.soulcodingmatt.record</arg>
                        <arg>-Aequilibrium.record.postfix=Record</arg>
                        <arg>-Aequilibrium.vo.package=io.github.soulcodingmatt.vo</arg>
                        <arg>-Aequilibrium.vo.postfix=Vo</arg>
                    </compilerArgs>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>io.github.soulcodingmatt</groupId>
                            <artifactId>equilibrium</artifactId>
                            <version>${equilibrium.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

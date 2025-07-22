# Example pom.xml with Project Lombok integration

If you want to make use of _Project Equilibrium's_ integration of _Project Lombok's_ ```@SuperBuilder``` you **must**
add the respective dependencies to your project's ```pom.xml```.

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

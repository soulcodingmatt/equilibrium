# Example basic pom.xml

## About
This is the most basic configuration of a ```pom.xml``` for using *Project Equilibrium* in your own software project.
You **won't be able to use all features** with this skeleton of a ```pom.xml```. More advanced features like usage of
_Project Lombok's_ ```@SuperBuilder``` with _Project Equilibrium's_ ```@GenerateDto(builder = true)``` won't be accessible without adding the
needed dependencies to the ```pom.xml``` of your project. That is also true for the ```@ValidateDto``` annotation
which makes use of _Jakarta Bean Validation_ - the respective dependencies **and** a suitable implementation have to
be added to your dependencies. You will find examples for these use cases in the ```/examples``` folder of _Project Equilibrium's_ repository.

## Maven Compiler Plugin
For _Project Equilibrium_ to work, you have to use and configure the _Maven Compiler Plugin_ (```maven-compiler-plugin```).
_Project Equilibrium_ has to be set in the ```<annotationProcessorPaths>``` section.

### Compiler Arguments
There are six optional compiler arguments for _Project Equilibrium_ that can
be added to the ```<compilerArgs>``` section of the _Maven Compiler Plugin_:

Define the default package and default psotfix for generated DTO files:
- ```<arg>-Aequilibrium.dto.package=com.soulcodingmatt.dto</arg>```
- ```<arg>-Aequilibrium.dto.postfix=Dto</arg>```

Define the default package and default psotfix for generated Record files:
- ```<arg>-Aequilibrium.record.package=com.soulcodingmatt.record</arg>```
- ```<arg>-Aequilibrium.record.postfix=Record</arg>```

Define the default package and default psotfix for generated VO files:
- ```<arg>-Aequilibrium.vo.package=com.soulcodingmatt.vo</arg>```
- ```<arg>-Aequilibrium.vo.postfix=Vo</arg>```

If these defaults aren't set and no packages are defined in the respective annotations
for value container class generation, a fallback logic falls in place that
creates a default package name consisting of the groupId and artifactId of your project,
and creates a default file name consisting of the name of the base class and the
respective file type, that means ```Dto```, ```Record```, or ```Vo``` are
postfixed to the original file name by default.
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
                        <arg>-Aequilibrium.dto.package=com.soulcodingmatt.dto</arg>
                        <arg>-Aequilibrium.dto.postfix=Dto</arg>
                        <arg>-Aequilibrium.record.package=com.soulcodingmatt.record</arg>
                        <arg>-Aequilibrium.record.postfix=Record</arg>
                        <arg>-Aequilibrium.vo.package=com.soulcodingmatt.vo</arg>
                        <arg>-Aequilibrium.vo.postfix=Vo</arg>
                    </compilerArgs>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>io.github.soulcodingmatt</groupId>
                            <artifactId>equilibrium</artifactId>
                            <version>${equilibrium.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

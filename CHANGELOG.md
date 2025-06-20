# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Preparing for stable 0.2.0 release

## [0.2.0-RC1] - 2025-06-20

### Added
- Support for multiple annotations of the same type with different parameters
- Selective field ignoring by ID for `@GenerateDto`, `@GenerateRecord`, and `@GenerateVo`
- Warning system when ignore ID feature is used with unassigned ID fields
- Enhanced error messaging and validation

### Changed
- Made method overrides standard for DTOs and VOs (removed overrides parameters)
- Improved package path fallback mechanism
- Refactored codebase to address SonarQube warnings for better code quality

### Fixed
- Package path resolution now returns current project's package path as fallback

### Technical Improvements
- Enhanced test coverage with additional test cases
- Code quality improvements addressing static analysis warnings
- Better error messaging for developer experience
- Removed unused imports for cleaner codebase

### Features
- **DTO Generation**: Automatic generation of Data Transfer Objects with field preservation and type safety
- **Record Generation**: Java Record creation with configurable packages and postfixes
- **Value Object Generation**: VO generation with advanced configuration options
- **Type Preservation**: Maintains field types including generics
- **Configuration Flexibility**: Multiple compiler arguments for customization:
  - `-Aequilibrium.dto.package`: Target package for DTOs
  - `-Aequilibrium.dto.postfix`: Suffix for DTO class names
  - `-Aequilibrium.record.package`: Target package for Records
  - `-Aequilibrium.record.postfix`: Suffix for Record class names
  - `-Aequilibrium.vo.package`: Target package for VOs
  - `-Aequilibrium.vo.postfix`: Suffix for VO class names

### Technical Details
- **Minimum Java Version**: Java 21
- **Build Tool**: Maven 3.x
- **Dependencies**: 
  - Google Auto Service 1.1.1
  - Google Guava 33.4.8-jre
  - JUnit Jupiter 5.13.1 (testing)
- **License**: GNU General Public License v3.0

### Documentation
- Comprehensive README with installation and usage examples
- Maven Central deployment configuration
- IDE setup instructions for IntelliJ IDEA
- Lombok integration guidelines

### Notes
- This release candidate includes significant enhancements to the multiple annotation feature
- All major features from 0.1.0-RC1 remain stable and are enhanced
- Please test the new multiple annotation capabilities and provide feedback

## Project Information

- **Repository**: https://github.com/soulcodingmatt/equilibrium
- **Author**: Matthias Lange (matt@soulcodingmatt.com)
- **License**: [GPL-3.0](https://www.gnu.org/licenses/gpl-3.0.html)
- **Maven Central**: Available as `io.github.soulcodingmatt:equilibrium`

---

**Note**: This project is currently under active development. Features and APIs may change before the first stable release.

## [0.1.0-RC1] - Previous Release

### Added
- Initial support for Java 21
- Basic annotation processing with `@GenerateDto`, `@GenerateRecord`, and `@GenerateVo`
- Configurable package names and class postfixes via compiler arguments
- Field exclusion capabilities with `@IgnoreDto`, `@IgnoreRecord`, `@IgnoreVo`, and `@IgnoreAll` annotations
- Inheritance support for generated classes
- Optional Lombok integration with builder pattern support (`@SuperBuilder`)
- Comprehensive Maven configuration with annotation processor paths
- IDE integration support (IntelliJ IDEA configuration documented)
- Value Object generation with customizable features

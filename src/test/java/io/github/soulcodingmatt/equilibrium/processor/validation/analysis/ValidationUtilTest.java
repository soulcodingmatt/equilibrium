package io.github.soulcodingmatt.equilibrium.processor.validation.analysis;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidationUtilTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "com.example",
        "com.example123",
        "Com.Example", // uppercase allowed
        "com.example-package", // hyphen allowed (note: "example-package" is not a keyword)
        "com.example_package", // underscore allowed
        "com.example-123", // hyphen allowed
        "com.example_123", // underscore allowed
        "com.example-test.sub-test", // multiple hyphens
        "com.example_test.sub_test" // multiple underscores
      })
  void testIsValidPackageName_Valid(String packageName) {
    assertTrue(ValidationUtil.isValidPackageName(packageName));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "123com.example", // cannot start with number
        "com.123example", // package part cannot start with number
        "com.example.123package", // package part cannot start with number
        "com.123.example", // package part cannot start with number
        "com.123.456", // multiple package parts cannot start with number
        "com..example", // double dot
        "com.example.", // trailing dot
        ".com.example", // leading dot
        "com.example/package", // slash not allowed
        "com.example package", // space not allowed
        "com.example.package", // Java keyword "package"
        "com.example.package.subpackage", // Java keyword "package"
        "com.Example.Package", // Java keyword "package" (case insensitive)
        "com.example123.package", // Java keyword "package"
        "com.new.example", // Java keyword "new"
        "com.example.new", // Java keyword "new"
        "com.class.example", // Java keyword "class"
        "com.interface.example" // Java keyword "interface"
      })
  @NullAndEmptySource
  void testIsValidPackageName_Invalid(String packageName) {
    assertFalse(ValidationUtil.isValidPackageName(packageName));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Dto",
        "Record",
        "VO",
        "Dto123",
        "Record456",
        "VO789",
        "dto", // lowercase allowed
        "record", // lowercase allowed
        "vo", // lowercase allowed
        "123Dto" // numbers allowed at start
      })
  void testIsValidPostfix_Valid(String postfix) {
    assertTrue(ValidationUtil.isValidPostfix(postfix));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Dto-123", // hyphen not allowed
        "Record_456", // underscore not allowed
        "VO 789", // space not allowed
        "Dto/123" // slash not allowed
      })
  @NullAndEmptySource
  void testIsValidPostfix_Invalid(String postfix) {
    assertFalse(ValidationUtil.isValidPostfix(postfix));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "field",
        "fieldName",
        "field123",
        "fieldName456",
        "Field", // uppercase allowed
        "FieldName", // uppercase allowed
        "123field" // numbers allowed at start
      })
  void testIsValidFieldName_Valid(String fieldName) {
    assertTrue(ValidationUtil.isValidFieldName(fieldName));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "field-name", // hyphen not allowed
        "field_name", // underscore not allowed
        "field name", // space not allowed
        "field/name" // slash not allowed
      })
  @NullAndEmptySource
  void testIsValidFieldName_Invalid(String fieldName) {
    assertFalse(ValidationUtil.isValidFieldName(fieldName));
  }

  @Test
  void testGetDefaultPackageName() {
    // Default package names should follow Java conventions (lowercase)
    assertEquals(
        "com.example.project.dto",
        ValidationUtil.getDefaultPackageName("com.example", "project", "dto"));
    assertEquals(
        "com.example.project.record",
        ValidationUtil.getDefaultPackageName("com.example", "project", "record"));
    assertEquals(
        "com.example.project.vo",
        ValidationUtil.getDefaultPackageName("com.example", "project", "vo"));

    // Should convert to lowercase even if input is mixed case
    assertEquals(
        "com.example.project.dto",
        ValidationUtil.getDefaultPackageName("Com.Example", "Project", "DTO"));
  }

  @Test
  void testGetDefaultPostfix() {
    // Default postfixes should follow Java conventions
    assertEquals("Dto", ValidationUtil.getDefaultPostfix("DTO"));
    assertEquals("Record", ValidationUtil.getDefaultPostfix("Record"));
    assertEquals("Vo", ValidationUtil.getDefaultPostfix("VO"));
  }

  @Test
  void testGetDefaultPostfix_InvalidClassType() {
    assertThrows(IllegalArgumentException.class, () -> ValidationUtil.getDefaultPostfix("Invalid"));
  }

  @Test
  void testPackageNameValidationRules_validAndKeywordSuffixes() {
    assertTrue(ValidationUtil.isValidPackageName("com.example"));
    assertTrue(ValidationUtil.isValidPackageName("com.example123"));
    assertTrue(ValidationUtil.isValidPackageName("Com.Example"));
    assertTrue(ValidationUtil.isValidPackageName("com.example-test"));
    assertTrue(ValidationUtil.isValidPackageName("com.example_test"));
    assertTrue(ValidationUtil.isValidPackageName("com.example-123"));
    assertTrue(ValidationUtil.isValidPackageName("com.example_123"));
    assertTrue(ValidationUtil.isValidPackageName("com.example-test.sub-test"));
    assertTrue(ValidationUtil.isValidPackageName("com.example_test.sub_test"));

    assertTrue(ValidationUtil.isValidPackageName("com.example.package123"));
    assertTrue(ValidationUtil.isValidPackageName("com.example.package_test"));
    assertTrue(ValidationUtil.isValidPackageName("com.example.package-test"));
  }

  @Test
  void testPackageNameValidationRules_invalidSyntaxAndReservedParts() {
    assertFalse(ValidationUtil.isValidPackageName("123com.example"));
    assertFalse(ValidationUtil.isValidPackageName("com.123example"));
    assertFalse(ValidationUtil.isValidPackageName("com.example.123test"));
    assertFalse(ValidationUtil.isValidPackageName("com.123.example"));
    assertFalse(ValidationUtil.isValidPackageName("com.123.456"));
    assertFalse(ValidationUtil.isValidPackageName("com..example"));
    assertFalse(ValidationUtil.isValidPackageName("com.example."));
    assertFalse(ValidationUtil.isValidPackageName(".com.example"));
    assertFalse(ValidationUtil.isValidPackageName("com.example/test"));
    assertFalse(ValidationUtil.isValidPackageName("com.example test"));
    assertFalse(ValidationUtil.isValidPackageName(null));
    assertFalse(ValidationUtil.isValidPackageName(""));

    assertFalse(ValidationUtil.isValidPackageName("com.example.package"));
    assertFalse(ValidationUtil.isValidPackageName("com.example.new"));
    assertFalse(ValidationUtil.isValidPackageName("com.class.example"));
    assertFalse(ValidationUtil.isValidPackageName("com.interface.example"));
    assertFalse(ValidationUtil.isValidPackageName("com.Example.Package"));
  }

  @Test
  void testGetPackageValidationError_NullAndEmpty() {
    // Null package name
    assertEquals(
        "Package name cannot be null or empty", ValidationUtil.getPackageValidationError(null));

    // Empty package name
    assertEquals(
        "Package name cannot be null or empty", ValidationUtil.getPackageValidationError(""));
  }

  @Test
  void testGetPackageValidationError_LeadingTrailingDots() {
    // Leading dot
    assertEquals(
        "Package name '.com.example' cannot start or end with a dot",
        ValidationUtil.getPackageValidationError(".com.example"));

    // Trailing dot
    assertEquals(
        "Package name 'com.example.' cannot start or end with a dot",
        ValidationUtil.getPackageValidationError("com.example."));

    // Both leading and trailing dots
    assertEquals(
        "Package name '.com.example.' cannot start or end with a dot",
        ValidationUtil.getPackageValidationError(".com.example."));
  }

  @Test
  void testGetPackageValidationError_ConsecutiveDots() {
    // Double dots
    assertEquals(
        "Package name 'com..example' cannot contain consecutive dots",
        ValidationUtil.getPackageValidationError("com..example"));

    // Triple dots
    assertEquals(
        "Package name 'com...example' cannot contain consecutive dots",
        ValidationUtil.getPackageValidationError("com...example"));

    // Multiple consecutive dots in different positions
    assertEquals(
        "Package name 'com..example..test' cannot contain consecutive dots",
        ValidationUtil.getPackageValidationError("com..example..test"));
  }

  @Test
  void testGetPackageValidationError_InvalidCharacters() {
    // Space
    String error = ValidationUtil.getPackageValidationError("com.example test");
    assertNotNull(error);
    assertTrue(error.contains("invalid part"));
    assertTrue(error.contains("example test"));

    // Slash
    error = ValidationUtil.getPackageValidationError("com.example/test");
    assertNotNull(error);
    assertTrue(error.contains("invalid part"));
    assertTrue(error.contains("example/test"));

    // Special characters
    error = ValidationUtil.getPackageValidationError("com.example@test");
    assertNotNull(error);
    assertTrue(error.contains("invalid part"));

    error = ValidationUtil.getPackageValidationError("com.example#test");
    assertNotNull(error);
    assertTrue(error.contains("invalid part"));

    error = ValidationUtil.getPackageValidationError("com.example$test");
    assertNotNull(error);
    assertTrue(error.contains("invalid part"));
  }

  @Test
  void testGetPackageValidationError_StartsWithNumber() {
    // Package starts with number
    String error = ValidationUtil.getPackageValidationError("123com.example");
    assertNotNull(error);
    assertTrue(error.contains("invalid part"));
    assertTrue(error.contains("123com"));

    // Package part starts with number
    error = ValidationUtil.getPackageValidationError("com.123example");
    assertNotNull(error);
    assertTrue(error.contains("invalid part"));
    assertTrue(error.contains("123example"));

    // Multiple parts starting with numbers
    error = ValidationUtil.getPackageValidationError("com.123.456");
    assertNotNull(error);
    assertTrue(error.contains("invalid part"));
  }

  @Test
  void testGetPackageValidationError_ReservedKeywords() {
    // Common Java keywords
    String error = ValidationUtil.getPackageValidationError("com.example.package");
    assertNotNull(error);
    assertTrue(error.contains("reserved keyword"));
    assertTrue(error.contains("package"));

    error = ValidationUtil.getPackageValidationError("com.example.new");
    assertNotNull(error);
    assertTrue(error.contains("reserved keyword"));
    assertTrue(error.contains("new"));

    error = ValidationUtil.getPackageValidationError("com.class.example");
    assertNotNull(error);
    assertTrue(error.contains("reserved keyword"));
    assertTrue(error.contains("class"));

    error = ValidationUtil.getPackageValidationError("com.interface.example");
    assertNotNull(error);
    assertTrue(error.contains("reserved keyword"));
    assertTrue(error.contains("interface"));

    // Case insensitive keyword check
    error = ValidationUtil.getPackageValidationError("com.Example.Package");
    assertNotNull(error);
    assertTrue(error.contains("reserved keyword"));
    assertTrue(error.contains("Package"));

    // More keywords
    error = ValidationUtil.getPackageValidationError("com.abstract.example");
    assertNotNull(error);
    assertTrue(error.contains("reserved keyword"));

    error = ValidationUtil.getPackageValidationError("com.example.static");
    assertNotNull(error);
    assertTrue(error.contains("reserved keyword"));

    error = ValidationUtil.getPackageValidationError("com.void.example");
    assertNotNull(error);
    assertTrue(error.contains("reserved keyword"));
  }

  @Test
  void testGetPackageValidationError_ValidPackages() {
    // Valid packages should return null
    assertNull(ValidationUtil.getPackageValidationError("com.example"));
    assertNull(ValidationUtil.getPackageValidationError("com.example123"));
    assertNull(ValidationUtil.getPackageValidationError("Com.Example"));
    assertNull(ValidationUtil.getPackageValidationError("com.example-test"));
    assertNull(ValidationUtil.getPackageValidationError("com.example_test"));
    assertNull(ValidationUtil.getPackageValidationError("com.example-123"));
    assertNull(ValidationUtil.getPackageValidationError("com.example_123"));

    // Keywords with additional characters are valid
    assertNull(ValidationUtil.getPackageValidationError("com.example.package123"));
    assertNull(ValidationUtil.getPackageValidationError("com.example.package_test"));
    assertNull(ValidationUtil.getPackageValidationError("com.example.package-test"));
  }

  @Test
  void testPostfixEdgeCases() {
    // Empty string
    assertFalse(ValidationUtil.isValidPostfix(""));

    // Null
    assertFalse(ValidationUtil.isValidPostfix(null));

    // Single character
    assertTrue(ValidationUtil.isValidPostfix("D"));
    assertTrue(ValidationUtil.isValidPostfix("1"));

    // Only numbers
    assertTrue(ValidationUtil.isValidPostfix("123"));

    // Mixed case
    assertTrue(ValidationUtil.isValidPostfix("DtO"));
    assertTrue(ValidationUtil.isValidPostfix("dTO"));

    // Invalid characters
    assertFalse(ValidationUtil.isValidPostfix("Dto-"));
    assertFalse(ValidationUtil.isValidPostfix("Dto_"));
    assertFalse(ValidationUtil.isValidPostfix("Dto."));
    assertFalse(ValidationUtil.isValidPostfix("Dto "));
    assertFalse(ValidationUtil.isValidPostfix(" Dto"));
    assertFalse(ValidationUtil.isValidPostfix("Dt o"));
    assertFalse(ValidationUtil.isValidPostfix("Dto@"));
    assertFalse(ValidationUtil.isValidPostfix("Dto#"));
    assertFalse(ValidationUtil.isValidPostfix("Dto$"));
  }

  @Test
  void testFieldNameEdgeCases() {
    // Empty string
    assertFalse(ValidationUtil.isValidFieldName(""));

    // Null
    assertFalse(ValidationUtil.isValidFieldName(null));

    // Single character
    assertTrue(ValidationUtil.isValidFieldName("f"));
    assertTrue(ValidationUtil.isValidFieldName("1"));

    // Only numbers
    assertTrue(ValidationUtil.isValidFieldName("123"));

    // Mixed case
    assertTrue(ValidationUtil.isValidFieldName("fieldName"));
    assertTrue(ValidationUtil.isValidFieldName("FieldName"));
    assertTrue(ValidationUtil.isValidFieldName("FIELDNAME"));

    // Invalid characters
    assertFalse(ValidationUtil.isValidFieldName("field-name"));
    assertFalse(ValidationUtil.isValidFieldName("field_name"));
    assertFalse(ValidationUtil.isValidFieldName("field.name"));
    assertFalse(ValidationUtil.isValidFieldName("field name"));
    assertFalse(ValidationUtil.isValidFieldName(" field"));
    assertFalse(ValidationUtil.isValidFieldName("field "));
    assertFalse(ValidationUtil.isValidFieldName("fie ld"));
    assertFalse(ValidationUtil.isValidFieldName("field@"));
    assertFalse(ValidationUtil.isValidFieldName("field#"));
    assertFalse(ValidationUtil.isValidFieldName("field$"));
  }

  @Test
  void testPackageNameWithAllReservedKeywords() {
    // Test a comprehensive set of Java reserved keywords
    String[] keywords = {
      "abstract",
      "assert",
      "boolean",
      "break",
      "byte",
      "case",
      "catch",
      "char",
      "class",
      "const",
      "continue",
      "default",
      "do",
      "double",
      "else",
      "enum",
      "extends",
      "final",
      "finally",
      "float",
      "for",
      "goto",
      "if",
      "implements",
      "import",
      "instanceof",
      "int",
      "interface",
      "long",
      "native",
      "new",
      "package",
      "private",
      "protected",
      "public",
      "return",
      "short",
      "static",
      "strictfp",
      "super",
      "switch",
      "synchronized",
      "this",
      "throw",
      "throws",
      "transient",
      "try",
      "void",
      "volatile",
      "while"
    };

    for (String keyword : keywords) {
      // Test keyword as package part
      assertFalse(
          ValidationUtil.isValidPackageName("com." + keyword),
          "Package name with keyword '" + keyword + "' should be invalid");

      // Test keyword with different case
      assertFalse(
          ValidationUtil.isValidPackageName("com." + keyword.toUpperCase()),
          "Package name with uppercase keyword '" + keyword.toUpperCase() + "' should be invalid");

      // Test keyword with mixed case
      if (keyword.length() > 1) {
        String mixedCase = keyword.substring(0, 1).toUpperCase() + keyword.substring(1);
        assertFalse(
            ValidationUtil.isValidPackageName("com." + mixedCase),
            "Package name with mixed case keyword '" + mixedCase + "' should be invalid");
      }
    }
  }

  @Test
  void testPackageNameComplexEdgeCases() {
    // Multiple consecutive dots
    assertFalse(ValidationUtil.isValidPackageName("com....example"));

    // Only dots
    assertFalse(ValidationUtil.isValidPackageName("..."));
    assertFalse(ValidationUtil.isValidPackageName("."));

    // Single part package (valid)
    assertTrue(ValidationUtil.isValidPackageName("com"));

    // Very long package name (valid if all parts are valid) - but "package" is a keyword
    assertFalse(
        ValidationUtil.isValidPackageName("com.example.very.long.package.name.with.many.parts"));

    // Very long package name without keywords - "long" is also a keyword, so avoid it
    assertTrue(ValidationUtil.isValidPackageName("com.example.very.big.pkg.name.with.many.parts"));

    // Package with numbers in valid positions
    assertTrue(ValidationUtil.isValidPackageName("com.example2.test3"));
    assertTrue(ValidationUtil.isValidPackageName("a1.b2.c3"));

    // Package with hyphens and underscores mixed
    assertTrue(ValidationUtil.isValidPackageName("com.example-test_package"));
    assertTrue(ValidationUtil.isValidPackageName("com.example_test-package"));
  }

  @Test
  void testUtilityClassCannotBeInstantiated() {
    InvocationTargetException ex =
        assertThrows(
            InvocationTargetException.class,
            () -> {
              var constructor = ValidationUtil.class.getDeclaredConstructor();
              constructor.setAccessible(true);
              constructor.newInstance();
            });
    assertTrue(ex.getCause() instanceof UnsupportedOperationException);
    assertEquals("This is a utility class and cannot be instantiated", ex.getCause().getMessage());
  }
}

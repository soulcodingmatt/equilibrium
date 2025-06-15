package io.github.soulcodingmatt.equilibrium.processor.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "com.example",
        "com.example.package",
        "com.example.package.subpackage",
        "com.example123",
        "com.example.package123",
        "Com.Example",  // uppercase allowed
        "com.Example.Package",  // uppercase allowed
        "com.example123.package",  // numbers allowed after first letter
        "com.example-package",  // hyphen allowed
        "com.example_package",  // underscore allowed
        "com.example.package-123",  // hyphen allowed
        "com.example.package_123",  // underscore allowed
        "com.example-package.sub-package",  // multiple hyphens
        "com.example_package.sub_package"  // multiple underscores
    })
    void testIsValidPackageName_Valid(String packageName) {
        assertTrue(ValidationUtil.isValidPackageName(packageName));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123com.example",  // cannot start with number
        "com.123example",  // package part cannot start with number
        "com.example.123package",  // package part cannot start with number
        "com.123.example",  // package part cannot start with number
        "com.123.456",  // multiple package parts cannot start with number
        "com..example",  // double dot
        "com.example.",  // trailing dot
        ".com.example",  // leading dot
        "com.example/package",  // slash not allowed
        "com.example package"  // space not allowed
    })
    @NullAndEmptySource
    void testIsValidPackageName_Invalid(String packageName) {
        assertFalse(ValidationUtil.isValidPackageName(packageName));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Dto",
        "Record",
        "VO",
        "Dto123",
        "Record456",
        "VO789",
        "dto",  // lowercase allowed
        "record",  // lowercase allowed
        "vo",  // lowercase allowed
        "123Dto"  // numbers allowed at start
    })
    void testIsValidPostfix_Valid(String postfix) {
        assertTrue(ValidationUtil.isValidPostfix(postfix));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Dto-123",  // hyphen not allowed
        "Record_456",  // underscore not allowed
        "VO 789",  // space not allowed
        "Dto/123"  // slash not allowed
    })
    @NullAndEmptySource
    void testIsValidPostfix_Invalid(String postfix) {
        assertFalse(ValidationUtil.isValidPostfix(postfix));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "field",
        "fieldName",
        "field123",
        "fieldName456",
        "Field",  // uppercase allowed
        "FieldName",  // uppercase allowed
        "123field"  // numbers allowed at start
    })
    void testIsValidFieldName_Valid(String fieldName) {
        assertTrue(ValidationUtil.isValidFieldName(fieldName));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "field-name",  // hyphen not allowed
        "field_name",  // underscore not allowed
        "field name",  // space not allowed
        "field/name"  // slash not allowed
    })
    @NullAndEmptySource
    void testIsValidFieldName_Invalid(String fieldName) {
        assertFalse(ValidationUtil.isValidFieldName(fieldName));
    }

    @Test
    void testGetDefaultPackageName() {
        // Default package names should follow Java conventions (lowercase)
        assertEquals("com.example.project.dto", 
            ValidationUtil.getDefaultPackageName("com.example", "project", "dto"));
        assertEquals("com.example.project.record", 
            ValidationUtil.getDefaultPackageName("com.example", "project", "record"));
        assertEquals("com.example.project.vo", 
            ValidationUtil.getDefaultPackageName("com.example", "project", "vo"));
        
        // Should convert to lowercase even if input is mixed case
        assertEquals("com.example.project.dto", 
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
        assertThrows(IllegalArgumentException.class, 
            () -> ValidationUtil.getDefaultPostfix("Invalid"));
    }

    @Test
    void testPackageNameValidationRules() {
        // Valid cases
        assertTrue(ValidationUtil.isValidPackageName("com.example"));
        assertTrue(ValidationUtil.isValidPackageName("com.example.package"));
        assertTrue(ValidationUtil.isValidPackageName("com.example123"));
        assertTrue(ValidationUtil.isValidPackageName("com.example.package123"));
        assertTrue(ValidationUtil.isValidPackageName("Com.Example"));
        assertTrue(ValidationUtil.isValidPackageName("com.Example.Package"));
        assertTrue(ValidationUtil.isValidPackageName("com.example-package"));  // hyphen allowed
        assertTrue(ValidationUtil.isValidPackageName("com.example_package"));  // underscore allowed
        assertTrue(ValidationUtil.isValidPackageName("com.example.package-123"));  // hyphen allowed
        assertTrue(ValidationUtil.isValidPackageName("com.example.package_123"));  // underscore allowed
        assertTrue(ValidationUtil.isValidPackageName("com.example-package.sub-package"));  // multiple hyphens
        assertTrue(ValidationUtil.isValidPackageName("com.example_package.sub_package"));  // multiple underscores

        // Invalid cases
        assertFalse(ValidationUtil.isValidPackageName("123com.example"));  // starts with number
        assertFalse(ValidationUtil.isValidPackageName("com.123example"));  // part starts with number
        assertFalse(ValidationUtil.isValidPackageName("com.example.123package"));  // part starts with number
        assertFalse(ValidationUtil.isValidPackageName("com.123.example"));  // part starts with number
        assertFalse(ValidationUtil.isValidPackageName("com.123.456"));  // parts start with numbers
        assertFalse(ValidationUtil.isValidPackageName("com..example"));  // double dot
        assertFalse(ValidationUtil.isValidPackageName("com.example."));  // trailing dot
        assertFalse(ValidationUtil.isValidPackageName(".com.example"));  // leading dot
        assertFalse(ValidationUtil.isValidPackageName("com.example/package"));  // slash not allowed
        assertFalse(ValidationUtil.isValidPackageName("com.example package"));  // space not allowed
        assertFalse(ValidationUtil.isValidPackageName(null));  // null
        assertFalse(ValidationUtil.isValidPackageName(""));  // empty
    }
} 
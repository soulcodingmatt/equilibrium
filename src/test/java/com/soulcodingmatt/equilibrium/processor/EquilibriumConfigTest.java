package com.soulcodingmatt.equilibrium.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EquilibriumConfigTest {
    private Map<String, String> options;
    private EquilibriumConfig config;

    @BeforeEach
    void setUp() {
        options = new HashMap<>();
        ProcessingEnvironment processingEnv = new ProcessingEnvironment() {
            @Override
            public Map<String, String> getOptions() {
                return options;
            }

            // Implement other required methods with default values
            @Override
            public javax.lang.model.util.Elements getElementUtils() {
                return null;
            }

            @Override
            public javax.lang.model.util.Types getTypeUtils() {
                return null;
            }

            @Override
            public javax.annotation.processing.Filer getFiler() {
                return null;
            }

            @Override
            public javax.annotation.processing.Messager getMessager() {
                return null;
            }

            @Override
            public javax.lang.model.SourceVersion getSourceVersion() {
                return null;
            }

            @Override
            public java.util.Locale getLocale() {
                return null;
            }
        };
        config = new EquilibriumConfig(processingEnv);
    }

    @Test
    void testGetDtoPackage() {
        // Test with no configuration
        assertTrue(config.getDtoPackage().isEmpty());

        // Test with valid package
        options.put("equilibrium.dto.package", "com.example");
        assertEquals(Optional.of("com.example"), config.getDtoPackage());

        // Test with invalid package
        options.put("equilibrium.dto.package", "123com.example");
        assertTrue(config.getDtoPackage().isEmpty());
    }

    @Test
    void testGetDtoPostfix() {
        // Test with no configuration
        assertEquals("Dto", config.getDtoPostfix());

        // Test with valid postfix
        options.put("equilibrium.dto.postfix", "DTO");
        assertEquals("DTO", config.getDtoPostfix());

        // Test with valid postfix that includes numbers
        options.put("equilibrium.dto.postfix", "123Dto");
        assertEquals("123Dto", config.getDtoPostfix());
    }

    @Test
    void testGetRecordPackage() {
        // Test with no configuration
        assertTrue(config.getRecordPackage().isEmpty());

        // Test with valid package
        options.put("equilibrium.record.package", "com.example");
        assertEquals(Optional.of("com.example"), config.getRecordPackage());

        // Test with invalid package
        options.put("equilibrium.record.package", "123com.example");
        assertTrue(config.getRecordPackage().isEmpty());
    }

    @Test
    void testGetRecordPostfix() {
        // Test with no configuration
        assertEquals("Record", config.getRecordPostfix());

        // Test with valid postfix
        options.put("equilibrium.record.postfix", "Record");
        assertEquals("Record", config.getRecordPostfix());

        // Test with valid postfix that includes numbers
        options.put("equilibrium.record.postfix", "123Record");
        assertEquals("123Record", config.getRecordPostfix());
    }

    @Test
    void testGetVoPackage() {
        // Test with no configuration
        assertTrue(config.getVoPackage().isEmpty());

        // Test with valid package
        options.put("equilibrium.vo.package", "com.example");
        assertEquals(Optional.of("com.example"), config.getVoPackage());

        // Test with invalid package
        options.put("equilibrium.vo.package", "123com.example");
        assertTrue(config.getVoPackage().isEmpty());
    }

    @Test
    void testGetVoPostfix() {
        // Test with no configuration
        assertEquals("Vo", config.getVoPostfix());

        // Test with valid postfix
        options.put("equilibrium.vo.postfix", "VO");
        assertEquals("VO", config.getVoPostfix());

        // Test with valid postfix that includes numbers
        options.put("equilibrium.vo.postfix", "123Vo");
        assertEquals("123Vo", config.getVoPostfix());
    }

    @Test
    void testValidateAndGetPackage() {
        // Test with valid annotation package
        options.clear();
        assertEquals("com.example", config.validateAndGetPackage("com.example", EquilibriumConfig.DTO));

        // Test with invalid annotation package but valid global package
        options.clear();
        options.put("equilibrium.dto.package", "com.example");
        assertEquals("com.example", config.validateAndGetPackage("123com.example", EquilibriumConfig.DTO));

        // Test with invalid annotation package and no global package
        options.clear();
        assertEquals("com.soulcodingmatt.equilibrium.dto", 
            config.validateAndGetPackage("123com.example", EquilibriumConfig.DTO));
    }

    @Test
    void testValidateAndGetPostfix() {
        // Test with valid annotation postfix
        assertEquals("Dto", config.validateAndGetPostfix("Dto", EquilibriumConfig.DTO));

        // Test with valid postfix that includes numbers
        assertEquals("123Dto", config.validateAndGetPostfix("123Dto", EquilibriumConfig.DTO));

        // Test with invalid class type
        assertThrows(IllegalArgumentException.class, () -> 
            config.validateAndGetPostfix("", "INVALID_TYPE"));
    }

    @Test
    void testIsValidFieldName() {
        // Test valid field names
        assertTrue(config.isValidFieldName("field"));
        assertTrue(config.isValidFieldName("fieldName"));
        assertTrue(config.isValidFieldName("field123"));
        assertTrue(config.isValidFieldName("123field"));

        // Test invalid field names
        assertFalse(config.isValidFieldName("field name"));  // spaces not allowed
        assertFalse(config.isValidFieldName("field/name"));  // special characters not allowed
        assertFalse(config.isValidFieldName("field-name"));  // hyphens not allowed
        assertFalse(config.isValidFieldName("field_name"));  // underscores not allowed
    }
} 
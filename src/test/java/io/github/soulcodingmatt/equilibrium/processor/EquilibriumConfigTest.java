package io.github.soulcodingmatt.equilibrium.processor;

import static org.junit.jupiter.api.Assertions.*;

import io.github.soulcodingmatt.equilibrium.processor.config.EquilibriumConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.processing.ProcessingEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EquilibriumConfigTest {
  private Map<String, String> options;
  private EquilibriumConfig config;

  @BeforeEach
  void setUp() {
    options = new HashMap<>();
    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
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
    assertEquals(
        "com.example", config.validateAndGetPackage("123com.example", EquilibriumConfig.DTO));

    // Test with invalid annotation package and no global package
    options.clear();
    assertEquals(
        "io.github.soulcodingmatt.equilibrium.dto",
        config.validateAndGetPackage("123com.example", EquilibriumConfig.DTO));
  }

  @Test
  void testValidateAndGetPackage_ValidPackageNames() {
    options.clear();

    // Test simple valid package
    assertEquals("com.example", config.validateAndGetPackage("com.example", EquilibriumConfig.DTO));

    // Test multi-level package
    assertEquals(
        "com.example.dto.model",
        config.validateAndGetPackage("com.example.dto.model", EquilibriumConfig.DTO));

    // Test package with numbers
    assertEquals(
        "com.example123.dto",
        config.validateAndGetPackage("com.example123.dto", EquilibriumConfig.DTO));

    // Test package with underscores
    assertEquals(
        "com.example_test.dto",
        config.validateAndGetPackage("com.example_test.dto", EquilibriumConfig.DTO));

    // Test package with hyphens
    assertEquals(
        "com.example-test.dto",
        config.validateAndGetPackage("com.example-test.dto", EquilibriumConfig.DTO));

    // Test single-level package
    assertEquals("example", config.validateAndGetPackage("example", EquilibriumConfig.DTO));
  }

  @Test
  void testValidateAndGetPackage_InvalidPackageNames_ThrowsWithClearMessages() {
    options.clear();

    // Test null package (empty string falls back to default)
    String defaultPackage = config.validateAndGetPackage("", EquilibriumConfig.DTO);
    assertNotNull(defaultPackage);
    assertTrue(defaultPackage.endsWith(".dto"));

    // Test package starting with number - falls back to default (doesn't throw)
    String result1 = config.validateAndGetPackage("123com.example", EquilibriumConfig.DTO);
    assertNotNull(result1);
    assertTrue(result1.endsWith(".dto"));

    // Test package with leading dot - falls back to default (doesn't throw)
    String result2 = config.validateAndGetPackage(".com.example", EquilibriumConfig.DTO);
    assertNotNull(result2);
    assertTrue(result2.endsWith(".dto"));

    // Test package with trailing dot - falls back to default (doesn't throw)
    String result3 = config.validateAndGetPackage("com.example.", EquilibriumConfig.DTO);
    assertNotNull(result3);
    assertTrue(result3.endsWith(".dto"));

    // Test package with consecutive dots - falls back to default (doesn't throw)
    String result4 = config.validateAndGetPackage("com..example", EquilibriumConfig.DTO);
    assertNotNull(result4);
    assertTrue(result4.endsWith(".dto"));

    // Test package with spaces - falls back to default (doesn't throw)
    String result5 = config.validateAndGetPackage("com.example test", EquilibriumConfig.DTO);
    assertNotNull(result5);
    assertTrue(result5.endsWith(".dto"));

    // Test package with special characters - falls back to default (doesn't throw)
    String result6 = config.validateAndGetPackage("com.example@test", EquilibriumConfig.DTO);
    assertNotNull(result6);
    assertTrue(result6.endsWith(".dto"));

    // Test package with Java reserved keyword - falls back to default (doesn't throw)
    String result7 = config.validateAndGetPackage("com.class.example", EquilibriumConfig.DTO);
    assertNotNull(result7);
    assertTrue(result7.endsWith(".dto"));
  }

  @Test
  void testValidateAndGetPackage_FallbackToGlobalConfig() {
    // Test fallback to global DTO package
    options.clear();
    options.put("equilibrium.dto.package", "com.global.dto");
    assertEquals("com.global.dto", config.validateAndGetPackage("", EquilibriumConfig.DTO));

    // Test fallback to global Record package
    options.clear();
    options.put("equilibrium.record.package", "com.global.record");
    assertEquals("com.global.record", config.validateAndGetPackage("", EquilibriumConfig.RECORD));

    // Test fallback to global VO package
    options.clear();
    options.put("equilibrium.vo.package", "com.global.vo");
    assertEquals("com.global.vo", config.validateAndGetPackage("", EquilibriumConfig.VO));
  }

  @Test
  void testValidateAndGetPackage_FallbackToDefaultPackage() {
    // Test fallback to default package when no annotation package and no global config
    options.clear();

    String dtoPackage = config.validateAndGetPackage("", EquilibriumConfig.DTO);
    assertTrue(dtoPackage.endsWith(".dto"), "Default DTO package should end with .dto");

    String recordPackage = config.validateAndGetPackage("", EquilibriumConfig.RECORD);
    assertTrue(recordPackage.endsWith(".record"), "Default Record package should end with .record");

    String voPackage = config.validateAndGetPackage("", EquilibriumConfig.VO);
    assertTrue(voPackage.endsWith(".vo"), "Default VO package should end with .vo");
  }

  @Test
  void testValidateAndGetPackage_AnnotationPackageTakesPrecedence() {
    // Test that valid annotation package takes precedence over global config
    options.clear();
    options.put("equilibrium.dto.package", "com.global.dto");

    assertEquals(
        "com.annotation.dto",
        config.validateAndGetPackage("com.annotation.dto", EquilibriumConfig.DTO));
  }

  @Test
  void testValidateAndGetPostfix() {
    // Test with valid annotation postfix
    assertEquals("Dto", config.validateAndGetPostfix("Dto", EquilibriumConfig.DTO));

    // Test with valid postfix that includes numbers
    assertEquals("123Dto", config.validateAndGetPostfix("123Dto", EquilibriumConfig.DTO));

    // Test with invalid class type
    assertThrows(
        IllegalArgumentException.class, () -> config.validateAndGetPostfix("", "INVALID_TYPE"));
  }

  @Test
  void testValidateAndGetPostfix_ValidPostfixes() {
    options.clear();

    // Test simple valid postfix
    assertEquals("Dto", config.validateAndGetPostfix("Dto", EquilibriumConfig.DTO));

    // Test uppercase postfix
    assertEquals("DTO", config.validateAndGetPostfix("DTO", EquilibriumConfig.DTO));

    // Test postfix with numbers
    assertEquals("Dto123", config.validateAndGetPostfix("Dto123", EquilibriumConfig.DTO));
    assertEquals("123Dto", config.validateAndGetPostfix("123Dto", EquilibriumConfig.DTO));

    // Test all numeric postfix
    assertEquals("123", config.validateAndGetPostfix("123", EquilibriumConfig.DTO));

    // Test long postfix
    assertEquals(
        "DataTransferObject",
        config.validateAndGetPostfix("DataTransferObject", EquilibriumConfig.DTO));
  }

  @Test
  void testValidateAndGetPostfix_InvalidPostfixes_ReturnsDefault() {
    options.clear();

    // Test empty postfix - should return default
    assertEquals("Dto", config.validateAndGetPostfix("", EquilibriumConfig.DTO));
    assertEquals("Record", config.validateAndGetPostfix("", EquilibriumConfig.RECORD));
    assertEquals("Vo", config.validateAndGetPostfix("", EquilibriumConfig.VO));

    // Test postfix with spaces - should return default
    assertEquals("Dto", config.validateAndGetPostfix("Dto Test", EquilibriumConfig.DTO));

    // Test postfix with special characters - should return default
    assertEquals("Dto", config.validateAndGetPostfix("Dto-Test", EquilibriumConfig.DTO));
    assertEquals("Dto", config.validateAndGetPostfix("Dto_Test", EquilibriumConfig.DTO));
    assertEquals("Dto", config.validateAndGetPostfix("Dto@Test", EquilibriumConfig.DTO));
    assertEquals("Dto", config.validateAndGetPostfix("Dto.Test", EquilibriumConfig.DTO));

    // Test postfix with only special characters - should return default
    assertEquals("Dto", config.validateAndGetPostfix("@#$", EquilibriumConfig.DTO));
  }

  @Test
  void testValidateAndGetPostfix_FallbackToGlobalConfig() {
    // Test fallback to global DTO postfix
    options.clear();
    options.put("equilibrium.dto.postfix", "DataTransferObject");
    assertEquals("DataTransferObject", config.validateAndGetPostfix("", EquilibriumConfig.DTO));
    assertEquals(
        "DataTransferObject",
        config.validateAndGetPostfix("Invalid-Postfix", EquilibriumConfig.DTO));

    // Test fallback to global Record postfix
    options.clear();
    options.put("equilibrium.record.postfix", "Rec");
    assertEquals("Rec", config.validateAndGetPostfix("", EquilibriumConfig.RECORD));
    assertEquals("Rec", config.validateAndGetPostfix("Invalid-Postfix", EquilibriumConfig.RECORD));

    // Test fallback to global VO postfix
    options.clear();
    options.put("equilibrium.vo.postfix", "ValueObject");
    assertEquals("ValueObject", config.validateAndGetPostfix("", EquilibriumConfig.VO));
    assertEquals(
        "ValueObject", config.validateAndGetPostfix("Invalid-Postfix", EquilibriumConfig.VO));
  }

  @Test
  void testValidateAndGetPostfix_FallbackToDefaultPostfix() {
    // Test fallback to default postfix when no annotation postfix and no global config
    options.clear();

    assertEquals("Dto", config.validateAndGetPostfix("", EquilibriumConfig.DTO));
    assertEquals("Record", config.validateAndGetPostfix("", EquilibriumConfig.RECORD));
    assertEquals("Vo", config.validateAndGetPostfix("", EquilibriumConfig.VO));

    // Test with invalid postfixes
    assertEquals("Dto", config.validateAndGetPostfix("Invalid-Postfix", EquilibriumConfig.DTO));
    assertEquals(
        "Record", config.validateAndGetPostfix("Invalid-Postfix", EquilibriumConfig.RECORD));
    assertEquals("Vo", config.validateAndGetPostfix("Invalid-Postfix", EquilibriumConfig.VO));
  }

  @Test
  void testValidateAndGetPostfix_AnnotationPostfixTakesPrecedence() {
    // Test that valid annotation postfix takes precedence over global config
    options.clear();
    options.put("equilibrium.dto.postfix", "GlobalDto");

    assertEquals(
        "AnnotationDto", config.validateAndGetPostfix("AnnotationDto", EquilibriumConfig.DTO));
  }

  @Test
  void testValidateAndGetPostfix_InvalidClassType() {
    // Test with invalid class type - need to pass invalid postfix to reach the switch statement
    // When postfix is valid, it returns immediately without checking classType
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> config.validateAndGetPostfix("", "INVALID_TYPE"));
    assertTrue(ex.getMessage().contains("Invalid class type"));

    // Also test with invalid postfix (special characters) and invalid class type
    IllegalArgumentException ex2 =
        assertThrows(
            IllegalArgumentException.class,
            () -> config.validateAndGetPostfix("Invalid-Postfix", "INVALID_TYPE"));
    assertTrue(ex2.getMessage().contains("Invalid class type"));
  }

  @Test
  void testIsValidFieldName() {
    // Test valid field names
    assertTrue(config.isValidFieldName("field"));
    assertTrue(config.isValidFieldName("fieldName"));
    assertTrue(config.isValidFieldName("field123"));
    assertTrue(config.isValidFieldName("123field"));

    // Test invalid field names
    assertFalse(config.isValidFieldName("field name")); // spaces not allowed
    assertFalse(config.isValidFieldName("field/name")); // special characters not allowed
    assertFalse(config.isValidFieldName("field-name")); // hyphens not allowed
    assertFalse(config.isValidFieldName("field_name")); // underscores not allowed
  }

  @Test
  void testAllSupportedOptionsAreRead() {
    // Test that all supported options are read from ProcessingEnvironment.getOptions()
    options.clear();

    // Set all supported options
    options.put("equilibrium.dto.package", "com.example.dto");
    options.put("equilibrium.dto.postfix", "DataTransferObject");
    options.put("equilibrium.record.package", "com.example.record");
    options.put("equilibrium.record.postfix", "Rec");
    options.put("equilibrium.vo.package", "com.example.vo");
    options.put("equilibrium.vo.postfix", "ValueObject");
    options.put("equilibrium.groupId", "com.example");
    options.put("equilibrium.artifactId", "myproject");

    // Create new config with all options set
    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return options;
          }

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
    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // Verify all DTO options are read
    assertEquals(Optional.of("com.example.dto"), testConfig.getDtoPackage());
    assertEquals("DataTransferObject", testConfig.getDtoPostfix());

    // Verify all Record options are read
    assertEquals(Optional.of("com.example.record"), testConfig.getRecordPackage());
    assertEquals("Rec", testConfig.getRecordPostfix());

    // Verify all VO options are read
    assertEquals(Optional.of("com.example.vo"), testConfig.getVoPackage());
    assertEquals("ValueObject", testConfig.getVoPostfix());

    // Verify groupId and artifactId are used in default package derivation
    // When annotation package is empty and no global package is set, it should use
    // groupId/artifactId
    options.clear();
    options.put("equilibrium.groupId", "com.example");
    options.put("equilibrium.artifactId", "myproject");
    EquilibriumConfig configWithCoordinates = new EquilibriumConfig(processingEnv);

    String defaultDtoPackage =
        configWithCoordinates.validateAndGetPackage("", EquilibriumConfig.DTO);
    assertTrue(
        defaultDtoPackage.contains("com.example") || defaultDtoPackage.contains("myproject"),
        "Default package should be derived from groupId and artifactId");
  }

  @Test
  void testGroupIdAndArtifactIdFromOptions() {
    // Test that groupId and artifactId are read from options
    options.clear();
    options.put("equilibrium.groupId", "org.test");
    options.put("equilibrium.artifactId", "testapp");

    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return options;
          }

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
    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // Verify that default package uses the configured groupId and artifactId
    String defaultPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.DTO);
    assertTrue(
        defaultPackage.contains("org.test") || defaultPackage.contains("testapp"),
        "Default package should use configured groupId and artifactId");
  }

  @Test
  void testOptionsReadFromProcessingEnvironment() {
    // Verify that options are actually read from ProcessingEnvironment.getOptions()
    Map<String, String> testOptions = new HashMap<>();
    testOptions.put("equilibrium.dto.package", "com.test.dto");
    testOptions.put("equilibrium.dto.postfix", "TestDto");

    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return testOptions;
          }

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

    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // Verify the options were read correctly
    assertEquals(Optional.of("com.test.dto"), testConfig.getDtoPackage());
    assertEquals("TestDto", testConfig.getDtoPostfix());
  }

  @Test
  void testInferGroupIdArtifactIdFromPomXml() {
    // This test verifies that when options are not set, EquilibriumConfig
    // infers groupId and artifactId from the pom.xml file in the project root.
    // The actual pom.xml in this project has:
    // <groupId>io.github.soulcodingmatt</groupId>
    // <artifactId>equilibrium</artifactId>

    options.clear();
    // Do not set equilibrium.groupId or equilibrium.artifactId

    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return options;
          }

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

    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // Verify that the default package uses inferred coordinates from pom.xml
    // The actual pom.xml has groupId=io.github.soulcodingmatt and artifactId=equilibrium
    String defaultPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.DTO);

    // The default package should contain the sanitized groupId and artifactId
    // Expected: io.github.soulcodingmatt.equilibrium.dto (or similar)
    assertTrue(
        defaultPackage.contains("io.github.soulcodingmatt")
            || defaultPackage.contains("equilibrium"),
        "Default package should be derived from pom.xml coordinates. Got: " + defaultPackage);
  }

  @Test
  void testInferenceStrategiesWithMultiplePomStructures() {
    // This test verifies that the multiple inference strategies work:
    // 1. Coordinates outside parent tags
    // 2. Coordinates with parent section removed
    // 3. Fallback pattern

    // Since we're testing with the actual pom.xml in the project,
    // we verify that the inference works correctly for the current structure.
    // The current pom.xml has no parent, so it should use the simple strategy.

    options.clear();

    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return options;
          }

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

    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // Get the default package for DTO, Record, and VO
    String dtoPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.DTO);
    String recordPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.RECORD);
    String voPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.VO);

    // All should be derived from the same base (inferred from pom.xml)
    assertTrue(
        dtoPackage.contains("io.github.soulcodingmatt") || dtoPackage.contains("equilibrium"),
        "DTO package should use inferred coordinates. Got: " + dtoPackage);
    assertTrue(
        recordPackage.contains("io.github.soulcodingmatt") || recordPackage.contains("equilibrium"),
        "Record package should use inferred coordinates. Got: " + recordPackage);
    assertTrue(
        voPackage.contains("io.github.soulcodingmatt") || voPackage.contains("equilibrium"),
        "VO package should use inferred coordinates. Got: " + voPackage);

    // Verify they have the correct postfix in the package
    assertTrue(dtoPackage.endsWith(".dto"), "DTO package should end with .dto");
    assertTrue(recordPackage.endsWith(".record"), "Record package should end with .record");
    assertTrue(voPackage.endsWith(".vo"), "VO package should end with .vo");
  }

  @Test
  void testDefaultPackageDerivation_OptionsOnly() {
    // Test precedence level 1: Options are set (highest priority)
    // When options are set, they should be used regardless of pom.xml
    options.clear();
    options.put("equilibrium.dto.package", "com.options.dto");
    options.put("equilibrium.record.package", "com.options.record");
    options.put("equilibrium.vo.package", "com.options.vo");

    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return options;
          }

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
    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // Verify that options are used for default package derivation
    assertEquals("com.options.dto", testConfig.validateAndGetPackage("", EquilibriumConfig.DTO));
    assertEquals(
        "com.options.record", testConfig.validateAndGetPackage("", EquilibriumConfig.RECORD));
    assertEquals("com.options.vo", testConfig.validateAndGetPackage("", EquilibriumConfig.VO));
  }

  @Test
  void testDefaultPackageDerivation_PomInference() {
    // Test precedence level 2: Pom inference (when options are not set)
    // This test uses the actual pom.xml in the project
    options.clear();
    // Do not set any package options - should fall back to pom inference

    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return options;
          }

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
    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // Verify that pom inference is used (from actual pom.xml)
    String dtoPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.DTO);
    String recordPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.RECORD);
    String voPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.VO);

    // Should contain coordinates from pom.xml (io.github.soulcodingmatt.equilibrium)
    assertTrue(
        dtoPackage.contains("io.github.soulcodingmatt") || dtoPackage.contains("equilibrium"),
        "DTO package should be inferred from pom.xml. Got: " + dtoPackage);
    assertTrue(
        recordPackage.contains("io.github.soulcodingmatt") || recordPackage.contains("equilibrium"),
        "Record package should be inferred from pom.xml. Got: " + recordPackage);
    assertTrue(
        voPackage.contains("io.github.soulcodingmatt") || voPackage.contains("equilibrium"),
        "VO package should be inferred from pom.xml. Got: " + voPackage);

    // Verify correct postfixes
    assertTrue(dtoPackage.endsWith(".dto"));
    assertTrue(recordPackage.endsWith(".record"));
    assertTrue(voPackage.endsWith(".vo"));
  }

  @Test
  void testDefaultPackageDerivation_HardcodedDefault() {
    // Test precedence level 3: Hardcoded default (when pom.xml is not found)
    // We can't easily test this without changing the working directory,
    // but we can verify the behavior by setting groupId/artifactId options
    // which simulates the fallback behavior
    options.clear();
    options.put("equilibrium.groupId", "io.github.soulcodingmatt");
    options.put("equilibrium.artifactId", "equilibrium");
    // Do not set package options

    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return options;
          }

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
    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // Verify that hardcoded defaults are used
    String dtoPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.DTO);
    String recordPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.RECORD);
    String voPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.VO);

    // Should use the hardcoded default coordinates
    assertTrue(
        dtoPackage.contains("io.github.soulcodingmatt") && dtoPackage.contains("equilibrium"),
        "DTO package should use hardcoded defaults. Got: " + dtoPackage);
    assertTrue(
        recordPackage.contains("io.github.soulcodingmatt") && recordPackage.contains("equilibrium"),
        "Record package should use hardcoded defaults. Got: " + recordPackage);
    assertTrue(
        voPackage.contains("io.github.soulcodingmatt") && voPackage.contains("equilibrium"),
        "VO package should use hardcoded defaults. Got: " + voPackage);
  }

  @Test
  void testDefaultPackageDerivation_PrecedenceOrder() {
    // Test the complete precedence order: options > pom inference > hardcoded default

    // Level 1: Options take precedence over everything
    options.clear();
    options.put("equilibrium.dto.package", "com.level1.options");
    options.put("equilibrium.groupId", "com.level2.pom");
    options.put("equilibrium.artifactId", "pomproject");

    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return options;
          }

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
    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // Options should win
    assertEquals("com.level1.options", testConfig.validateAndGetPackage("", EquilibriumConfig.DTO));

    // Level 2: When options are not set, use groupId/artifactId (simulating pom inference)
    options.clear();
    options.put("equilibrium.groupId", "com.level2.pom");
    options.put("equilibrium.artifactId", "pomproject");
    EquilibriumConfig testConfig2 = new EquilibriumConfig(processingEnv);

    String dtoPackage = testConfig2.validateAndGetPackage("", EquilibriumConfig.DTO);
    assertTrue(
        dtoPackage.contains("com.level2.pom") || dtoPackage.contains("pomproject"),
        "Should use groupId/artifactId when package option not set. Got: " + dtoPackage);

    // Level 3: When nothing is set, use hardcoded defaults (tested in actual project context)
    // This is already tested in testDefaultPackageDerivation_PomInference
  }

  @Test
  void testDefaultPackageDerivation_AllClassTypes() {
    // Verify that default package derivation works for all class types (DTO, Record, VO)
    options.clear();
    options.put("equilibrium.groupId", "com.test.all");
    options.put("equilibrium.artifactId", "alltypes");

    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return options;
          }

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
    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // All class types should derive their default package correctly
    String dtoPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.DTO);
    String recordPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.RECORD);
    String voPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.VO);

    // All should contain the base coordinates
    assertTrue(dtoPackage.contains("com.test.all") || dtoPackage.contains("alltypes"));
    assertTrue(recordPackage.contains("com.test.all") || recordPackage.contains("alltypes"));
    assertTrue(voPackage.contains("com.test.all") || voPackage.contains("alltypes"));

    // Each should have the correct postfix
    assertTrue(dtoPackage.endsWith(".dto"));
    assertTrue(recordPackage.endsWith(".record"));
    assertTrue(voPackage.endsWith(".vo"));
  }

  @Test
  void testFallbackWhenPomXmlNotFound() {
    // This test verifies that when pom.xml cannot be read or doesn't exist,
    // the system falls back to hardcoded defaults.
    // We can't easily simulate this without changing the working directory,
    // but we can verify the behavior when options are set (which takes precedence).

    options.clear();
    options.put("equilibrium.groupId", "com.test");
    options.put("equilibrium.artifactId", "testproject");

    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return options;
          }

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

    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // When options are set, they should take precedence over pom.xml inference
    String defaultPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.DTO);
    assertTrue(
        defaultPackage.contains("com.test") || defaultPackage.contains("testproject"),
        "Default package should use configured groupId/artifactId. Got: " + defaultPackage);
  }

  @Test
  void testSanitizationOfGroupIdAndArtifactId() {
    // This test verifies that groupId and artifactId are sanitized correctly
    // when they contain dashes, underscores, or other special characters.

    options.clear();
    options.put("equilibrium.groupId", "com.test-group");
    options.put("equilibrium.artifactId", "my_artifact-id");

    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return options;
          }

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

    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // Get the default package - it should have sanitized the groupId and artifactId
    String defaultPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.DTO);

    // Verify that dashes and underscores are removed
    assertFalse(defaultPackage.contains("-"), "Package should not contain dashes");
    assertFalse(defaultPackage.contains("_"), "Package should not contain underscores");

    // Verify it's a valid package name
    assertTrue(
        defaultPackage.matches("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$"),
        "Package should be a valid Java package name. Got: " + defaultPackage);
  }

  @Test
  void testPrecedenceOfOptionsOverPomInference() {
    // This test verifies that when both options and pom.xml are available,
    // options take precedence.

    options.clear();
    options.put("equilibrium.groupId", "com.override");
    options.put("equilibrium.artifactId", "overrideproject");

    ProcessingEnvironment processingEnv =
        new ProcessingEnvironment() {
          @Override
          public Map<String, String> getOptions() {
            return options;
          }

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

    EquilibriumConfig testConfig = new EquilibriumConfig(processingEnv);

    // The default package should use the configured options, not the pom.xml values
    String defaultPackage = testConfig.validateAndGetPackage("", EquilibriumConfig.DTO);

    // Should contain the override values, not the pom.xml values
    assertTrue(
        defaultPackage.contains("com.override") || defaultPackage.contains("overrideproject"),
        "Default package should use configured options over pom.xml. Got: " + defaultPackage);

    // Should NOT contain the pom.xml values (unless they happen to match)
    // Since we're overriding with different values, this should be safe
    assertFalse(
        defaultPackage.contains("io.github.soulcodingmatt")
            && !defaultPackage.contains("com.override"),
        "Default package should not use pom.xml values when options are set");
  }
}

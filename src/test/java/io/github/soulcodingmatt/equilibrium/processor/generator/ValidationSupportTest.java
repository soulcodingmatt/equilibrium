package io.github.soulcodingmatt.equilibrium.processor.generator;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import io.github.soulcodingmatt.equilibrium.processor.testsupport.CompileTestClasspath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Locks in {@link ValidationSupport} import collection and type-safe emission for DTO, Record, and
 * VO validation annotations.
 */
class ValidationSupportTest {

  @BeforeEach
  void resetHarness() {
    ValidationSupportHarnessProcessor.dtoImportsForId1 = null;
    ValidationSupportHarnessProcessor.dtoImportsForId2 = null;
    ValidationSupportHarnessProcessor.dtoWriteTypeSafeBlock = null;
    ValidationSupportHarnessProcessor.recordImportsForId1 = null;
    ValidationSupportHarnessProcessor.recordWriteTypeSafeBlock = null;
    ValidationSupportHarnessProcessor.voImportsForId1 = null;
    ValidationSupportHarnessProcessor.voWriteTypeSafeBlock = null;
    ValidationSupportHarnessProcessor.shouldApplyDtoId1 = false;
    ValidationSupportHarnessProcessor.shouldApplyDtoId2 = false;
  }

  @Test
  void collectValidationImports_respectsIds_andMergesTypeSafeAndStringAnnotations() {
    compileProbes();

    assertTrue(
        ValidationSupportHarnessProcessor.dtoImportsForId1.contains(
            "jakarta.validation.constraints.NotNull"));
    assertTrue(
        ValidationSupportHarnessProcessor.dtoImportsForId1.contains(
            "jakarta.validation.constraints.Email"));
    assertFalse(
        ValidationSupportHarnessProcessor.dtoImportsForId1.contains(
            "jakarta.validation.constraints.Min"));

    assertTrue(
        ValidationSupportHarnessProcessor.dtoImportsForId2.contains(
            "jakarta.validation.constraints.Min"));
    assertFalse(
        ValidationSupportHarnessProcessor.dtoImportsForId2.contains(
            "jakarta.validation.constraints.NotNull"));
  }

  @Test
  void shouldApplyValidation_matchesIdsArray() {
    compileProbes();

    assertTrue(ValidationSupportHarnessProcessor.shouldApplyDtoId1);
    assertFalse(ValidationSupportHarnessProcessor.shouldApplyDtoId2);
  }

  @Test
  void writeTypeSafeValidations_emitsNotNullWithCustomMessage_forDto() {
    compileProbes();

    String block = ValidationSupportHarnessProcessor.dtoWriteTypeSafeBlock;
    assertTrue(block.contains("    @NotNull"));
    assertTrue(block.contains("custom null"));
  }

  @Test
  void collectRecordValidationImports_andWrite_recordProbe() {
    compileProbes();

    assertTrue(
        ValidationSupportHarnessProcessor.recordImportsForId1.contains(
            "jakarta.validation.constraints.NotNull"));
    String block = ValidationSupportHarnessProcessor.recordWriteTypeSafeBlock;
    assertTrue(block.contains("@NotNull"));
    assertTrue(block.contains("rec"));
  }

  @Test
  void collectVoValidationImports_andWrite_voProbe() {
    compileProbes();

    assertTrue(
        ValidationSupportHarnessProcessor.voImportsForId1.contains(
            "jakarta.validation.constraints.NotNull"));
    String block = ValidationSupportHarnessProcessor.voWriteTypeSafeBlock;
    assertTrue(block.contains("    @NotNull"));
    assertTrue(block.contains("vo"));
  }

  private static void compileProbes() {
    Compilation compilation =
        javac()
            .withClasspath(CompileTestClasspath.currentJvm())
            .withProcessors(new ValidationSupportHarnessProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "com.acme.probe.ValidationDtoProbe",
                    """
                    package com.acme.probe;

                    import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;
                    import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;

                    public class ValidationDtoProbe {

                      @ValidateDto(
                          ids = {1},
                          notNull = @NotNull(message = "custom null"),
                          value = {"@Email(message = \\"via string\\")"})
                      private String withId1;

                      @ValidateDto(ids = {2}, min = @Min(value = 5))
                      private long withId2;
                    }
                    """),
                JavaFileObjects.forSourceString(
                    "com.acme.probe.ValidationRecordProbe",
                    """
                    package com.acme.probe;

                    import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;
                    import io.github.soulcodingmatt.equilibrium.experimental.validation.record.ValidateRecord;

                    public class ValidationRecordProbe {

                      @ValidateRecord(ids = {1}, notNull = @NotNull(message = "rec"))
                      private String component;
                    }
                    """),
                JavaFileObjects.forSourceString(
                    "com.acme.probe.ValidationVoProbe",
                    """
                    package com.acme.probe;

                    import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;
                    import io.github.soulcodingmatt.equilibrium.experimental.validation.vo.ValidateVo;

                    public class ValidationVoProbe {

                      @ValidateVo(ids = {1}, notNull = @NotNull(message = "vo"))
                      private String field;
                    }
                    """));

    assertThat(compilation).succeeded();
  }
}

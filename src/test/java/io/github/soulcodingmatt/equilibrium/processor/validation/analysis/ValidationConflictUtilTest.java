package io.github.soulcodingmatt.equilibrium.processor.validation.analysis;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import io.github.soulcodingmatt.equilibrium.processor.testsupport.CompileTestClasspath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Locks in {@link ValidationConflictUtil} messages before refactors in the validationconflict
 * package.
 */
class ValidationConflictUtilTest {

  @BeforeEach
  void resetHarness() {
    ValidationConflictHarnessProcessor.reset();
  }

  @Test
  void validateField_detectsPositiveAndNegativeOnSameAnnotation() {
    compileProbe(
        """
        package com.acme.probe;

        import io.github.soulcodingmatt.equilibrium.experimental.validation.common.Negative;
        import io.github.soulcodingmatt.equilibrium.experimental.validation.common.Positive;
        import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;

        public class ValidationConflictProbe {

          @ValidateDto(
              positive = @Positive(message = "p"),
              negative = @Negative(message = "n"))
          private int positiveNegative;
        }
        """);

    assertEquals(1, ValidationConflictHarnessProcessor.positiveNegativeErrors.size());
    assertTrue(
        ValidationConflictHarnessProcessor.positiveNegativeErrors
            .get(0)
            .contains("@Positive and @Negative are contradictory"));
  }

  @Test
  void validateField_rejectsNotNullOnPrimitive() {
    compileProbe(
        """
        package com.acme.probe;

        import io.github.soulcodingmatt.equilibrium.experimental.validation.common.NotNull;
        import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;

        public class ValidationConflictProbe {

          @ValidateDto(notNull = @NotNull(message = "x"))
          private int notNullOnPrimitive;
        }
        """);

    assertEquals(1, ValidationConflictHarnessProcessor.notNullOnPrimitiveErrors.size());
    assertTrue(
        ValidationConflictHarnessProcessor.notNullOnPrimitiveErrors
            .get(0)
            .contains("cannot be applied to primitive"));
  }

  @Test
  void validateField_rejectsNotBlankOnNonString() {
    compileProbe(
        """
        package com.acme.probe;

        import io.github.soulcodingmatt.equilibrium.experimental.validation.common.NotBlank;
        import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;

        public class ValidationConflictProbe {

          @ValidateDto(notBlank = @NotBlank(message = "x"))
          private int notBlankOnInt;
        }
        """);

    assertEquals(1, ValidationConflictHarnessProcessor.notBlankOnIntErrors.size());
    assertTrue(ValidationConflictHarnessProcessor.notBlankOnIntErrors.get(0).contains("@NotBlank"));
    assertFalse(ValidationConflictHarnessProcessor.notBlankOnIntErrors.get(0).isEmpty());
  }

  private static void compileProbe(String source) {
    Compilation compilation =
        javac()
            .withClasspath(CompileTestClasspath.currentJvm())
            .withProcessors(new ValidationConflictHarnessProcessor())
            .compile(
                JavaFileObjects.forSourceString(ValidationConflictHarnessProcessor.PROBE, source));

    assertThat(compilation).succeeded();
  }
}

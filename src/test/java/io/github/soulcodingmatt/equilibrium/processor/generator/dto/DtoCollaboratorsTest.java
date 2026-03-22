package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import io.github.soulcodingmatt.equilibrium.processor.testsupport.CompileTestClasspath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Locks in behavior of {@link DtoBuilderDefaultSupport}, {@link DtoImportPlanner}, and {@link
 * DtoFieldWriter} in order.
 */
class DtoCollaboratorsTest {

  @BeforeEach
  void resetHarness() {
    DtoCollaboratorsHarnessProcessor.lastBuilderFieldDeclaration = null;
    DtoCollaboratorsHarnessProcessor.lastBuilderDefaultAnnotationBlock = null;
    DtoCollaboratorsHarnessProcessor.lastImportSection = null;
    DtoCollaboratorsHarnessProcessor.lastFieldWriterLabelBlock = null;
  }

  @Test
  void dtoBuilderDefaultSupport_fieldDeclaration_andBuilderDefaultAnnotation() {
    compileProbe();

    assertTrue(
        DtoCollaboratorsHarnessProcessor.lastBuilderFieldDeclaration.contains("= 42"),
        () -> "declaration: " + DtoCollaboratorsHarnessProcessor.lastBuilderFieldDeclaration);
    assertTrue(
        DtoCollaboratorsHarnessProcessor.lastBuilderDefaultAnnotationBlock.contains(
            "@Builder.Default"),
        () ->
            "annotation block: "
                + DtoCollaboratorsHarnessProcessor.lastBuilderDefaultAnnotationBlock);
  }

  @Test
  void dtoImportPlanner_mergesNestedMappingValidationAndBuilderImports() {
    compileProbe();

    String imp = DtoCollaboratorsHarnessProcessor.lastImportSection;
    assertTrue(imp.contains("import com.acme.probe.dto.LinkedDto"), () -> imp);
    assertTrue(imp.contains("import jakarta.validation.constraints.NotNull"), () -> imp);
    assertTrue(imp.contains("import lombok.Builder"), () -> imp);
  }

  @Test
  void dtoFieldWriter_emitsValidationAndPrivateField_forLabel() {
    compileProbe();

    String block = DtoCollaboratorsHarnessProcessor.lastFieldWriterLabelBlock;
    assertTrue(block.contains("@NotNull"), () -> block);
    assertTrue(block.contains("private java.lang.String label"), () -> block);
  }

  private static void compileProbe() {
    Compilation compilation =
        javac()
            .withClasspath(CompileTestClasspath.currentJvm())
            .withProcessors(new DtoCollaboratorsHarnessProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "com.acme.dto.LinkedDto",
                    """
                    package com.acme.dto;

                    public class LinkedDto {}
                    """),
                JavaFileObjects.forSourceString(
                    "com.acme.other.Entity",
                    """
                    package com.acme.other;

                    public class Entity {}
                    """),
                JavaFileObjects.forSourceString(
                    "com.acme.probe.DtoCollaboratorsProbe",
                    """
                    package com.acme.probe;

                    import com.acme.dto.LinkedDto;
                    import com.acme.other.Entity;
                    import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
                    import io.github.soulcodingmatt.equilibrium.annotations.dto.NestedMapping;
                    import io.github.soulcodingmatt.equilibrium.experimental.validation.common.NotNull;
                    import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;

                    public class DtoCollaboratorsProbe {

                      @NestedMapping(dtoClass = LinkedDto.class)
                      private Entity ref;

                      @ValidateDto(ids = {1}, notNull = @NotNull(message = "n"))
                      private String label;

                      @DtoBuilderDefault(intValue = 42)
                      private int score;
                    }
                    """));

    assertThat(compilation).succeeded();
  }
}

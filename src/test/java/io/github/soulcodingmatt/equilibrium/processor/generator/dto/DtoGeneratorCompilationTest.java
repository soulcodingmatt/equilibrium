package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import io.github.soulcodingmatt.equilibrium.processor.core.EquilibriumProcessor;
import org.junit.jupiter.api.Test;

/**
 * End-to-end compilation checks for DTO generation: catches accidental behavior changes in {@link
 * DtoGenerator} and its collaborators during refactors.
 */
class DtoGeneratorCompilationTest {

  @Test
  void generateDto_producesExpectedFieldsAndAccessors() throws Exception {
    Compilation compilation =
        javac()
            .withProcessors(new EquilibriumProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "com.acme.model.CompileProbePerson",
                    """
                    package com.acme.model;

                    import io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto;

                    @GenerateDto(pkg = "com.acme.dto.gen", name = "CompileProbePersonDto")
                    public class CompileProbePerson {
                      private String nickname;
                      private int score;
                    }
                    """));

    assertThat(compilation).succeeded();

    String generated =
        compilation
            .generatedSourceFile("com.acme.dto.gen.CompileProbePersonDto")
            .orElseThrow()
            .getCharContent(true)
            .toString();

    assertTrue(generated.contains("private java.lang.String nickname;"));
    assertTrue(generated.contains("private int score;"));
    assertTrue(generated.contains("public java.lang.String getNickname()"));
    assertTrue(generated.contains("public void setNickname(java.lang.String nickname)"));
    assertTrue(generated.contains("public int getScore()"));
    assertTrue(generated.contains("public void setScore(int score)"));
  }
}

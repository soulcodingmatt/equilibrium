package io.github.soulcodingmatt.equilibrium.processor.generation.vo;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import io.github.soulcodingmatt.equilibrium.processor.orchestration.EquilibriumProcessor;
import org.junit.jupiter.api.Test;

class VoGeneratorCompilationTest {

  @Test
  void generateVo_producesExpectedFieldsAndAccessors() throws Exception {
    Compilation compilation =
        javac()
            .withProcessors(new EquilibriumProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "com.acme.model.CompileProbePerson",
                    """
                    package com.acme.model;

                    import io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVo;

                    @GenerateVo(pkg = "com.acme.vo.gen", name = "CompileProbePersonVo")
                    public class CompileProbePerson {
                      private String nickname;
                      private int score;
                    }
                    """));

    assertThat(compilation).succeeded();

    String generated =
        compilation
            .generatedSourceFile("com.acme.vo.gen.CompileProbePersonVo")
            .orElseThrow()
            .getCharContent(true)
            .toString();

    assertTrue(generated.contains("private final java.lang.String nickname;"));
    assertTrue(generated.contains("private final int score;"));
    assertTrue(generated.contains("public java.lang.String getNickname()"));
    assertTrue(generated.contains("public int getScore()"));
    assertTrue(generated.contains("public boolean equals("));
    assertTrue(generated.contains("public int hashCode()"));
    assertTrue(generated.contains("public String toString()"));
  }
}

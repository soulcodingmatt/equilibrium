package io.github.soulcodingmatt.equilibrium.processor.generation.record;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import io.github.soulcodingmatt.equilibrium.processor.orchestration.EquilibriumProcessor;
import org.junit.jupiter.api.Test;

class RecordGeneratorCompilationTest {

  @Test
  void generateRecord_producesExpectedRecordWithParameters() throws Exception {
    Compilation compilation =
        javac()
            .withProcessors(new EquilibriumProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "com.acme.model.CompileProbePerson",
                    """
                    package com.acme.model;

                    import io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecord;

                    @GenerateRecord(pkg = "com.acme.record.gen", name = "CompileProbePersonRecord")
                    public class CompileProbePerson {
                      private String nickname;
                      private int score;
                    }
                    """));

    assertThat(compilation).succeeded();

    String generated =
        compilation
            .generatedSourceFile("com.acme.record.gen.CompileProbePersonRecord")
            .orElseThrow()
            .getCharContent(true)
            .toString();

    assertTrue(generated.contains("public record CompileProbePersonRecord("));
    assertTrue(generated.contains("java.lang.String nickname"));
    assertTrue(generated.contains("int score"));
  }
}

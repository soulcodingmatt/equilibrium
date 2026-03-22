package io.github.soulcodingmatt.equilibrium.processor.generator;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import io.github.soulcodingmatt.equilibrium.processor.generator.dto.DtoGenerator;
import io.github.soulcodingmatt.equilibrium.processor.testsupport.CompileTestClasspath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NestedMappingResolverTest {

  @BeforeEach
  void resetHarness() {
    NestedMappingResolverHarnessProcessor.lastFindDtoImport = null;
    NestedMappingResolverHarnessProcessor.lastTransformedScalar = null;
    NestedMappingResolverHarnessProcessor.lastTransformedList = null;
  }

  @Test
  void findDtoImport_usesFirstSourceRelativeDtoPackage_whenNotRegistered() {
    compileProbeWithDto("UnregisteredOnlyDto");

    assertEquals(
        "com.acme.probe.dto.UnregisteredOnlyDto",
        NestedMappingResolverHarnessProcessor.lastFindDtoImport);
  }

  @Test
  void findDtoImport_usesRegistry_whenDtoWasRegistered() {
    DtoGenerator.registerGeneratedDto("RegisteredOnlyDto", "com.registered.RegisteredOnlyDto");
    compileProbeWithDto("RegisteredOnlyDto");

    assertEquals(
        "com.registered.RegisteredOnlyDto",
        NestedMappingResolverHarnessProcessor.lastFindDtoImport);
  }

  @Test
  void getTransformedFieldType_replacesNonCollectionWithDtoSimpleName() {
    compileProbeWithDto("UnregisteredOnlyDto");

    assertEquals(
        "UnregisteredOnlyDto", NestedMappingResolverHarnessProcessor.lastTransformedScalar);
  }

  @Test
  void getTransformedFieldType_replacesCollectionElementTypeWithDtoSimpleName() {
    compileProbeWithDto("UnregisteredOnlyDto");

    assertEquals(
        "java.util.List<UnregisteredOnlyDto>",
        NestedMappingResolverHarnessProcessor.lastTransformedList);
  }

  private static void compileProbeWithDto(String dtoSimpleName) {
    Compilation compilation =
        javac()
            .withClasspath(CompileTestClasspath.currentJvm())
            .withProcessors(new NestedMappingResolverHarnessProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "com.acme.dto." + dtoSimpleName,
                    "package com.acme.dto;\n\npublic class " + dtoSimpleName + " {}\n"),
                JavaFileObjects.forSourceString(
                    "com.acme.voice.Voice",
                    """
                    package com.acme.voice;

                    public class Voice {}
                    """),
                JavaFileObjects.forSourceString(
                    "com.acme.probe.NestedMappingResolutionProbe",
                    """
                    package com.acme.probe;

                    import io.github.soulcodingmatt.equilibrium.annotations.dto.NestedMapping;
                    import java.util.List;

                    public class NestedMappingResolutionProbe {

                      @NestedMapping(dtoClass = com.acme.dto.%s.class)
                      private com.acme.voice.Voice voice;

                      @NestedMapping(dtoClass = com.acme.dto.%s.class)
                      private List<com.acme.voice.Voice> voices;
                    }
                    """
                        .formatted(dtoSimpleName, dtoSimpleName)));

    assertThat(compilation).succeeded();
  }
}

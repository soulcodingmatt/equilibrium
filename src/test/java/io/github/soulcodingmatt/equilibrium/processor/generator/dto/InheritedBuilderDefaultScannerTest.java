package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import io.github.soulcodingmatt.equilibrium.processor.testsupport.CompileTestClasspath;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.VariableElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InheritedBuilderDefaultScannerTest {

  @BeforeEach
  void resetHarness() {
    InheritedBuilderScanHarnessProcessor.lastScan = null;
  }

  @Test
  void scan_whenBuilderDisabled_returnsEmpty() {
    InheritedBuilderDefaultScan scan =
        InheritedBuilderDefaultScanner.scan(null, false, Collections.emptyList());
    assertTrue(scan.safeInitializers().isEmpty());
    assertTrue(scan.extraImports().isEmpty());
  }

  @Test
  void scan_whenTreesNull_returnsEmpty() {
    InheritedBuilderDefaultScan scan =
        InheritedBuilderDefaultScanner.scan(null, true, Collections.emptyList());
    assertTrue(scan.safeInitializers().isEmpty());
    assertTrue(scan.extraImports().isEmpty());
  }

  @Test
  void scan_whenFieldsEmpty_returnsEmpty() {
    InheritedBuilderDefaultScan scan =
        InheritedBuilderDefaultScanner.scan(null, true, Collections.emptyList());
    assertTrue(scan.safeInitializers().isEmpty());
  }

  /**
   * End-to-end: {@code @lombok.Builder.Default} initializers on a compiled probe type are
   * classified and copied the same way as before the refactor (literals, enum, collections,
   * Optional, Collections.empty*, and {@code @DtoBuilderDefault(inherit=false)} exclusion).
   */
  @Test
  void scan_withLombokBuilderDefaults_coercesExpectedInitializersAndImports() {
    Compilation compilation =
        javac()
            .withClasspath(CompileTestClasspath.currentJvm())
            .withProcessors(new InheritedBuilderScanHarnessProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "com.acme.probe.LombokInheritedDefaultsProbe",
                    """
                    package com.acme.probe;

                    import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
                    import java.util.ArrayList;
                    import java.util.Collections;
                    import java.util.List;
                    import java.util.Optional;
                    import java.util.Set;
                    import lombok.Builder;

                    public class LombokInheritedDefaultsProbe {

                      @Builder.Default
                      private int counter = 7;

                      @Builder.Default
                      private String label = "hi";

                      enum Hue {
                        A,
                        B
                      }

                      @Builder.Default
                      private Hue hue = Hue.A;

                      @Builder.Default
                      private List<String> items = new ArrayList();

                      @Builder.Default
                      private Optional<String> opt = Optional.empty();

                      @Builder.Default
                      private List<String> emptyList = Collections.emptyList();

                      @Builder.Default
                      private Set<String> emptySet = Collections.emptySet();

                      @Builder.Default
                      private java.util.Map<String, String> emptyMap = Collections.emptyMap();

                      @Builder.Default
                      @DtoBuilderDefault(inherit = false)
                      private int skipped = 99;
                    }
                    """));

    assertThat(compilation).succeeded();

    InheritedBuilderDefaultScan scan = InheritedBuilderScanHarnessProcessor.lastScan;
    assertNotNull(scan);

    Map<String, String> byName = new HashMap<>();
    for (Map.Entry<VariableElement, String> e : scan.safeInitializers().entrySet()) {
      byName.put(e.getKey().getSimpleName().toString(), e.getValue());
    }

    assertEquals("7", byName.get("counter"));
    assertEquals("\"hi\"", byName.get("label"));
    assertEquals("Hue.A", byName.get("hue"));
    assertEquals("new ArrayList()", byName.get("items"));
    assertEquals("Optional.empty()", byName.get("opt"));
    assertEquals("Collections.emptyList()", byName.get("emptyList"));
    assertEquals("Collections.emptySet()", byName.get("emptySet"));
    assertEquals("Collections.emptyMap()", byName.get("emptyMap"));

    assertFalse(byName.containsKey("skipped"));

    Set<String> imports = new HashSet<>(scan.extraImports());
    assertTrue(imports.contains("java.util.List"));
    assertTrue(imports.contains("java.util.ArrayList"));
    assertTrue(imports.contains("java.util.Optional"));
    assertTrue(imports.contains("java.util.Collections"));
  }
}

package io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ValidationStringAnnotationImportsTest {

  @Test
  void mergeFromValueStrings_mapsSimpleAnnotationNameToImport() {
    Set<String> imports = new HashSet<>();
    ValidationStringAnnotationImports.mergeFromValueStrings(
        imports, new String[] {"@Email(message = \"x\")"});
    assertTrue(imports.contains("jakarta.validation.constraints.Email"));
  }

  @Test
  void mergeFromValueStrings_skipsBlankEntries() {
    Set<String> imports = new HashSet<>();
    ValidationStringAnnotationImports.mergeFromValueStrings(imports, new String[] {"  ", "\t"});
    assertTrue(imports.isEmpty());
  }
}

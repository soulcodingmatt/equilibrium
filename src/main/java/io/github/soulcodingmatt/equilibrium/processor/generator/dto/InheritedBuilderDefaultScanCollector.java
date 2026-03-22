package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import com.sun.source.util.Trees;
import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.VariableElement;

/** Runs the per-field scan that feeds {@link InheritedBuilderDefaultScan}. */
final class InheritedBuilderDefaultScanCollector {

  private InheritedBuilderDefaultScanCollector() {}

  static InheritedBuilderDefaultScan collect(Trees trees, List<VariableElement> fields) {
    Map<VariableElement, String> inheritedDefaultInitializers = new HashMap<>();
    Set<String> extraImportsForInheritedDefaults = new HashSet<>();

    VariableInitializerTextExtractor sourceExtractor = new VariableInitializerTextExtractor(trees);
    SafeInitializerCoercion coercion =
        new SafeInitializerCoercion(trees, extraImportsForInheritedDefaults);

    for (VariableElement field : fields) {
      DtoBuilderDefault override = field.getAnnotation(DtoBuilderDefault.class);
      boolean inheritable = (override == null) || override.inherit();

      if (inheritable && LombokBuilderDefaultPresence.hasLombokBuilderDefault(field)) {
        String init = sourceExtractor.extractInitializerSubstring(field);
        if (init != null && !init.isEmpty()) {
          String safe = coercion.coerce(field, init);
          if (safe != null && !safe.isEmpty()) {
            inheritedDefaultInitializers.put(field, safe);
          }
        }
      }
    }

    return InheritedBuilderDefaultScan.ofMutable(
        inheritedDefaultInitializers, extraImportsForInheritedDefaults);
  }
}

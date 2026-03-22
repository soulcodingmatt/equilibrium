package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import java.util.Map;
import java.util.Set;
import javax.lang.model.element.VariableElement;

/**
 * Result of scanning the source AST for {@code @Builder.Default} initializers that can be copied
 * safely onto generated DTO fields.
 */
public record InheritedBuilderDefaultScan(
    Map<VariableElement, String> safeInitializers, Set<String> extraImports) {

  public static InheritedBuilderDefaultScan empty() {
    return new InheritedBuilderDefaultScan(Map.of(), Set.of());
  }

  static InheritedBuilderDefaultScan ofMutable(
      Map<VariableElement, String> map, Set<String> extra) {
    return new InheritedBuilderDefaultScan(Map.copyOf(map), Set.copyOf(extra));
  }
}

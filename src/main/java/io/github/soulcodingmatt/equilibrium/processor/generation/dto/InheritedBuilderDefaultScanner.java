package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import com.sun.source.util.Trees;
import java.util.List;
import javax.lang.model.element.VariableElement;

/**
 * Extracts safe initializer expressions from {@code @Builder.Default} fields in the source class so
 * they can be replayed on generated DTO fields.
 */
public final class InheritedBuilderDefaultScanner {

  private InheritedBuilderDefaultScanner() {}

  public static InheritedBuilderDefaultScan scan(
      Trees trees, boolean builder, List<VariableElement> fields) {
    if (!builder || trees == null) {
      return InheritedBuilderDefaultScan.empty();
    }
    return InheritedBuilderDefaultScanCollector.collect(trees, fields);
  }
}

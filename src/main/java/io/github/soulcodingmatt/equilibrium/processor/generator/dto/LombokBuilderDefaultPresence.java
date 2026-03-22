package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import javax.lang.model.element.VariableElement;

/** Whether a field carries Lombok's {@code @Builder.Default} annotation mirror. */
final class LombokBuilderDefaultPresence {

  private LombokBuilderDefaultPresence() {}

  static boolean hasLombokBuilderDefault(VariableElement field) {
    return field.getAnnotationMirrors().stream()
        .anyMatch(mirror -> mirror.getAnnotationType().toString().equals("lombok.Builder.Default"));
  }
}

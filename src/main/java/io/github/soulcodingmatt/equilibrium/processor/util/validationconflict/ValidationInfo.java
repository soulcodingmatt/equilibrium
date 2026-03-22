package io.github.soulcodingmatt.equilibrium.processor.util.validationconflict;

/** One active nested validation on an Equilibrium {@code @Validate*} annotation. */
public final class ValidationInfo {
  final String type;
  final Object annotation;

  ValidationInfo(String type, Object annotation) {
    this.type = type;
    this.annotation = annotation;
  }
}

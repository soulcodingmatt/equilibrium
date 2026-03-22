package io.github.soulcodingmatt.equilibrium.processor.validation.analysis.conflict;

/** One active nested validation on an Equilibrium {@code @Validate*} annotation. */
public final class ValidationInfo {
  /** Dispatch label for the constraint (e.g. {@code "Min"}, {@code "NotNull"}). */
  final String constraintKind;

  /** Mirror annotation instance for that constraint. */
  final Object constraintAnnotation;

  ValidationInfo(String constraintKind, Object constraintAnnotation) {
    this.constraintKind = constraintKind;
    this.constraintAnnotation = constraintAnnotation;
  }
}

package io.github.soulcodingmatt.equilibrium.processor.generator.defaultvalue;

/** Normalizes string literal values for generated Java field initializers. */
public final class DefaultValueStringLiterals {

  private DefaultValueStringLiterals() {}

  public static String processStringValue(String annotationValue) {
    if (annotationValue.startsWith("\"") && annotationValue.endsWith("\"")) {
      return annotationValue;
    }
    if (annotationValue.startsWith("\\\"") && annotationValue.endsWith("\\\"")) {
      return annotationValue;
    }
    return "\"" + escapeQuotes(annotationValue) + "\"";
  }

  static String escapeQuotes(String text) {
    return text.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}

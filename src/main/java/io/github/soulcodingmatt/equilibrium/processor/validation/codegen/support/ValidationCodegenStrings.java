package io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support;

import static io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support.ValidationCodegenConstants.*;

/** String helpers for parsing validation snippets and escaping emitted Java source. */
public final class ValidationCodegenStrings {

  private ValidationCodegenStrings() {}

  public static String escapeQuotes(String text) {
    return text.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  public static String extractAnnotationClass(String annotationString) {
    String trimmed = annotationString.trim();
    if (!trimmed.startsWith("@")) {
      return null;
    }
    String withoutAt = trimmed.substring(1);
    int parenIndex = withoutAt.indexOf('(');
    String annotationName = parenIndex > 0 ? withoutAt.substring(0, parenIndex) : withoutAt;
    return getValidationAnnotationImport(annotationName);
  }

  public static String getValidationAnnotationImport(String annotationName) {
    return switch (annotationName) {
      case "NotNull" -> IMPORT_NOT_NULL;
      case "NotEmpty" -> IMPORT_NOT_EMPTY;
      case "NotBlank" -> IMPORT_NOT_BLANK;
      case "Size" -> IMPORT_SIZE;
      case "Min" -> IMPORT_MIN;
      case "Max" -> IMPORT_MAX;
      case "DecimalMin" -> IMPORT_DECIMAL_MIN;
      case "DecimalMax" -> IMPORT_DECIMAL_MAX;
      case "Positive" -> IMPORT_POSITIVE;
      case "PositiveOrZero" -> IMPORT_POSITIVE_OR_ZERO;
      case "Negative" -> IMPORT_NEGATIVE;
      case "NegativeOrZero" -> IMPORT_NEGATIVE_OR_ZERO;
      case "Email" -> IMPORT_EMAIL;
      case "Pattern" -> IMPORT_PATTERN;
      case "Digits" -> IMPORT_DIGITS;
      case "Future" -> IMPORT_FUTURE;
      case "FutureOrPresent" -> IMPORT_FUTURE_OR_PRESENT;
      case "Past" -> IMPORT_PAST;
      case "PastOrPresent" -> IMPORT_PAST_OR_PRESENT;
      case "AssertTrue" -> IMPORT_ASSERT_TRUE;
      case "AssertFalse" -> IMPORT_ASSERT_FALSE;
      case "Valid" -> IMPORT_VALID;
      default -> null;
    };
  }
}

package io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support;

import static io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support.ValidationCodegenConstants.*;

import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;
import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Emits type-safe Jakarta validation annotations for DTO fields from {@link ValidateDto}. */
public final class ValidationDtoCodegen {

  private ValidationDtoCodegen() {}

  public static void writeTypeSafeValidations(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    writeDtoNotNull(writer, validateAnnotation);
    writeDtoNotBlank(writer, validateAnnotation);
    writeDtoSize(writer, validateAnnotation);
    writeDtoMin(writer, validateAnnotation);
    writeDtoMax(writer, validateAnnotation);
    writeDtoEmail(writer, validateAnnotation);
    writeDtoPattern(writer, validateAnnotation);
    writeDtoNotEmpty(writer, validateAnnotation);
    writeDtoPositive(writer, validateAnnotation);
    writeDtoPositiveOrZero(writer, validateAnnotation);
    writeDtoNegative(writer, validateAnnotation);
    writeDtoNegativeOrZero(writer, validateAnnotation);
    writeDtoDigits(writer, validateAnnotation);
    writeDtoPast(writer, validateAnnotation);
    writeDtoFuture(writer, validateAnnotation);
    writeDtoPastOrPresent(writer, validateAnnotation);
    writeDtoFutureOrPresent(writer, validateAnnotation);
  }

  private static void writeDtoNotNull(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    NotNull notNull = validateAnnotation.notNull();
    if (!notNull.message().isEmpty()) {
      writer.write(INDENT + ANN_NOT_NULL);
      if (!notNull.message().equals(DEFAULT_NOT_NULL)) {
        writer.write(
            WRITER_MESSAGE_OPEN
                + ValidationCodegenStrings.escapeQuotes(notNull.message())
                + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  private static void writeDtoNotBlank(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    NotBlank notBlank = validateAnnotation.notBlank();
    if (!notBlank.message().isEmpty()) {
      writer.write(INDENT + ANN_NOT_BLANK);
      if (!notBlank.message().equals(DEFAULT_NOT_BLANK)) {
        writer.write(
            WRITER_MESSAGE_OPEN
                + ValidationCodegenStrings.escapeQuotes(notBlank.message())
                + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  private static void writeDtoSize(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    Size size = validateAnnotation.size();
    if (size.min() != -1 || size.max() != -1) {
      writer.write(INDENT + ANN_SIZE);
      List<String> params = new ArrayList<>();
      if (size.min() != -1 && size.min() != 0) {
        params.add(MIN + size.min());
      }
      if (size.max() != -1 && size.max() != Integer.MAX_VALUE) {
        params.add(MAX + size.max());
      }
      if (!size.message().isEmpty() && !size.message().equals(DEFAULT_SIZE)) {
        params.add(
            MESSAGE_PARAM_PREFIX + ValidationCodegenStrings.escapeQuotes(size.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }
  }

  private static void writeDtoMin(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    Min min = validateAnnotation.min();
    if (min.value() != Long.MIN_VALUE) {
      writer.write(INDENT + ANN_MIN);
      List<String> params = new ArrayList<>();
      params.add(VALUE + min.value());
      if (!min.message().isEmpty() && !min.message().equals(DEFAULT_MIN)) {
        params.add(
            MESSAGE_PARAM_PREFIX + ValidationCodegenStrings.escapeQuotes(min.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }
  }

  private static void writeDtoMax(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    Max max = validateAnnotation.max();
    if (max.value() != Long.MAX_VALUE) {
      writer.write(INDENT + ANN_MAX);
      List<String> params = new ArrayList<>();
      params.add(VALUE + max.value());
      if (!max.message().isEmpty() && !max.message().equals(MUST_BE_LESS_THAN_OR_EQUAL_TO_VALUE)) {
        params.add(
            MESSAGE_PARAM_PREFIX + ValidationCodegenStrings.escapeQuotes(max.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }
  }

  private static void writeDtoEmail(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    Email email = validateAnnotation.email();
    if (!email.message().isEmpty()) {
      writer.write(INDENT + ANN_EMAIL);
      List<String> params = new ArrayList<>();
      if (!email.regexp().equals(EMAIL_REGEXP_DEFAULT)) {
        params.add(
            REGEXP_PARAM_PREFIX + ValidationCodegenStrings.escapeQuotes(email.regexp()) + "\"");
      }
      if (!email.message().isEmpty() && !email.message().equals(DEFAULT_EMAIL)) {
        params.add(
            MESSAGE_PARAM_PREFIX + ValidationCodegenStrings.escapeQuotes(email.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }
  }

  private static void writeDtoPattern(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    Pattern pattern = validateAnnotation.pattern();
    if (!pattern.regexp().isEmpty()) {
      writer.write(INDENT + ANN_PATTERN);
      List<String> params = new ArrayList<>();
      params.add(
          REGEXP_PARAM_PREFIX + ValidationCodegenStrings.escapeQuotes(pattern.regexp()) + "\"");
      if (!pattern.message().isEmpty() && !pattern.message().equals(DEFAULT_PATTERN)) {
        params.add(
            MESSAGE_PARAM_PREFIX + ValidationCodegenStrings.escapeQuotes(pattern.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }
  }

  private static void writeDtoNotEmpty(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    NotEmpty notEmpty = validateAnnotation.notEmpty();
    if (!notEmpty.message().isEmpty()) {
      writer.write(INDENT + ANN_NOT_EMPTY);
      if (!notEmpty.message().equals(DEFAULT_NOT_EMPTY)) {
        writer.write(
            WRITER_MESSAGE_OPEN
                + ValidationCodegenStrings.escapeQuotes(notEmpty.message())
                + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  private static void writeDtoPositive(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    Positive positive = validateAnnotation.positive();
    if (!positive.message().isEmpty()) {
      writer.write(INDENT + ANN_POSITIVE);
      if (!positive.message().equals(DEFAULT_POSITIVE)) {
        writer.write(
            WRITER_MESSAGE_OPEN
                + ValidationCodegenStrings.escapeQuotes(positive.message())
                + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  private static void writeDtoPositiveOrZero(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    PositiveOrZero positiveOrZero = validateAnnotation.positiveOrZero();
    if (!positiveOrZero.message().isEmpty()) {
      writer.write(INDENT + ANN_POSITIVE_OR_ZERO);
      if (!positiveOrZero.message().equals(DEFAULT_POSITIVE_OR_ZERO)) {
        writer.write(
            WRITER_MESSAGE_OPEN
                + ValidationCodegenStrings.escapeQuotes(positiveOrZero.message())
                + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  private static void writeDtoNegative(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    Negative negative = validateAnnotation.negative();
    if (!negative.message().isEmpty()) {
      writer.write(INDENT + ANN_NEGATIVE);
      if (!negative.message().equals(DEFAULT_NEGATIVE)) {
        writer.write(
            WRITER_MESSAGE_OPEN
                + ValidationCodegenStrings.escapeQuotes(negative.message())
                + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  private static void writeDtoNegativeOrZero(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    NegativeOrZero negativeOrZero = validateAnnotation.negativeOrZero();
    if (!negativeOrZero.message().isEmpty()) {
      writer.write(INDENT + ANN_NEGATIVE_OR_ZERO);
      if (!negativeOrZero.message().equals(MUST_BE_LESS_THAN_OR_EQUAL_TO_0)) {
        writer.write(
            WRITER_MESSAGE_OPEN
                + ValidationCodegenStrings.escapeQuotes(negativeOrZero.message())
                + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  private static void writeDtoDigits(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    Digits digits = validateAnnotation.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      writer.write(INDENT + ANN_DIGITS);
      List<String> params = new ArrayList<>();
      if (digits.integer() != -1) {
        params.add(INTEGER + digits.integer());
      }
      if (digits.fraction() != -1) {
        params.add(FRACTION + digits.fraction());
      }
      if (!digits.message().isEmpty() && !digits.message().equals(DEFAULT_DIGITS)) {
        params.add(
            MESSAGE_PARAM_PREFIX + ValidationCodegenStrings.escapeQuotes(digits.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }
  }

  private static void writeDtoPast(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    Past past = validateAnnotation.past();
    if (!past.message().isEmpty()) {
      writer.write(INDENT + ANN_PAST);
      if (!past.message().equals(DEFAULT_PAST)) {
        writer.write(
            WRITER_MESSAGE_OPEN
                + ValidationCodegenStrings.escapeQuotes(past.message())
                + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  private static void writeDtoFuture(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    Future future = validateAnnotation.future();
    if (!future.message().isEmpty()) {
      writer.write(INDENT + ANN_FUTURE);
      if (!future.message().equals(DEFAULT_FUTURE)) {
        writer.write(
            WRITER_MESSAGE_OPEN
                + ValidationCodegenStrings.escapeQuotes(future.message())
                + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  private static void writeDtoPastOrPresent(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    PastOrPresent pastOrPresent = validateAnnotation.pastOrPresent();
    if (!pastOrPresent.message().isEmpty()) {
      writer.write(INDENT + ANN_PAST_OR_PRESENT);
      if (!pastOrPresent.message().equals(DEFAULT_PAST_OR_PRESENT)) {
        writer.write(
            WRITER_MESSAGE_OPEN
                + ValidationCodegenStrings.escapeQuotes(pastOrPresent.message())
                + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  private static void writeDtoFutureOrPresent(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    FutureOrPresent futureOrPresent = validateAnnotation.futureOrPresent();
    if (!futureOrPresent.message().isEmpty()) {
      writer.write(INDENT + ANN_FUTURE_OR_PRESENT);
      if (!futureOrPresent.message().equals(DEFAULT_FUTURE_OR_PRESENT)) {
        writer.write(
            WRITER_MESSAGE_OPEN
                + ValidationCodegenStrings.escapeQuotes(futureOrPresent.message())
                + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  public static void addTypeSafeValidationImports(
      Set<String> validationImports, ValidateDto validateAnnotation) {
    ValidationTypeSafeImports.addForDto(validationImports, validateAnnotation);
  }
}

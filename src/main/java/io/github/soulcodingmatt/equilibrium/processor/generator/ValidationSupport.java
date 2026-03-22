package io.github.soulcodingmatt.equilibrium.processor.generator;

import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;
import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.*;
import io.github.soulcodingmatt.equilibrium.experimental.validation.record.*;
import io.github.soulcodingmatt.equilibrium.experimental.validation.vo.*;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;

/** Utility methods for validation-related import collection and annotation emission. */
public final class ValidationSupport {

  private static final String INDENT = "    ";

  private static final String MIN = "min = ";
  private static final String MAX = "max = ";
  private static final String VALUE = "value = ";
  private static final String INTEGER = "integer = ";
  private static final String FRACTION = "fraction = ";

  private static final String MUST_BE_LESS_THAN_OR_EQUAL_TO_VALUE =
      "must be less than or equal to {value}";
  private static final String MUST_BE_LESS_THAN_OR_EQUAL_TO_0 = "must be less than or equal to 0";

  private static final String DEFAULT_NOT_NULL = "must not be null";
  private static final String DEFAULT_NOT_BLANK = "must not be blank";
  private static final String DEFAULT_SIZE = "size must be between {min} and {max}";
  private static final String DEFAULT_MIN = "must be greater than or equal to {value}";
  private static final String DEFAULT_EMAIL = "must be a well-formed email address";
  private static final String DEFAULT_PATTERN = "must match \"{regexp}\"";
  private static final String DEFAULT_NOT_EMPTY = "must not be empty";
  private static final String DEFAULT_POSITIVE = "must be greater than 0";
  private static final String DEFAULT_POSITIVE_OR_ZERO = "must be greater than or equal to 0";
  private static final String DEFAULT_NEGATIVE = "must be less than 0";
  private static final String DEFAULT_DIGITS =
      "numeric value out of bounds (<{integer} digits>.<{fraction} digits> expected)";
  private static final String DEFAULT_PAST = "must be a date in the past";
  private static final String DEFAULT_FUTURE = "must be a date in the future";
  private static final String DEFAULT_PAST_OR_PRESENT =
      "must be a date in the past or in the present";
  private static final String DEFAULT_FUTURE_OR_PRESENT =
      "must be a date in the present or in the future";

  private static final String EMAIL_REGEXP_DEFAULT = ".*";

  private static final String MESSAGE_PARAM_PREFIX = "message = \"";
  private static final String REGEXP_PARAM_PREFIX = "regexp = \"";
  private static final String WRITER_MESSAGE_OPEN = "(" + MESSAGE_PARAM_PREFIX;
  private static final String WRITER_MESSAGE_CLOSE = "\")";

  private static final String JAKARTA_CONSTRAINTS_PACKAGE = "jakarta.validation.constraints.";
  private static final String IMPORT_NOT_NULL = JAKARTA_CONSTRAINTS_PACKAGE + "NotNull";
  private static final String IMPORT_NOT_EMPTY = JAKARTA_CONSTRAINTS_PACKAGE + "NotEmpty";
  private static final String IMPORT_NOT_BLANK = JAKARTA_CONSTRAINTS_PACKAGE + "NotBlank";
  private static final String IMPORT_SIZE = JAKARTA_CONSTRAINTS_PACKAGE + "Size";
  private static final String IMPORT_MIN = JAKARTA_CONSTRAINTS_PACKAGE + "Min";
  private static final String IMPORT_MAX = JAKARTA_CONSTRAINTS_PACKAGE + "Max";
  private static final String IMPORT_DECIMAL_MIN = JAKARTA_CONSTRAINTS_PACKAGE + "DecimalMin";
  private static final String IMPORT_DECIMAL_MAX = JAKARTA_CONSTRAINTS_PACKAGE + "DecimalMax";
  private static final String IMPORT_POSITIVE = JAKARTA_CONSTRAINTS_PACKAGE + "Positive";
  private static final String IMPORT_POSITIVE_OR_ZERO =
      JAKARTA_CONSTRAINTS_PACKAGE + "PositiveOrZero";
  private static final String IMPORT_NEGATIVE = JAKARTA_CONSTRAINTS_PACKAGE + "Negative";
  private static final String IMPORT_NEGATIVE_OR_ZERO =
      JAKARTA_CONSTRAINTS_PACKAGE + "NegativeOrZero";
  private static final String IMPORT_EMAIL = JAKARTA_CONSTRAINTS_PACKAGE + "Email";
  private static final String IMPORT_PATTERN = JAKARTA_CONSTRAINTS_PACKAGE + "Pattern";
  private static final String IMPORT_DIGITS = JAKARTA_CONSTRAINTS_PACKAGE + "Digits";
  private static final String IMPORT_FUTURE = JAKARTA_CONSTRAINTS_PACKAGE + "Future";
  private static final String IMPORT_FUTURE_OR_PRESENT =
      JAKARTA_CONSTRAINTS_PACKAGE + "FutureOrPresent";
  private static final String IMPORT_PAST = JAKARTA_CONSTRAINTS_PACKAGE + "Past";
  private static final String IMPORT_PAST_OR_PRESENT =
      JAKARTA_CONSTRAINTS_PACKAGE + "PastOrPresent";
  private static final String IMPORT_ASSERT_TRUE = JAKARTA_CONSTRAINTS_PACKAGE + "AssertTrue";
  private static final String IMPORT_ASSERT_FALSE = JAKARTA_CONSTRAINTS_PACKAGE + "AssertFalse";
  private static final String IMPORT_VALID = "jakarta.validation.Valid";

  private static final String ANN_NOT_NULL = "@NotNull";
  private static final String ANN_NOT_BLANK = "@NotBlank";
  private static final String ANN_NOT_EMPTY = "@NotEmpty";
  private static final String ANN_SIZE = "@Size";
  private static final String ANN_MIN = "@Min";
  private static final String ANN_MAX = "@Max";
  private static final String ANN_EMAIL = "@Email";
  private static final String ANN_PATTERN = "@Pattern";
  private static final String ANN_POSITIVE = "@Positive";
  private static final String ANN_POSITIVE_OR_ZERO = "@PositiveOrZero";
  private static final String ANN_NEGATIVE = "@Negative";
  private static final String ANN_NEGATIVE_OR_ZERO = "@NegativeOrZero";
  private static final String ANN_DIGITS = "@Digits";
  private static final String ANN_PAST = "@Past";
  private static final String ANN_FUTURE = "@Future";
  private static final String ANN_PAST_OR_PRESENT = "@PastOrPresent";
  private static final String ANN_FUTURE_OR_PRESENT = "@FutureOrPresent";

  private ValidationSupport() {
    throw new AssertionError("You should not be here!");
  }

  public static Set<String> collectValidationImports(List<VariableElement> fields, int dtoId) {
    Set<String> validationImports = new HashSet<>();

    for (VariableElement field : fields) {
      ValidateDto[] validateAnnotations = field.getAnnotationsByType(ValidateDto.class);
      for (ValidateDto validateAnnotation : validateAnnotations) {
        if (shouldApplyValidation(validateAnnotation, dtoId)) {
          addTypeSafeValidationImports(validationImports, validateAnnotation);

          for (String validation : validateAnnotation.value()) {
            if (!validation.trim().isEmpty()) {
              String annotationClass = extractAnnotationClass(validation);
              if (annotationClass != null) {
                validationImports.add(annotationClass);
              }
            }
          }
        }
      }
    }

    return validationImports;
  }

  public static boolean shouldApplyValidation(ValidateDto validateAnnotation, int dtoId) {
    int[] validationIds = validateAnnotation.ids();
    if (validationIds.length == 0) {
      return true;
    }
    for (int validationId : validationIds) {
      if (validationId == dtoId) {
        return true;
      }
    }
    return false;
  }

  public static void writeTypeSafeValidations(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    NotNull notNull = validateAnnotation.notNull();
    if (!notNull.message().equals("")) {
      writer.write(INDENT + ANN_NOT_NULL);
      if (!notNull.message().equals(DEFAULT_NOT_NULL)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(notNull.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    NotBlank notBlank = validateAnnotation.notBlank();
    if (!notBlank.message().equals("")) {
      writer.write(INDENT + ANN_NOT_BLANK);
      if (!notBlank.message().equals(DEFAULT_NOT_BLANK)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(notBlank.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

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
      if (!size.message().equals("") && !size.message().equals(DEFAULT_SIZE)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(size.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }

    Min min = validateAnnotation.min();
    if (min.value() != Long.MIN_VALUE) {
      writer.write(INDENT + ANN_MIN);
      List<String> params = new ArrayList<>();
      params.add(VALUE + min.value());
      if (!min.message().equals("") && !min.message().equals(DEFAULT_MIN)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(min.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }

    Max max = validateAnnotation.max();
    if (max.value() != Long.MAX_VALUE) {
      writer.write(INDENT + ANN_MAX);
      List<String> params = new ArrayList<>();
      params.add(VALUE + max.value());
      if (!max.message().equals("") && !max.message().equals(MUST_BE_LESS_THAN_OR_EQUAL_TO_VALUE)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(max.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }

    Email email = validateAnnotation.email();
    if (!email.message().equals("")) {
      writer.write(INDENT + ANN_EMAIL);
      List<String> params = new ArrayList<>();
      if (!email.regexp().equals(EMAIL_REGEXP_DEFAULT)) {
        params.add(REGEXP_PARAM_PREFIX + escapeQuotes(email.regexp()) + "\"");
      }
      if (!email.message().equals("") && !email.message().equals(DEFAULT_EMAIL)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(email.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }

    Pattern pattern = validateAnnotation.pattern();
    if (!pattern.regexp().isEmpty()) {
      writer.write(INDENT + ANN_PATTERN);
      List<String> params = new ArrayList<>();
      params.add(REGEXP_PARAM_PREFIX + escapeQuotes(pattern.regexp()) + "\"");
      if (!pattern.message().equals("") && !pattern.message().equals(DEFAULT_PATTERN)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(pattern.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }

    NotEmpty notEmpty = validateAnnotation.notEmpty();
    if (!notEmpty.message().equals("")) {
      writer.write(INDENT + ANN_NOT_EMPTY);
      if (!notEmpty.message().equals(DEFAULT_NOT_EMPTY)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(notEmpty.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    Positive positive = validateAnnotation.positive();
    if (!positive.message().equals("")) {
      writer.write(INDENT + ANN_POSITIVE);
      if (!positive.message().equals(DEFAULT_POSITIVE)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(positive.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    PositiveOrZero positiveOrZero = validateAnnotation.positiveOrZero();
    if (!positiveOrZero.message().equals("")) {
      writer.write(INDENT + ANN_POSITIVE_OR_ZERO);
      if (!positiveOrZero.message().equals(DEFAULT_POSITIVE_OR_ZERO)) {
        writer.write(
            WRITER_MESSAGE_OPEN + escapeQuotes(positiveOrZero.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    Negative negative = validateAnnotation.negative();
    if (!negative.message().equals("")) {
      writer.write(INDENT + ANN_NEGATIVE);
      if (!negative.message().equals(DEFAULT_NEGATIVE)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(negative.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    NegativeOrZero negativeOrZero = validateAnnotation.negativeOrZero();
    if (!negativeOrZero.message().equals("")) {
      writer.write(INDENT + ANN_NEGATIVE_OR_ZERO);
      if (!negativeOrZero.message().equals(MUST_BE_LESS_THAN_OR_EQUAL_TO_0)) {
        writer.write(
            WRITER_MESSAGE_OPEN + escapeQuotes(negativeOrZero.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

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
      if (!digits.message().equals("") && !digits.message().equals(DEFAULT_DIGITS)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(digits.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }

    Past past = validateAnnotation.past();
    if (!past.message().equals("")) {
      writer.write(INDENT + ANN_PAST);
      if (!past.message().equals(DEFAULT_PAST)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(past.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    Future future = validateAnnotation.future();
    if (!future.message().equals("")) {
      writer.write(INDENT + ANN_FUTURE);
      if (!future.message().equals(DEFAULT_FUTURE)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(future.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    PastOrPresent pastOrPresent = validateAnnotation.pastOrPresent();
    if (!pastOrPresent.message().equals("")) {
      writer.write(INDENT + ANN_PAST_OR_PRESENT);
      if (!pastOrPresent.message().equals(DEFAULT_PAST_OR_PRESENT)) {
        writer.write(
            WRITER_MESSAGE_OPEN + escapeQuotes(pastOrPresent.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    FutureOrPresent futureOrPresent = validateAnnotation.futureOrPresent();
    if (!futureOrPresent.message().equals("")) {
      writer.write(INDENT + ANN_FUTURE_OR_PRESENT);
      if (!futureOrPresent.message().equals(DEFAULT_FUTURE_OR_PRESENT)) {
        writer.write(
            WRITER_MESSAGE_OPEN + escapeQuotes(futureOrPresent.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  private static void addTypeSafeValidationImports(
      Set<String> validationImports, ValidateDto validateAnnotation) {
    if (!validateAnnotation.notNull().message().equals("")) {
      validationImports.add(IMPORT_NOT_NULL);
    }
    if (!validateAnnotation.notBlank().message().equals("")) {
      validationImports.add(IMPORT_NOT_BLANK);
    }
    if (!validateAnnotation.notEmpty().message().equals("")) {
      validationImports.add(IMPORT_NOT_EMPTY);
    }
    if (!validateAnnotation.positive().message().equals("")) {
      validationImports.add(IMPORT_POSITIVE);
    }
    if (!validateAnnotation.positiveOrZero().message().equals("")) {
      validationImports.add(IMPORT_POSITIVE_OR_ZERO);
    }
    if (!validateAnnotation.negative().message().equals("")) {
      validationImports.add(IMPORT_NEGATIVE);
    }
    if (!validateAnnotation.negativeOrZero().message().equals("")) {
      validationImports.add(IMPORT_NEGATIVE_OR_ZERO);
    }
    if (!validateAnnotation.past().message().equals("")) {
      validationImports.add(IMPORT_PAST);
    }
    if (!validateAnnotation.future().message().equals("")) {
      validationImports.add(IMPORT_FUTURE);
    }
    if (!validateAnnotation.pastOrPresent().message().equals("")) {
      validationImports.add(IMPORT_PAST_OR_PRESENT);
    }
    if (!validateAnnotation.futureOrPresent().message().equals("")) {
      validationImports.add(IMPORT_FUTURE_OR_PRESENT);
    }
    if (!validateAnnotation.email().message().equals("")) {
      validationImports.add(IMPORT_EMAIL);
    }

    Size size = validateAnnotation.size();
    if (size.min() != -1 || size.max() != -1) {
      validationImports.add(IMPORT_SIZE);
    }
    if (validateAnnotation.min().value() != Long.MIN_VALUE) {
      validationImports.add(IMPORT_MIN);
    }
    if (validateAnnotation.max().value() != Long.MAX_VALUE) {
      validationImports.add(IMPORT_MAX);
    }
    if (!validateAnnotation.pattern().regexp().isEmpty()) {
      validationImports.add(IMPORT_PATTERN);
    }
    Digits digits = validateAnnotation.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      validationImports.add(IMPORT_DIGITS);
    }
  }

  private static String extractAnnotationClass(String annotationString) {
    String trimmed = annotationString.trim();
    if (!trimmed.startsWith("@")) {
      return null;
    }
    String withoutAt = trimmed.substring(1);
    int parenIndex = withoutAt.indexOf('(');
    String annotationName = parenIndex > 0 ? withoutAt.substring(0, parenIndex) : withoutAt;
    return getValidationAnnotationImport(annotationName);
  }

  private static String getValidationAnnotationImport(String annotationName) {
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

  private static String escapeQuotes(String text) {
    return text.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  /**
   * Collects validation imports for Record components based on ValidateRecord annotations.
   *
   * @param fields the fields to check for ValidateRecord annotations
   * @param recordId the ID of the record being generated
   * @return set of validation import statements
   */
  public static Set<String> collectRecordValidationImports(
      List<VariableElement> fields, int recordId) {
    Set<String> validationImports = new HashSet<>();

    for (VariableElement field : fields) {
      ValidateRecord[] validateAnnotations = field.getAnnotationsByType(ValidateRecord.class);
      for (ValidateRecord validateAnnotation : validateAnnotations) {
        if (shouldApplyRecordValidation(validateAnnotation, recordId)) {
          addTypeSafeRecordValidationImports(validationImports, validateAnnotation);

          for (String validation : validateAnnotation.value()) {
            if (!validation.trim().isEmpty()) {
              String annotationClass = extractAnnotationClass(validation);
              if (annotationClass != null) {
                validationImports.add(annotationClass);
              }
            }
          }
        }
      }
    }

    return validationImports;
  }

  /**
   * Checks if a ValidateRecord annotation should be applied for the given record ID.
   *
   * @param validateAnnotation the ValidateRecord annotation
   * @param recordId the ID of the record being generated
   * @return true if validation should be applied, false otherwise
   */
  public static boolean shouldApplyRecordValidation(
      ValidateRecord validateAnnotation, int recordId) {
    int[] validationIds = validateAnnotation.ids();
    if (validationIds.length == 0) {
      return true;
    }
    for (int validationId : validationIds) {
      if (validationId == recordId) {
        return true;
      }
    }
    return false;
  }

  /**
   * Writes type-safe validation annotations for a Record component.
   *
   * @param writer the writer to write to
   * @param validateAnnotation the ValidateRecord annotation
   * @throws IOException if writing fails
   */
  public static void writeTypeSafeRecordValidations(
      Writer writer, ValidateRecord validateAnnotation) throws IOException {
    NotNull notNull = validateAnnotation.notNull();
    if (!notNull.message().equals("")) {
      writer.write(INDENT + ANN_NOT_NULL);
      if (!notNull.message().equals(DEFAULT_NOT_NULL)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(notNull.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write(" ");
    }

    NotBlank notBlank = validateAnnotation.notBlank();
    if (!notBlank.message().equals("")) {
      writer.write(ANN_NOT_BLANK);
      if (!notBlank.message().equals(DEFAULT_NOT_BLANK)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(notBlank.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write(" ");
    }

    Size size = validateAnnotation.size();
    if (size.min() != -1 || size.max() != -1) {
      writer.write(ANN_SIZE);
      List<String> params = new ArrayList<>();
      if (size.min() != -1 && size.min() != 0) {
        params.add(MIN + size.min());
      }
      if (size.max() != -1 && size.max() != Integer.MAX_VALUE) {
        params.add(MAX + size.max());
      }
      if (!size.message().equals("") && !size.message().equals(DEFAULT_SIZE)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(size.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write(" ");
    }

    Min min = validateAnnotation.min();
    if (min.value() != Long.MIN_VALUE) {
      writer.write(ANN_MIN);
      List<String> params = new ArrayList<>();
      params.add(VALUE + min.value());
      if (!min.message().equals("") && !min.message().equals(DEFAULT_MIN)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(min.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write(" ");
    }

    Max max = validateAnnotation.max();
    if (max.value() != Long.MAX_VALUE) {
      writer.write(ANN_MAX);
      List<String> params = new ArrayList<>();
      params.add(VALUE + max.value());
      if (!max.message().equals("") && !max.message().equals(MUST_BE_LESS_THAN_OR_EQUAL_TO_VALUE)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(max.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write(" ");
    }

    Email email = validateAnnotation.email();
    if (!email.message().equals("")) {
      writer.write(ANN_EMAIL);
      List<String> params = new ArrayList<>();
      if (!email.regexp().equals(EMAIL_REGEXP_DEFAULT)) {
        params.add(REGEXP_PARAM_PREFIX + escapeQuotes(email.regexp()) + "\"");
      }
      if (!email.message().equals("") && !email.message().equals(DEFAULT_EMAIL)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(email.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write(" ");
    }

    Pattern pattern = validateAnnotation.pattern();
    if (!pattern.regexp().isEmpty()) {
      writer.write(ANN_PATTERN);
      List<String> params = new ArrayList<>();
      params.add(REGEXP_PARAM_PREFIX + escapeQuotes(pattern.regexp()) + "\"");
      if (!pattern.message().equals("") && !pattern.message().equals(DEFAULT_PATTERN)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(pattern.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write(" ");
    }

    NotEmpty notEmpty = validateAnnotation.notEmpty();
    if (!notEmpty.message().equals("")) {
      writer.write(ANN_NOT_EMPTY);
      if (!notEmpty.message().equals(DEFAULT_NOT_EMPTY)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(notEmpty.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write(" ");
    }

    Positive positive = validateAnnotation.positive();
    if (!positive.message().equals("")) {
      writer.write(ANN_POSITIVE);
      if (!positive.message().equals(DEFAULT_POSITIVE)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(positive.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write(" ");
    }

    PositiveOrZero positiveOrZero = validateAnnotation.positiveOrZero();
    if (!positiveOrZero.message().equals("")) {
      writer.write(ANN_POSITIVE_OR_ZERO);
      if (!positiveOrZero.message().equals(DEFAULT_POSITIVE_OR_ZERO)) {
        writer.write(
            WRITER_MESSAGE_OPEN + escapeQuotes(positiveOrZero.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write(" ");
    }

    Negative negative = validateAnnotation.negative();
    if (!negative.message().equals("")) {
      writer.write(ANN_NEGATIVE);
      if (!negative.message().equals(DEFAULT_NEGATIVE)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(negative.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write(" ");
    }

    NegativeOrZero negativeOrZero = validateAnnotation.negativeOrZero();
    if (!negativeOrZero.message().equals("")) {
      writer.write(ANN_NEGATIVE_OR_ZERO);
      if (!negativeOrZero.message().equals(MUST_BE_LESS_THAN_OR_EQUAL_TO_0)) {
        writer.write(
            WRITER_MESSAGE_OPEN + escapeQuotes(negativeOrZero.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write(" ");
    }

    Digits digits = validateAnnotation.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      writer.write(ANN_DIGITS);
      List<String> params = new ArrayList<>();
      if (digits.integer() != -1) {
        params.add(INTEGER + digits.integer());
      }
      if (digits.fraction() != -1) {
        params.add(FRACTION + digits.fraction());
      }
      if (!digits.message().equals("") && !digits.message().equals(DEFAULT_DIGITS)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(digits.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write(" ");
    }

    Past past = validateAnnotation.past();
    if (!past.message().equals("")) {
      writer.write(ANN_PAST);
      if (!past.message().equals(DEFAULT_PAST)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(past.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write(" ");
    }

    Future future = validateAnnotation.future();
    if (!future.message().equals("")) {
      writer.write(ANN_FUTURE);
      if (!future.message().equals(DEFAULT_FUTURE)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(future.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write(" ");
    }

    PastOrPresent pastOrPresent = validateAnnotation.pastOrPresent();
    if (!pastOrPresent.message().equals("")) {
      writer.write(ANN_PAST_OR_PRESENT);
      if (!pastOrPresent.message().equals(DEFAULT_PAST_OR_PRESENT)) {
        writer.write(
            WRITER_MESSAGE_OPEN + escapeQuotes(pastOrPresent.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write(" ");
    }

    FutureOrPresent futureOrPresent = validateAnnotation.futureOrPresent();
    if (!futureOrPresent.message().equals("")) {
      writer.write(ANN_FUTURE_OR_PRESENT);
      if (!futureOrPresent.message().equals(DEFAULT_FUTURE_OR_PRESENT)) {
        writer.write(
            WRITER_MESSAGE_OPEN + escapeQuotes(futureOrPresent.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write(" ");
    }
  }

  /** Adds type-safe validation imports for Record validations. */
  private static void addTypeSafeRecordValidationImports(
      Set<String> validationImports, ValidateRecord validateAnnotation) {
    if (!validateAnnotation.notNull().message().equals("")) {
      validationImports.add(IMPORT_NOT_NULL);
    }
    if (!validateAnnotation.notBlank().message().equals("")) {
      validationImports.add(IMPORT_NOT_BLANK);
    }
    if (!validateAnnotation.notEmpty().message().equals("")) {
      validationImports.add(IMPORT_NOT_EMPTY);
    }
    if (!validateAnnotation.positive().message().equals("")) {
      validationImports.add(IMPORT_POSITIVE);
    }
    if (!validateAnnotation.positiveOrZero().message().equals("")) {
      validationImports.add(IMPORT_POSITIVE_OR_ZERO);
    }
    if (!validateAnnotation.negative().message().equals("")) {
      validationImports.add(IMPORT_NEGATIVE);
    }
    if (!validateAnnotation.negativeOrZero().message().equals("")) {
      validationImports.add(IMPORT_NEGATIVE_OR_ZERO);
    }
    if (!validateAnnotation.past().message().equals("")) {
      validationImports.add(IMPORT_PAST);
    }
    if (!validateAnnotation.future().message().equals("")) {
      validationImports.add(IMPORT_FUTURE);
    }
    if (!validateAnnotation.pastOrPresent().message().equals("")) {
      validationImports.add(IMPORT_PAST_OR_PRESENT);
    }
    if (!validateAnnotation.futureOrPresent().message().equals("")) {
      validationImports.add(IMPORT_FUTURE_OR_PRESENT);
    }
    if (!validateAnnotation.email().message().equals("")) {
      validationImports.add(IMPORT_EMAIL);
    }

    Size size = validateAnnotation.size();
    if (size.min() != -1 || size.max() != -1) {
      validationImports.add(IMPORT_SIZE);
    }
    if (validateAnnotation.min().value() != Long.MIN_VALUE) {
      validationImports.add(IMPORT_MIN);
    }
    if (validateAnnotation.max().value() != Long.MAX_VALUE) {
      validationImports.add(IMPORT_MAX);
    }
    if (!validateAnnotation.pattern().regexp().isEmpty()) {
      validationImports.add(IMPORT_PATTERN);
    }
    Digits digits = validateAnnotation.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      validationImports.add(IMPORT_DIGITS);
    }
  }

  /**
   * Collects validation imports for VO fields based on ValidateVo annotations.
   *
   * @param fields the fields to check for ValidateVo annotations
   * @param voId the ID of the VO being generated
   * @return set of validation import statements
   */
  public static Set<String> collectVoValidationImports(List<VariableElement> fields, int voId) {
    Set<String> validationImports = new HashSet<>();

    for (VariableElement field : fields) {
      ValidateVo[] validateAnnotations = field.getAnnotationsByType(ValidateVo.class);
      for (ValidateVo validateAnnotation : validateAnnotations) {
        if (shouldApplyVoValidation(validateAnnotation, voId)) {
          addTypeSafeVoValidationImports(validationImports, validateAnnotation);

          for (String validation : validateAnnotation.value()) {
            if (!validation.trim().isEmpty()) {
              String annotationClass = extractAnnotationClass(validation);
              if (annotationClass != null) {
                validationImports.add(annotationClass);
              }
            }
          }
        }
      }
    }

    return validationImports;
  }

  /**
   * Checks if a ValidateVo annotation should be applied for the given VO ID.
   *
   * @param validateAnnotation the ValidateVo annotation
   * @param voId the ID of the VO being generated
   * @return true if validation should be applied, false otherwise
   */
  public static boolean shouldApplyVoValidation(ValidateVo validateAnnotation, int voId) {
    int[] validationIds = validateAnnotation.ids();
    if (validationIds.length == 0) {
      return true;
    }
    for (int validationId : validationIds) {
      if (validationId == voId) {
        return true;
      }
    }
    return false;
  }

  /**
   * Writes type-safe validation annotations for a VO field.
   *
   * @param writer the writer to write to
   * @param validateAnnotation the ValidateVo annotation
   * @throws IOException if writing fails
   */
  public static void writeTypeSafeVoValidations(Writer writer, ValidateVo validateAnnotation)
      throws IOException {
    NotNull notNull = validateAnnotation.notNull();
    if (!notNull.message().equals("")) {
      writer.write(INDENT + ANN_NOT_NULL);
      if (!notNull.message().equals(DEFAULT_NOT_NULL)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(notNull.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    NotBlank notBlank = validateAnnotation.notBlank();
    if (!notBlank.message().equals("")) {
      writer.write(INDENT + ANN_NOT_BLANK);
      if (!notBlank.message().equals(DEFAULT_NOT_BLANK)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(notBlank.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

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
      if (!size.message().equals("") && !size.message().equals(DEFAULT_SIZE)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(size.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }

    Min min = validateAnnotation.min();
    if (min.value() != Long.MIN_VALUE) {
      writer.write(INDENT + ANN_MIN);
      List<String> params = new ArrayList<>();
      params.add(VALUE + min.value());
      if (!min.message().equals("") && !min.message().equals(DEFAULT_MIN)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(min.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }

    Max max = validateAnnotation.max();
    if (max.value() != Long.MAX_VALUE) {
      writer.write(INDENT + ANN_MAX);
      List<String> params = new ArrayList<>();
      params.add(VALUE + max.value());
      if (!max.message().equals("") && !max.message().equals(MUST_BE_LESS_THAN_OR_EQUAL_TO_VALUE)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(max.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }

    Email email = validateAnnotation.email();
    if (!email.message().equals("")) {
      writer.write(INDENT + ANN_EMAIL);
      List<String> params = new ArrayList<>();
      if (!email.regexp().equals(EMAIL_REGEXP_DEFAULT)) {
        params.add(REGEXP_PARAM_PREFIX + escapeQuotes(email.regexp()) + "\"");
      }
      if (!email.message().equals("") && !email.message().equals(DEFAULT_EMAIL)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(email.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }

    Pattern pattern = validateAnnotation.pattern();
    if (!pattern.regexp().isEmpty()) {
      writer.write(INDENT + ANN_PATTERN);
      List<String> params = new ArrayList<>();
      params.add(REGEXP_PARAM_PREFIX + escapeQuotes(pattern.regexp()) + "\"");
      if (!pattern.message().equals("") && !pattern.message().equals(DEFAULT_PATTERN)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(pattern.message()) + "\"");
      }
      writer.write("(" + String.join(", ", params) + ")");
      writer.write("\n");
    }

    NotEmpty notEmpty = validateAnnotation.notEmpty();
    if (!notEmpty.message().equals("")) {
      writer.write(INDENT + ANN_NOT_EMPTY);
      if (!notEmpty.message().equals(DEFAULT_NOT_EMPTY)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(notEmpty.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    Positive positive = validateAnnotation.positive();
    if (!positive.message().equals("")) {
      writer.write(INDENT + ANN_POSITIVE);
      if (!positive.message().equals(DEFAULT_POSITIVE)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(positive.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    PositiveOrZero positiveOrZero = validateAnnotation.positiveOrZero();
    if (!positiveOrZero.message().equals("")) {
      writer.write(INDENT + ANN_POSITIVE_OR_ZERO);
      if (!positiveOrZero.message().equals(DEFAULT_POSITIVE_OR_ZERO)) {
        writer.write(
            WRITER_MESSAGE_OPEN + escapeQuotes(positiveOrZero.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    Negative negative = validateAnnotation.negative();
    if (!negative.message().equals("")) {
      writer.write(INDENT + ANN_NEGATIVE);
      if (!negative.message().equals(DEFAULT_NEGATIVE)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(negative.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    NegativeOrZero negativeOrZero = validateAnnotation.negativeOrZero();
    if (!negativeOrZero.message().equals("")) {
      writer.write(INDENT + ANN_NEGATIVE_OR_ZERO);
      if (!negativeOrZero.message().equals(MUST_BE_LESS_THAN_OR_EQUAL_TO_0)) {
        writer.write(
            WRITER_MESSAGE_OPEN + escapeQuotes(negativeOrZero.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

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
      if (!digits.message().equals("") && !digits.message().equals(DEFAULT_DIGITS)) {
        params.add(MESSAGE_PARAM_PREFIX + escapeQuotes(digits.message()) + "\"");
      }
      if (!params.isEmpty()) {
        writer.write("(" + String.join(", ", params) + ")");
      }
      writer.write("\n");
    }

    Past past = validateAnnotation.past();
    if (!past.message().equals("")) {
      writer.write(INDENT + ANN_PAST);
      if (!past.message().equals(DEFAULT_PAST)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(past.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    Future future = validateAnnotation.future();
    if (!future.message().equals("")) {
      writer.write(INDENT + ANN_FUTURE);
      if (!future.message().equals(DEFAULT_FUTURE)) {
        writer.write(WRITER_MESSAGE_OPEN + escapeQuotes(future.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    PastOrPresent pastOrPresent = validateAnnotation.pastOrPresent();
    if (!pastOrPresent.message().equals("")) {
      writer.write(INDENT + ANN_PAST_OR_PRESENT);
      if (!pastOrPresent.message().equals(DEFAULT_PAST_OR_PRESENT)) {
        writer.write(
            WRITER_MESSAGE_OPEN + escapeQuotes(pastOrPresent.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }

    FutureOrPresent futureOrPresent = validateAnnotation.futureOrPresent();
    if (!futureOrPresent.message().equals("")) {
      writer.write(INDENT + ANN_FUTURE_OR_PRESENT);
      if (!futureOrPresent.message().equals(DEFAULT_FUTURE_OR_PRESENT)) {
        writer.write(
            WRITER_MESSAGE_OPEN + escapeQuotes(futureOrPresent.message()) + WRITER_MESSAGE_CLOSE);
      }
      writer.write("\n");
    }
  }

  /** Adds type-safe validation imports for VO validations. */
  private static void addTypeSafeVoValidationImports(
      Set<String> validationImports, ValidateVo validateAnnotation) {
    if (!validateAnnotation.notNull().message().equals("")) {
      validationImports.add(IMPORT_NOT_NULL);
    }
    if (!validateAnnotation.notBlank().message().equals("")) {
      validationImports.add(IMPORT_NOT_BLANK);
    }
    if (!validateAnnotation.notEmpty().message().equals("")) {
      validationImports.add(IMPORT_NOT_EMPTY);
    }
    if (!validateAnnotation.positive().message().equals("")) {
      validationImports.add(IMPORT_POSITIVE);
    }
    if (!validateAnnotation.positiveOrZero().message().equals("")) {
      validationImports.add(IMPORT_POSITIVE_OR_ZERO);
    }
    if (!validateAnnotation.negative().message().equals("")) {
      validationImports.add(IMPORT_NEGATIVE);
    }
    if (!validateAnnotation.negativeOrZero().message().equals("")) {
      validationImports.add(IMPORT_NEGATIVE_OR_ZERO);
    }
    if (!validateAnnotation.past().message().equals("")) {
      validationImports.add(IMPORT_PAST);
    }
    if (!validateAnnotation.future().message().equals("")) {
      validationImports.add(IMPORT_FUTURE);
    }
    if (!validateAnnotation.pastOrPresent().message().equals("")) {
      validationImports.add(IMPORT_PAST_OR_PRESENT);
    }
    if (!validateAnnotation.futureOrPresent().message().equals("")) {
      validationImports.add(IMPORT_FUTURE_OR_PRESENT);
    }
    if (!validateAnnotation.email().message().equals("")) {
      validationImports.add(IMPORT_EMAIL);
    }

    Size size = validateAnnotation.size();
    if (size.min() != -1 || size.max() != -1) {
      validationImports.add(IMPORT_SIZE);
    }
    if (validateAnnotation.min().value() != Long.MIN_VALUE) {
      validationImports.add(IMPORT_MIN);
    }
    if (validateAnnotation.max().value() != Long.MAX_VALUE) {
      validationImports.add(IMPORT_MAX);
    }
    if (!validateAnnotation.pattern().regexp().isEmpty()) {
      validationImports.add(IMPORT_PATTERN);
    }
    Digits digits = validateAnnotation.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      validationImports.add(IMPORT_DIGITS);
    }
  }
}

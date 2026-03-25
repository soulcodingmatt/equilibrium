package io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support;

/** Shared literals for Jakarta validation import paths and emitted annotation text. */
public final class ValidationCodegenConstants {

  public static final String INDENT = "    ";

  public static final String MIN = "min = ";
  public static final String MAX = "max = ";
  public static final String VALUE = "value = ";
  public static final String INTEGER = "integer = ";
  public static final String FRACTION = "fraction = ";

  public static final String MUST_BE_LESS_THAN_OR_EQUAL_TO_VALUE =
      "must be less than or equal to {value}";
  public static final String MUST_BE_LESS_THAN_OR_EQUAL_TO_0 = "must be less than or equal to 0";

  public static final String DEFAULT_NOT_NULL = "must not be null";
  public static final String DEFAULT_NOT_BLANK = "must not be blank";
  public static final String DEFAULT_SIZE = "size must be between {min} and {max}";
  public static final String DEFAULT_MIN = "must be greater than or equal to {value}";
  public static final String DEFAULT_EMAIL = "must be a well-formed email address";
  public static final String DEFAULT_PATTERN = "must match \"{regexp}\"";
  public static final String DEFAULT_NOT_EMPTY = "must not be empty";
  public static final String DEFAULT_POSITIVE = "must be greater than 0";
  public static final String DEFAULT_POSITIVE_OR_ZERO = "must be greater than or equal to 0";
  public static final String DEFAULT_NEGATIVE = "must be less than 0";
  public static final String DEFAULT_DIGITS =
      "numeric value out of bounds (<{integer} digits>.<{fraction} digits> expected)";
  public static final String DEFAULT_PAST = "must be a date in the past";
  public static final String DEFAULT_FUTURE = "must be a date in the future";
  public static final String DEFAULT_PAST_OR_PRESENT =
      "must be a date in the past or in the present";
  public static final String DEFAULT_FUTURE_OR_PRESENT =
      "must be a date in the present or in the future";

  public static final String EMAIL_REGEXP_DEFAULT = ".*";

  public static final String MESSAGE_PARAM_PREFIX = "message = \"";
  public static final String REGEXP_PARAM_PREFIX = "regexp = \"";
  public static final String WRITER_MESSAGE_OPEN = "(" + MESSAGE_PARAM_PREFIX;
  public static final String WRITER_MESSAGE_CLOSE = "\")";

  public static final String JAKARTA_CONSTRAINTS_PACKAGE = "jakarta.validation.constraints.";
  public static final String IMPORT_NOT_NULL = JAKARTA_CONSTRAINTS_PACKAGE + "NotNull";
  public static final String IMPORT_NOT_EMPTY = JAKARTA_CONSTRAINTS_PACKAGE + "NotEmpty";
  public static final String IMPORT_NOT_BLANK = JAKARTA_CONSTRAINTS_PACKAGE + "NotBlank";
  public static final String IMPORT_SIZE = JAKARTA_CONSTRAINTS_PACKAGE + "Size";
  public static final String IMPORT_MIN = JAKARTA_CONSTRAINTS_PACKAGE + "Min";
  public static final String IMPORT_MAX = JAKARTA_CONSTRAINTS_PACKAGE + "Max";
  public static final String IMPORT_DECIMAL_MIN = JAKARTA_CONSTRAINTS_PACKAGE + "DecimalMin";
  public static final String IMPORT_DECIMAL_MAX = JAKARTA_CONSTRAINTS_PACKAGE + "DecimalMax";
  public static final String IMPORT_POSITIVE = JAKARTA_CONSTRAINTS_PACKAGE + "Positive";
  public static final String IMPORT_POSITIVE_OR_ZERO =
      JAKARTA_CONSTRAINTS_PACKAGE + "PositiveOrZero";
  public static final String IMPORT_NEGATIVE = JAKARTA_CONSTRAINTS_PACKAGE + "Negative";
  public static final String IMPORT_NEGATIVE_OR_ZERO =
      JAKARTA_CONSTRAINTS_PACKAGE + "NegativeOrZero";
  public static final String IMPORT_EMAIL = JAKARTA_CONSTRAINTS_PACKAGE + "Email";
  public static final String IMPORT_PATTERN = JAKARTA_CONSTRAINTS_PACKAGE + "Pattern";
  public static final String IMPORT_DIGITS = JAKARTA_CONSTRAINTS_PACKAGE + "Digits";
  public static final String IMPORT_FUTURE = JAKARTA_CONSTRAINTS_PACKAGE + "Future";
  public static final String IMPORT_FUTURE_OR_PRESENT =
      JAKARTA_CONSTRAINTS_PACKAGE + "FutureOrPresent";
  public static final String IMPORT_PAST = JAKARTA_CONSTRAINTS_PACKAGE + "Past";
  public static final String IMPORT_PAST_OR_PRESENT = JAKARTA_CONSTRAINTS_PACKAGE + "PastOrPresent";
  public static final String IMPORT_ASSERT_TRUE = JAKARTA_CONSTRAINTS_PACKAGE + "AssertTrue";
  public static final String IMPORT_ASSERT_FALSE = JAKARTA_CONSTRAINTS_PACKAGE + "AssertFalse";
  public static final String IMPORT_VALID = "jakarta.validation.Valid";

  public static final String ANN_NOT_NULL = "@NotNull";
  public static final String ANN_NOT_BLANK = "@NotBlank";
  public static final String ANN_NOT_EMPTY = "@NotEmpty";
  public static final String ANN_SIZE = "@Size";
  public static final String ANN_MIN = "@Min";
  public static final String ANN_MAX = "@Max";
  public static final String ANN_EMAIL = "@Email";
  public static final String ANN_PATTERN = "@Pattern";
  public static final String ANN_POSITIVE = "@Positive";
  public static final String ANN_POSITIVE_OR_ZERO = "@PositiveOrZero";
  public static final String ANN_NEGATIVE = "@Negative";
  public static final String ANN_NEGATIVE_OR_ZERO = "@NegativeOrZero";
  public static final String ANN_DIGITS = "@Digits";
  public static final String ANN_PAST = "@Past";
  public static final String ANN_FUTURE = "@Future";
  public static final String ANN_PAST_OR_PRESENT = "@PastOrPresent";
  public static final String ANN_FUTURE_OR_PRESENT = "@FutureOrPresent";

  private ValidationCodegenConstants() {}
}

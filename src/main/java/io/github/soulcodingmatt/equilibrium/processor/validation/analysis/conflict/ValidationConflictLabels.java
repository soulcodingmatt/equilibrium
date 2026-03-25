package io.github.soulcodingmatt.equilibrium.processor.validation.analysis.conflict;

/**
 * Label strings shared by validation conflict analysis; {@link
 * io.github.soulcodingmatt.equilibrium.processor.validation.analysis.ValidationConflictUtil}
 * re-exports these as its public API.
 */
public final class ValidationConflictLabels {

  public static final String NEGATIVE_OR_ZERO = "NegativeOrZero";
  public static final String FUTURE = "Future";
  public static final String PAST_OR_PRESENT = "PastOrPresent";
  public static final String POSITIVE = "Positive";
  public static final String POSITIVE_OR_ZERO = "PositiveOrZero";
  public static final String NEGATIVE = "Negative";
  public static final String DIGITS = "Digits";
  public static final String PAST = "Past";
  public static final String FUTURE_OR_PRESENT = "FutureOrPresent";
  public static final String IS_OF_TYPE = "' is of type ";
  public static final String NOT_EMPTY = "NotEmpty";
  public static final String MIN = "': @Min(";
  public static final String MAX = "': @Max(";
  public static final String FIELD = "Field '";
  public static final String PATTERN = "Pattern";
  public static final String NOT_BLANK = "NotBlank";
  public static final String EMAIL = "Email";
  public static final String NOT_NULL = "NotNull";

  private ValidationConflictLabels() {}
}

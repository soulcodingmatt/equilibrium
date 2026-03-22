package io.github.soulcodingmatt.equilibrium.processor.util.validationconflict;

import static io.github.soulcodingmatt.equilibrium.processor.util.validationconflict.ValidationConflictLabels.*;

import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;
import io.github.soulcodingmatt.equilibrium.experimental.validation.record.ValidateRecord;
import java.util.ArrayList;
import java.util.List;

/** Collects active nested Jakarta validations from {@link ValidateRecord}. */
final class ValidationRecordActiveAnnotations {

  private ValidationRecordActiveAnnotations() {}

  static List<ValidationInfo> collect(ValidateRecord validateRecord) {
    List<ValidationInfo> validations = new ArrayList<>();
    addNotNull(validations, validateRecord);
    addNotBlank(validations, validateRecord);
    addNotEmpty(validations, validateRecord);
    addSize(validations, validateRecord);
    addMin(validations, validateRecord);
    addMax(validations, validateRecord);
    addEmail(validations, validateRecord);
    addPattern(validations, validateRecord);
    addPositive(validations, validateRecord);
    addPositiveOrZero(validations, validateRecord);
    addNegative(validations, validateRecord);
    addNegativeOrZero(validations, validateRecord);
    addDigits(validations, validateRecord);
    addPast(validations, validateRecord);
    addFuture(validations, validateRecord);
    addPastOrPresent(validations, validateRecord);
    addFutureOrPresent(validations, validateRecord);
    return validations;
  }

  private static void addNotNull(List<ValidationInfo> out, ValidateRecord r) {
    NotNull notNull = r.notNull();
    if (!notNull.message().isEmpty()) {
      out.add(new ValidationInfo(NOT_NULL, notNull));
    }
  }

  private static void addNotBlank(List<ValidationInfo> out, ValidateRecord r) {
    NotBlank notBlank = r.notBlank();
    if (!notBlank.message().isEmpty()) {
      out.add(new ValidationInfo(NOT_BLANK, notBlank));
    }
  }

  private static void addNotEmpty(List<ValidationInfo> out, ValidateRecord r) {
    NotEmpty notEmpty = r.notEmpty();
    if (!notEmpty.message().isEmpty()) {
      out.add(new ValidationInfo(NOT_EMPTY, notEmpty));
    }
  }

  private static void addSize(List<ValidationInfo> out, ValidateRecord r) {
    Size size = r.size();
    if (size.min() != -1 || size.max() != -1) {
      out.add(new ValidationInfo("Size", size));
    }
  }

  private static void addMin(List<ValidationInfo> out, ValidateRecord r) {
    Min min = r.min();
    if (min.value() != Long.MIN_VALUE) {
      out.add(new ValidationInfo("Min", min));
    }
  }

  private static void addMax(List<ValidationInfo> out, ValidateRecord r) {
    Max max = r.max();
    if (max.value() != Long.MAX_VALUE) {
      out.add(new ValidationInfo("Max", max));
    }
  }

  private static void addEmail(List<ValidationInfo> out, ValidateRecord r) {
    Email email = r.email();
    if (!email.message().isEmpty()) {
      out.add(new ValidationInfo(EMAIL, email));
    }
  }

  private static void addPattern(List<ValidationInfo> out, ValidateRecord r) {
    Pattern pattern = r.pattern();
    if (!pattern.regexp().isEmpty()) {
      out.add(new ValidationInfo(PATTERN, pattern));
    }
  }

  private static void addPositive(List<ValidationInfo> out, ValidateRecord r) {
    Positive positive = r.positive();
    if (!positive.message().isEmpty()) {
      out.add(new ValidationInfo(POSITIVE, positive));
    }
  }

  private static void addPositiveOrZero(List<ValidationInfo> out, ValidateRecord r) {
    PositiveOrZero positiveOrZero = r.positiveOrZero();
    if (!positiveOrZero.message().isEmpty()) {
      out.add(new ValidationInfo(POSITIVE_OR_ZERO, positiveOrZero));
    }
  }

  private static void addNegative(List<ValidationInfo> out, ValidateRecord r) {
    Negative negative = r.negative();
    if (!negative.message().isEmpty()) {
      out.add(new ValidationInfo(NEGATIVE, negative));
    }
  }

  private static void addNegativeOrZero(List<ValidationInfo> out, ValidateRecord r) {
    NegativeOrZero negativeOrZero = r.negativeOrZero();
    if (!negativeOrZero.message().isEmpty()) {
      out.add(new ValidationInfo(NEGATIVE_OR_ZERO, negativeOrZero));
    }
  }

  private static void addDigits(List<ValidationInfo> out, ValidateRecord r) {
    Digits digits = r.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      out.add(new ValidationInfo(DIGITS, digits));
    }
  }

  private static void addPast(List<ValidationInfo> out, ValidateRecord r) {
    Past past = r.past();
    if (!past.message().isEmpty()) {
      out.add(new ValidationInfo(PAST, past));
    }
  }

  private static void addFuture(List<ValidationInfo> out, ValidateRecord r) {
    Future future = r.future();
    if (!future.message().isEmpty()) {
      out.add(new ValidationInfo(FUTURE, future));
    }
  }

  private static void addPastOrPresent(List<ValidationInfo> out, ValidateRecord r) {
    PastOrPresent pastOrPresent = r.pastOrPresent();
    if (!pastOrPresent.message().isEmpty()) {
      out.add(new ValidationInfo(PAST_OR_PRESENT, pastOrPresent));
    }
  }

  private static void addFutureOrPresent(List<ValidationInfo> out, ValidateRecord r) {
    FutureOrPresent futureOrPresent = r.futureOrPresent();
    if (!futureOrPresent.message().isEmpty()) {
      out.add(new ValidationInfo(FUTURE_OR_PRESENT, futureOrPresent));
    }
  }
}

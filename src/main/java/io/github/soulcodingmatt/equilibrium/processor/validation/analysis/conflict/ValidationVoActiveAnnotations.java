package io.github.soulcodingmatt.equilibrium.processor.validation.analysis.conflict;

import static io.github.soulcodingmatt.equilibrium.processor.validation.analysis.conflict.ValidationConflictLabels.*;

import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;
import io.github.soulcodingmatt.equilibrium.experimental.validation.vo.ValidateVo;
import java.util.ArrayList;
import java.util.List;

/** Collects active nested Jakarta validations from {@link ValidateVo}. */
final class ValidationVoActiveAnnotations {

  private ValidationVoActiveAnnotations() {}

  static List<ValidationInfo> collect(ValidateVo validateVo) {
    List<ValidationInfo> validations = new ArrayList<>();
    addNotNull(validations, validateVo);
    addNotBlank(validations, validateVo);
    addNotEmpty(validations, validateVo);
    addSize(validations, validateVo);
    addMin(validations, validateVo);
    addMax(validations, validateVo);
    addEmail(validations, validateVo);
    addPattern(validations, validateVo);
    addPositive(validations, validateVo);
    addPositiveOrZero(validations, validateVo);
    addNegative(validations, validateVo);
    addNegativeOrZero(validations, validateVo);
    addDigits(validations, validateVo);
    addPast(validations, validateVo);
    addFuture(validations, validateVo);
    addPastOrPresent(validations, validateVo);
    addFutureOrPresent(validations, validateVo);
    return validations;
  }

  private static void addNotNull(List<ValidationInfo> out, ValidateVo v) {
    NotNull notNull = v.notNull();
    if (!notNull.message().isEmpty()) {
      out.add(new ValidationInfo(NOT_NULL, notNull));
    }
  }

  private static void addNotBlank(List<ValidationInfo> out, ValidateVo v) {
    NotBlank notBlank = v.notBlank();
    if (!notBlank.message().isEmpty()) {
      out.add(new ValidationInfo(NOT_BLANK, notBlank));
    }
  }

  private static void addNotEmpty(List<ValidationInfo> out, ValidateVo v) {
    NotEmpty notEmpty = v.notEmpty();
    if (!notEmpty.message().isEmpty()) {
      out.add(new ValidationInfo(NOT_EMPTY, notEmpty));
    }
  }

  private static void addSize(List<ValidationInfo> out, ValidateVo v) {
    Size size = v.size();
    if (size.min() != -1 || size.max() != -1) {
      out.add(new ValidationInfo("Size", size));
    }
  }

  private static void addMin(List<ValidationInfo> out, ValidateVo v) {
    Min min = v.min();
    if (min.value() != Long.MIN_VALUE) {
      out.add(new ValidationInfo("Min", min));
    }
  }

  private static void addMax(List<ValidationInfo> out, ValidateVo v) {
    Max max = v.max();
    if (max.value() != Long.MAX_VALUE) {
      out.add(new ValidationInfo("Max", max));
    }
  }

  private static void addEmail(List<ValidationInfo> out, ValidateVo v) {
    Email email = v.email();
    if (!email.message().isEmpty()) {
      out.add(new ValidationInfo(EMAIL, email));
    }
  }

  private static void addPattern(List<ValidationInfo> out, ValidateVo v) {
    Pattern pattern = v.pattern();
    if (!pattern.regexp().isEmpty()) {
      out.add(new ValidationInfo(PATTERN, pattern));
    }
  }

  private static void addPositive(List<ValidationInfo> out, ValidateVo v) {
    Positive positive = v.positive();
    if (!positive.message().isEmpty()) {
      out.add(new ValidationInfo(POSITIVE, positive));
    }
  }

  private static void addPositiveOrZero(List<ValidationInfo> out, ValidateVo v) {
    PositiveOrZero positiveOrZero = v.positiveOrZero();
    if (!positiveOrZero.message().isEmpty()) {
      out.add(new ValidationInfo(POSITIVE_OR_ZERO, positiveOrZero));
    }
  }

  private static void addNegative(List<ValidationInfo> out, ValidateVo v) {
    Negative negative = v.negative();
    if (!negative.message().isEmpty()) {
      out.add(new ValidationInfo(NEGATIVE, negative));
    }
  }

  private static void addNegativeOrZero(List<ValidationInfo> out, ValidateVo v) {
    NegativeOrZero negativeOrZero = v.negativeOrZero();
    if (!negativeOrZero.message().isEmpty()) {
      out.add(new ValidationInfo(NEGATIVE_OR_ZERO, negativeOrZero));
    }
  }

  private static void addDigits(List<ValidationInfo> out, ValidateVo v) {
    Digits digits = v.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      out.add(new ValidationInfo(DIGITS, digits));
    }
  }

  private static void addPast(List<ValidationInfo> out, ValidateVo v) {
    Past past = v.past();
    if (!past.message().isEmpty()) {
      out.add(new ValidationInfo(PAST, past));
    }
  }

  private static void addFuture(List<ValidationInfo> out, ValidateVo v) {
    Future future = v.future();
    if (!future.message().isEmpty()) {
      out.add(new ValidationInfo(FUTURE, future));
    }
  }

  private static void addPastOrPresent(List<ValidationInfo> out, ValidateVo v) {
    PastOrPresent pastOrPresent = v.pastOrPresent();
    if (!pastOrPresent.message().isEmpty()) {
      out.add(new ValidationInfo(PAST_OR_PRESENT, pastOrPresent));
    }
  }

  private static void addFutureOrPresent(List<ValidationInfo> out, ValidateVo v) {
    FutureOrPresent futureOrPresent = v.futureOrPresent();
    if (!futureOrPresent.message().isEmpty()) {
      out.add(new ValidationInfo(FUTURE_OR_PRESENT, futureOrPresent));
    }
  }
}

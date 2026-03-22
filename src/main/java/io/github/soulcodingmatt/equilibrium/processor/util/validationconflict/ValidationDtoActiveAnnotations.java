package io.github.soulcodingmatt.equilibrium.processor.util.validationconflict;

import static io.github.soulcodingmatt.equilibrium.processor.util.validationconflict.ValidationConflictLabels.*;

import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;
import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import java.util.ArrayList;
import java.util.List;

/** Collects active nested Jakarta validations from {@link ValidateDto}. */
final class ValidationDtoActiveAnnotations {

  private ValidationDtoActiveAnnotations() {}

  static List<ValidationInfo> collect(ValidateDto validateDto) {
    List<ValidationInfo> validations = new ArrayList<>();
    addNotNull(validations, validateDto);
    addNotBlank(validations, validateDto);
    addNotEmpty(validations, validateDto);
    addSize(validations, validateDto);
    addMin(validations, validateDto);
    addMax(validations, validateDto);
    addEmail(validations, validateDto);
    addPattern(validations, validateDto);
    addPositive(validations, validateDto);
    addPositiveOrZero(validations, validateDto);
    addNegative(validations, validateDto);
    addNegativeOrZero(validations, validateDto);
    addDigits(validations, validateDto);
    addPast(validations, validateDto);
    addFuture(validations, validateDto);
    addPastOrPresent(validations, validateDto);
    addFutureOrPresent(validations, validateDto);
    return validations;
  }

  private static void addNotNull(List<ValidationInfo> out, ValidateDto dto) {
    NotNull notNull = dto.notNull();
    if (!notNull.message().isEmpty()) {
      out.add(new ValidationInfo(NOT_NULL, notNull));
    }
  }

  private static void addNotBlank(List<ValidationInfo> out, ValidateDto dto) {
    NotBlank notBlank = dto.notBlank();
    if (!notBlank.message().isEmpty()) {
      out.add(new ValidationInfo(NOT_BLANK, notBlank));
    }
  }

  private static void addNotEmpty(List<ValidationInfo> out, ValidateDto dto) {
    NotEmpty notEmpty = dto.notEmpty();
    if (!notEmpty.message().isEmpty()) {
      out.add(new ValidationInfo(NOT_EMPTY, notEmpty));
    }
  }

  private static void addSize(List<ValidationInfo> out, ValidateDto dto) {
    Size size = dto.size();
    if (size.min() != -1 || size.max() != -1) {
      out.add(new ValidationInfo("Size", size));
    }
  }

  private static void addMin(List<ValidationInfo> out, ValidateDto dto) {
    Min min = dto.min();
    if (min.value() != Long.MIN_VALUE) {
      out.add(new ValidationInfo("Min", min));
    }
  }

  private static void addMax(List<ValidationInfo> out, ValidateDto dto) {
    Max max = dto.max();
    if (max.value() != Long.MAX_VALUE) {
      out.add(new ValidationInfo("Max", max));
    }
  }

  private static void addEmail(List<ValidationInfo> out, ValidateDto dto) {
    Email email = dto.email();
    if (!email.message().isEmpty()) {
      out.add(new ValidationInfo(EMAIL, email));
    }
  }

  private static void addPattern(List<ValidationInfo> out, ValidateDto dto) {
    Pattern pattern = dto.pattern();
    if (!pattern.regexp().isEmpty()) {
      out.add(new ValidationInfo(PATTERN, pattern));
    }
  }

  private static void addPositive(List<ValidationInfo> out, ValidateDto dto) {
    Positive positive = dto.positive();
    if (!positive.message().isEmpty()) {
      out.add(new ValidationInfo(POSITIVE, positive));
    }
  }

  private static void addPositiveOrZero(List<ValidationInfo> out, ValidateDto dto) {
    PositiveOrZero positiveOrZero = dto.positiveOrZero();
    if (!positiveOrZero.message().isEmpty()) {
      out.add(new ValidationInfo(POSITIVE_OR_ZERO, positiveOrZero));
    }
  }

  private static void addNegative(List<ValidationInfo> out, ValidateDto dto) {
    Negative negative = dto.negative();
    if (!negative.message().isEmpty()) {
      out.add(new ValidationInfo(NEGATIVE, negative));
    }
  }

  private static void addNegativeOrZero(List<ValidationInfo> out, ValidateDto dto) {
    NegativeOrZero negativeOrZero = dto.negativeOrZero();
    if (!negativeOrZero.message().isEmpty()) {
      out.add(new ValidationInfo(NEGATIVE_OR_ZERO, negativeOrZero));
    }
  }

  private static void addDigits(List<ValidationInfo> out, ValidateDto dto) {
    Digits digits = dto.digits();
    if (digits.integer() != -1 || digits.fraction() != -1) {
      out.add(new ValidationInfo(DIGITS, digits));
    }
  }

  private static void addPast(List<ValidationInfo> out, ValidateDto dto) {
    Past past = dto.past();
    if (!past.message().isEmpty()) {
      out.add(new ValidationInfo(PAST, past));
    }
  }

  private static void addFuture(List<ValidationInfo> out, ValidateDto dto) {
    Future future = dto.future();
    if (!future.message().isEmpty()) {
      out.add(new ValidationInfo(FUTURE, future));
    }
  }

  private static void addPastOrPresent(List<ValidationInfo> out, ValidateDto dto) {
    PastOrPresent pastOrPresent = dto.pastOrPresent();
    if (!pastOrPresent.message().isEmpty()) {
      out.add(new ValidationInfo(PAST_OR_PRESENT, pastOrPresent));
    }
  }

  private static void addFutureOrPresent(List<ValidationInfo> out, ValidateDto dto) {
    FutureOrPresent futureOrPresent = dto.futureOrPresent();
    if (!futureOrPresent.message().isEmpty()) {
      out.add(new ValidationInfo(FUTURE_OR_PRESENT, futureOrPresent));
    }
  }
}

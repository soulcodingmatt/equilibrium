package io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support;

import static io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support.ValidationCodegenConstants.*;

import io.github.soulcodingmatt.equilibrium.experimental.validation.common.*;
import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.experimental.validation.record.ValidateRecord;
import io.github.soulcodingmatt.equilibrium.experimental.validation.vo.ValidateVo;
import java.util.Set;

/**
 * Collects Jakarta constraint imports implied by type-safe nested annotations on {@link
 * ValidateDto}, {@link ValidateRecord}, and {@link ValidateVo}.
 */
public final class ValidationTypeSafeImports {

  private ValidationTypeSafeImports() {}

  public static void addForDto(Set<String> validationImports, ValidateDto validateAnnotation) {
    addMessageTriggeredForDto(validationImports, validateAnnotation);
    addStructuralForDto(validationImports, validateAnnotation);
  }

  public static void addForRecord(
      Set<String> validationImports, ValidateRecord validateAnnotation) {
    addMessageTriggeredForRecord(validationImports, validateAnnotation);
    addStructuralForRecord(validationImports, validateAnnotation);
  }

  public static void addForVo(Set<String> validationImports, ValidateVo validateAnnotation) {
    addMessageTriggeredForVo(validationImports, validateAnnotation);
    addStructuralForVo(validationImports, validateAnnotation);
  }

  private static void addMessageTriggeredForDto(
      Set<String> validationImports, ValidateDto validateAnnotation) {
    if (!validateAnnotation.notNull().message().isEmpty()) {
      validationImports.add(IMPORT_NOT_NULL);
    }
    if (!validateAnnotation.notBlank().message().isEmpty()) {
      validationImports.add(IMPORT_NOT_BLANK);
    }
    if (!validateAnnotation.notEmpty().message().isEmpty()) {
      validationImports.add(IMPORT_NOT_EMPTY);
    }
    if (!validateAnnotation.positive().message().isEmpty()) {
      validationImports.add(IMPORT_POSITIVE);
    }
    if (!validateAnnotation.positiveOrZero().message().isEmpty()) {
      validationImports.add(IMPORT_POSITIVE_OR_ZERO);
    }
    if (!validateAnnotation.negative().message().isEmpty()) {
      validationImports.add(IMPORT_NEGATIVE);
    }
    if (!validateAnnotation.negativeOrZero().message().isEmpty()) {
      validationImports.add(IMPORT_NEGATIVE_OR_ZERO);
    }
    if (!validateAnnotation.past().message().isEmpty()) {
      validationImports.add(IMPORT_PAST);
    }
    if (!validateAnnotation.future().message().isEmpty()) {
      validationImports.add(IMPORT_FUTURE);
    }
    if (!validateAnnotation.pastOrPresent().message().isEmpty()) {
      validationImports.add(IMPORT_PAST_OR_PRESENT);
    }
    if (!validateAnnotation.futureOrPresent().message().isEmpty()) {
      validationImports.add(IMPORT_FUTURE_OR_PRESENT);
    }
    if (!validateAnnotation.email().message().isEmpty()) {
      validationImports.add(IMPORT_EMAIL);
    }
  }

  private static void addStructuralForDto(
      Set<String> validationImports, ValidateDto validateAnnotation) {
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

  private static void addMessageTriggeredForRecord(
      Set<String> validationImports, ValidateRecord validateAnnotation) {
    if (!validateAnnotation.notNull().message().isEmpty()) {
      validationImports.add(IMPORT_NOT_NULL);
    }
    if (!validateAnnotation.notBlank().message().isEmpty()) {
      validationImports.add(IMPORT_NOT_BLANK);
    }
    if (!validateAnnotation.notEmpty().message().isEmpty()) {
      validationImports.add(IMPORT_NOT_EMPTY);
    }
    if (!validateAnnotation.positive().message().isEmpty()) {
      validationImports.add(IMPORT_POSITIVE);
    }
    if (!validateAnnotation.positiveOrZero().message().isEmpty()) {
      validationImports.add(IMPORT_POSITIVE_OR_ZERO);
    }
    if (!validateAnnotation.negative().message().isEmpty()) {
      validationImports.add(IMPORT_NEGATIVE);
    }
    if (!validateAnnotation.negativeOrZero().message().isEmpty()) {
      validationImports.add(IMPORT_NEGATIVE_OR_ZERO);
    }
    if (!validateAnnotation.past().message().isEmpty()) {
      validationImports.add(IMPORT_PAST);
    }
    if (!validateAnnotation.future().message().isEmpty()) {
      validationImports.add(IMPORT_FUTURE);
    }
    if (!validateAnnotation.pastOrPresent().message().isEmpty()) {
      validationImports.add(IMPORT_PAST_OR_PRESENT);
    }
    if (!validateAnnotation.futureOrPresent().message().isEmpty()) {
      validationImports.add(IMPORT_FUTURE_OR_PRESENT);
    }
    if (!validateAnnotation.email().message().isEmpty()) {
      validationImports.add(IMPORT_EMAIL);
    }
  }

  private static void addStructuralForRecord(
      Set<String> validationImports, ValidateRecord validateAnnotation) {
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

  private static void addMessageTriggeredForVo(
      Set<String> validationImports, ValidateVo validateAnnotation) {
    if (!validateAnnotation.notNull().message().isEmpty()) {
      validationImports.add(IMPORT_NOT_NULL);
    }
    if (!validateAnnotation.notBlank().message().isEmpty()) {
      validationImports.add(IMPORT_NOT_BLANK);
    }
    if (!validateAnnotation.notEmpty().message().isEmpty()) {
      validationImports.add(IMPORT_NOT_EMPTY);
    }
    if (!validateAnnotation.positive().message().isEmpty()) {
      validationImports.add(IMPORT_POSITIVE);
    }
    if (!validateAnnotation.positiveOrZero().message().isEmpty()) {
      validationImports.add(IMPORT_POSITIVE_OR_ZERO);
    }
    if (!validateAnnotation.negative().message().isEmpty()) {
      validationImports.add(IMPORT_NEGATIVE);
    }
    if (!validateAnnotation.negativeOrZero().message().isEmpty()) {
      validationImports.add(IMPORT_NEGATIVE_OR_ZERO);
    }
    if (!validateAnnotation.past().message().isEmpty()) {
      validationImports.add(IMPORT_PAST);
    }
    if (!validateAnnotation.future().message().isEmpty()) {
      validationImports.add(IMPORT_FUTURE);
    }
    if (!validateAnnotation.pastOrPresent().message().isEmpty()) {
      validationImports.add(IMPORT_PAST_OR_PRESENT);
    }
    if (!validateAnnotation.futureOrPresent().message().isEmpty()) {
      validationImports.add(IMPORT_FUTURE_OR_PRESENT);
    }
    if (!validateAnnotation.email().message().isEmpty()) {
      validationImports.add(IMPORT_EMAIL);
    }
  }

  private static void addStructuralForVo(
      Set<String> validationImports, ValidateVo validateAnnotation) {
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

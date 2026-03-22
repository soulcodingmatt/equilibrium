package io.github.soulcodingmatt.equilibrium.processor.validation.analysis;

import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.experimental.validation.record.ValidateRecord;
import io.github.soulcodingmatt.equilibrium.experimental.validation.vo.ValidateVo;
import io.github.soulcodingmatt.equilibrium.processor.validation.analysis.conflict.ValidationConflictLabels;
import io.github.soulcodingmatt.equilibrium.processor.validation.analysis.conflict.ValidationSingleFieldValidator;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.VariableElement;

/**
 * Utility class for validating @ValidateDto annotation combinations to prevent invalid Jakarta Bean
 * Validation configurations.
 */
public class ValidationConflictUtil {

  public static final String NEGATIVE_OR_ZERO = ValidationConflictLabels.NEGATIVE_OR_ZERO;
  public static final String FUTURE = ValidationConflictLabels.FUTURE;
  public static final String PAST_OR_PRESENT = ValidationConflictLabels.PAST_OR_PRESENT;
  public static final String POSITIVE = ValidationConflictLabels.POSITIVE;
  public static final String POSITIVE_OR_ZERO = ValidationConflictLabels.POSITIVE_OR_ZERO;
  public static final String NEGATIVE = ValidationConflictLabels.NEGATIVE;
  public static final String DIGITS = ValidationConflictLabels.DIGITS;
  public static final String PAST = ValidationConflictLabels.PAST;
  public static final String FUTURE_OR_PRESENT = ValidationConflictLabels.FUTURE_OR_PRESENT;
  public static final String IS_OF_TYPE = ValidationConflictLabels.IS_OF_TYPE;
  public static final String NOT_EMPTY = ValidationConflictLabels.NOT_EMPTY;
  public static final String MIN = ValidationConflictLabels.MIN;
  public static final String MAX = ValidationConflictLabels.MAX;
  public static final String FIELD = ValidationConflictLabels.FIELD;
  public static final String PATTERN = ValidationConflictLabels.PATTERN;
  public static final String NOT_BLANK = ValidationConflictLabels.NOT_BLANK;
  public static final String EMAIL = ValidationConflictLabels.EMAIL;
  public static final String NOT_NULL = ValidationConflictLabels.NOT_NULL;

  private ValidationConflictUtil() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  /**
   * Validates all ValidateDto annotations on a field for conflicts and type compatibility.
   *
   * @param field the field being validated
   * @param validateDtoAnnotations array of ValidateDto annotations on the field
   * @return list of validation error messages, empty if no conflicts found
   */
  public static List<String> validateField(
      VariableElement field, ValidateDto[] validateDtoAnnotations) {
    List<String> errors = new ArrayList<>();

    for (ValidateDto validateDto : validateDtoAnnotations) {
      errors.addAll(ValidationSingleFieldValidator.validateDto(field, validateDto));
    }

    return errors;
  }

  /**
   * Validates all ValidateRecord annotations on a field for conflicts and type compatibility.
   *
   * @param field the field being validated
   * @param validateRecordAnnotations array of ValidateRecord annotations on the field
   * @return list of validation error messages, empty if no conflicts found
   */
  public static List<String> validateRecordField(
      VariableElement field, ValidateRecord[] validateRecordAnnotations) {
    List<String> errors = new ArrayList<>();

    for (ValidateRecord validateRecord : validateRecordAnnotations) {
      errors.addAll(ValidationSingleFieldValidator.validateRecord(field, validateRecord));
    }

    return errors;
  }

  /**
   * Validates all ValidateVo annotations on a field for conflicts and type compatibility.
   *
   * @param field the field being validated
   * @param validateVoAnnotations array of ValidateVo annotations on the field
   * @return list of validation error messages, empty if no conflicts found
   */
  public static List<String> validateVoField(
      VariableElement field, ValidateVo[] validateVoAnnotations) {
    List<String> errors = new ArrayList<>();

    for (ValidateVo validateVo : validateVoAnnotations) {
      errors.addAll(ValidationSingleFieldValidator.validateVo(field, validateVo));
    }

    return errors;
  }
}

package io.github.soulcodingmatt.equilibrium.processor.validation.analysis.conflict;

import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.experimental.validation.record.ValidateRecord;
import io.github.soulcodingmatt.equilibrium.experimental.validation.vo.ValidateVo;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/** Runs type and logical validation for one {@code @Validate*} annotation instance. */
public final class ValidationSingleFieldValidator {

  private ValidationSingleFieldValidator() {}

  public static List<String> validateDto(VariableElement field, ValidateDto validateDto) {
    List<String> errors = new ArrayList<>();
    TypeMirror fieldType = field.asType();
    String fieldName = field.getSimpleName().toString();

    List<ValidationInfo> activeValidations =
        ValidationAnnotationCollector.collectActiveValidations(validateDto);

    for (ValidationInfo validation : activeValidations) {
      errors.addAll(
          ValidationTypeCompatibilityChecker.checkTypeCompatibility(
              fieldName, fieldType, validation));
    }

    errors.addAll(
        ValidationPairConflictChecker.checkLogicalConflicts(fieldName, activeValidations));
    return errors;
  }

  public static List<String> validateRecord(VariableElement field, ValidateRecord validateRecord) {
    List<String> errors = new ArrayList<>();
    TypeMirror fieldType = field.asType();
    String fieldName = field.getSimpleName().toString();

    List<ValidationInfo> activeValidations =
        ValidationAnnotationCollector.collectActiveRecordValidations(validateRecord);

    for (ValidationInfo validation : activeValidations) {
      errors.addAll(
          ValidationTypeCompatibilityChecker.checkTypeCompatibility(
              fieldName, fieldType, validation));
    }

    errors.addAll(
        ValidationPairConflictChecker.checkLogicalConflicts(fieldName, activeValidations));
    return errors;
  }

  public static List<String> validateVo(VariableElement field, ValidateVo validateVo) {
    List<String> errors = new ArrayList<>();
    TypeMirror fieldType = field.asType();
    String fieldName = field.getSimpleName().toString();

    List<ValidationInfo> activeValidations =
        ValidationAnnotationCollector.collectActiveVoValidations(validateVo);

    for (ValidationInfo validation : activeValidations) {
      errors.addAll(
          ValidationTypeCompatibilityChecker.checkTypeCompatibility(
              fieldName, fieldType, validation));
    }

    errors.addAll(
        ValidationPairConflictChecker.checkLogicalConflicts(fieldName, activeValidations));
    return errors;
  }
}

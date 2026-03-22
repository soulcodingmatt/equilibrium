package io.github.soulcodingmatt.equilibrium.processor.validation.codegen;

import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.experimental.validation.record.ValidateRecord;
import io.github.soulcodingmatt.equilibrium.experimental.validation.vo.ValidateVo;
import io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support.ValidationDtoCodegen;
import io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support.ValidationIds;
import io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support.ValidationRecordCodegen;
import io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support.ValidationStringAnnotationImports;
import io.github.soulcodingmatt.equilibrium.processor.validation.codegen.support.ValidationVoCodegen;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;

/** Utility methods for validation-related import collection and annotation emission. */
public final class ValidationSupport {

  private ValidationSupport() {
    throw new AssertionError("You should not be here!");
  }

  public static Set<String> collectValidationImports(List<VariableElement> fields, int dtoId) {
    Set<String> validationImports = new HashSet<>();

    for (VariableElement field : fields) {
      for (ValidateDto validateAnnotation : field.getAnnotationsByType(ValidateDto.class)) {
        if (ValidationIds.matchesIds(validateAnnotation.ids(), dtoId)) {
          ValidationDtoCodegen.addTypeSafeValidationImports(validationImports, validateAnnotation);
          ValidationStringAnnotationImports.mergeFromValueStrings(
              validationImports, validateAnnotation.value());
        }
      }
    }

    return validationImports;
  }

  public static boolean shouldApplyValidation(ValidateDto validateAnnotation, int dtoId) {
    return ValidationIds.matchesIds(validateAnnotation.ids(), dtoId);
  }

  public static void writeTypeSafeValidations(Writer writer, ValidateDto validateAnnotation)
      throws IOException {
    ValidationDtoCodegen.writeTypeSafeValidations(writer, validateAnnotation);
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
      for (ValidateRecord validateAnnotation : field.getAnnotationsByType(ValidateRecord.class)) {
        if (ValidationIds.matchesIds(validateAnnotation.ids(), recordId)) {
          ValidationRecordCodegen.addTypeSafeRecordValidationImports(
              validationImports, validateAnnotation);
          ValidationStringAnnotationImports.mergeFromValueStrings(
              validationImports, validateAnnotation.value());
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
    return ValidationIds.matchesIds(validateAnnotation.ids(), recordId);
  }

  public static void writeTypeSafeRecordValidations(
      Writer writer, ValidateRecord validateAnnotation) throws IOException {
    ValidationRecordCodegen.writeTypeSafeRecordValidations(writer, validateAnnotation);
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
      for (ValidateVo validateAnnotation : field.getAnnotationsByType(ValidateVo.class)) {
        if (ValidationIds.matchesIds(validateAnnotation.ids(), voId)) {
          ValidationVoCodegen.addTypeSafeVoValidationImports(validationImports, validateAnnotation);
          ValidationStringAnnotationImports.mergeFromValueStrings(
              validationImports, validateAnnotation.value());
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
    return ValidationIds.matchesIds(validateAnnotation.ids(), voId);
  }

  public static void writeTypeSafeVoValidations(Writer writer, ValidateVo validateAnnotation)
      throws IOException {
    ValidationVoCodegen.writeTypeSafeVoValidations(writer, validateAnnotation);
  }
}

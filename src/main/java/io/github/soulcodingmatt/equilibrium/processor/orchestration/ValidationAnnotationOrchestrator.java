package io.github.soulcodingmatt.equilibrium.processor.orchestration;

import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.experimental.validation.record.ValidateRecord;
import io.github.soulcodingmatt.equilibrium.experimental.validation.vo.ValidateVo;
import io.github.soulcodingmatt.equilibrium.processor.validation.analysis.ValidationConflictUtil;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Validates experimental validation annotations ({@code @ValidateDto}, {@code @ValidateRecord},
 * {@code @ValidateVo}) on source class fields before code generation begins.
 */
class ValidationAnnotationOrchestrator {

  private final DiagnosticReporter reporter;

  ValidationAnnotationOrchestrator(DiagnosticReporter reporter) {
    this.reporter = reporter;
  }

  /**
   * Validates all validation annotations on the given class.
   *
   * @return {@code false} if any annotation has a conflict that should abort processing
   */
  boolean validateAll(TypeElement classElement) {
    return validateValidateDtoAnnotations(classElement)
        && validateValidateRecordAnnotations(classElement)
        && validateValidateVoAnnotations(classElement);
  }

  boolean validateValidateDtoAnnotations(TypeElement classElement) {
    boolean hasErrors = false;

    for (Element element : classElement.getEnclosedElements()) {
      if (element.getKind() == ElementKind.FIELD) {
        VariableElement field = (VariableElement) element;
        ValidateDto[] annotations = field.getAnnotationsByType(ValidateDto.class);

        if (annotations.length > 0) {
          List<String> errors = ValidationConflictUtil.validateField(field, annotations);
          for (String errorMessage : errors) {
            reporter.error(field, errorMessage);
            hasErrors = true;
          }
        }
      }
    }

    return !hasErrors;
  }

  boolean validateValidateRecordAnnotations(TypeElement classElement) {
    boolean hasErrors = false;

    for (Element element : classElement.getEnclosedElements()) {
      if (element.getKind() == ElementKind.FIELD) {
        VariableElement field = (VariableElement) element;
        ValidateRecord[] annotations = field.getAnnotationsByType(ValidateRecord.class);

        if (annotations.length > 0) {
          List<String> errors = ValidationConflictUtil.validateRecordField(field, annotations);
          for (String errorMessage : errors) {
            reporter.error(field, errorMessage);
            hasErrors = true;
          }
        }
      }
    }

    return !hasErrors;
  }

  boolean validateValidateVoAnnotations(TypeElement classElement) {
    boolean hasErrors = false;

    for (Element element : classElement.getEnclosedElements()) {
      if (element.getKind() == ElementKind.FIELD) {
        VariableElement field = (VariableElement) element;
        ValidateVo[] annotations = field.getAnnotationsByType(ValidateVo.class);

        if (annotations.length > 0) {
          List<String> errors = ValidationConflictUtil.validateVoField(field, annotations);
          for (String errorMessage : errors) {
            reporter.error(field, errorMessage);
            hasErrors = true;
          }
        }
      }
    }

    return !hasErrors;
  }
}

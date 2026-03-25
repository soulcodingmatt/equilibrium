package io.github.soulcodingmatt.equilibrium.processor.validation.codegen;

import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.processor.generation.dto.DtoValidationEmitter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;

/** Implements {@link DtoValidationEmitter} using the validation infrastructure. */
public final class DtoValidationEmitterImpl implements DtoValidationEmitter {

  @Override
  public Set<String> collectImports(List<VariableElement> fields, int dtoId) {
    return ValidationSupport.collectValidationImports(fields, dtoId);
  }

  @Override
  public void emitFieldAnnotations(Writer writer, VariableElement field, int dtoId)
      throws IOException {
    ValidateDto[] validateAnnotations = field.getAnnotationsByType(ValidateDto.class);
    for (ValidateDto validateAnnotation : validateAnnotations) {
      if (ValidationSupport.shouldApplyValidation(validateAnnotation, dtoId)) {
        ValidationSupport.writeTypeSafeValidations(writer, validateAnnotation);
        for (String validation : validateAnnotation.value()) {
          if (!validation.trim().isEmpty()) {
            writer.write("    " + validation + "\n");
          }
        }
      }
    }
  }
}

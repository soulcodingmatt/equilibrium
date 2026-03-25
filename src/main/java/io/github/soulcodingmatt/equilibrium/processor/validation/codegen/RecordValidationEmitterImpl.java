package io.github.soulcodingmatt.equilibrium.processor.validation.codegen;

import io.github.soulcodingmatt.equilibrium.experimental.validation.record.ValidateRecord;
import io.github.soulcodingmatt.equilibrium.processor.generation.record.RecordValidationEmitter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;

/** Implements {@link RecordValidationEmitter} using the validation infrastructure. */
public final class RecordValidationEmitterImpl implements RecordValidationEmitter {

  @Override
  public Set<String> collectImports(List<VariableElement> fields, int recordId) {
    return ValidationSupport.collectRecordValidationImports(fields, recordId);
  }

  @Override
  public void emitParameterAnnotations(Writer writer, VariableElement field, int recordId)
      throws IOException {
    ValidateRecord[] validateAnnotations = field.getAnnotationsByType(ValidateRecord.class);
    for (ValidateRecord validateAnnotation : validateAnnotations) {
      if (ValidationSupport.shouldApplyRecordValidation(validateAnnotation, recordId)) {
        ValidationSupport.writeTypeSafeRecordValidations(writer, validateAnnotation);
      }
    }
  }
}

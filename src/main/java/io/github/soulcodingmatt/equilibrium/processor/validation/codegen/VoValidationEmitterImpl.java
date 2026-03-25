package io.github.soulcodingmatt.equilibrium.processor.validation.codegen;

import io.github.soulcodingmatt.equilibrium.experimental.validation.vo.ValidateVo;
import io.github.soulcodingmatt.equilibrium.processor.generation.vo.VoValidationEmitter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;

/** Implements {@link VoValidationEmitter} using the validation infrastructure. */
public final class VoValidationEmitterImpl implements VoValidationEmitter {

  @Override
  public Set<String> collectImports(List<VariableElement> fields, int voId) {
    return ValidationSupport.collectVoValidationImports(fields, voId);
  }

  @Override
  public void emitFieldAnnotations(Writer writer, VariableElement field, int voId)
      throws IOException {
    ValidateVo[] validateAnnotations = field.getAnnotationsByType(ValidateVo.class);
    for (ValidateVo validateAnnotation : validateAnnotations) {
      if (ValidationSupport.shouldApplyVoValidation(validateAnnotation, voId)) {
        ValidationSupport.writeTypeSafeVoValidations(writer, validateAnnotation);
      }
    }
  }
}

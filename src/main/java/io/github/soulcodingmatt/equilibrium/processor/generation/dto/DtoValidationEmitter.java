package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;

/**
 * Decouples DTO generation from the validation layer.
 *
 * <p>Implementations live in the validation layer; generators depend only on this interface.
 */
public interface DtoValidationEmitter {

  /** Returns the set of import strings required for validation annotations on the given fields. */
  Set<String> collectImports(List<VariableElement> fields, int dtoId);

  /**
   * Writes all applicable validation annotations for {@code field} into the generated source file.
   */
  void emitFieldAnnotations(Writer writer, VariableElement field, int dtoId) throws IOException;
}

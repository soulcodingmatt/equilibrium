package io.github.soulcodingmatt.equilibrium.processor.generation.record;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;

/**
 * Decouples record generation from the validation layer.
 *
 * <p>Implementations live in the validation layer; generators depend only on this interface.
 */
public interface RecordValidationEmitter {

  /** Returns the set of import strings required for validation annotations on the given fields. */
  Set<String> collectImports(List<VariableElement> fields, int recordId);

  /**
   * Writes all applicable validation annotations for {@code field} (as a record parameter) into the
   * generated source file.
   */
  void emitParameterAnnotations(Writer writer, VariableElement field, int recordId)
      throws IOException;
}

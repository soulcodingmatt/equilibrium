package io.github.soulcodingmatt.equilibrium.processor.generation.vo;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;

/**
 * Decouples VO generation from the validation layer.
 *
 * <p>Implementations live in the validation layer; generators depend only on this interface.
 */
public interface VoValidationEmitter {

  /** Returns the set of import strings required for validation annotations on the given fields. */
  Set<String> collectImports(List<VariableElement> fields, int voId);

  /**
   * Writes all applicable validation annotations for {@code field} into the generated source file.
   */
  void emitFieldAnnotations(Writer writer, VariableElement field, int voId) throws IOException;
}

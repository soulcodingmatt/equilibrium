package io.github.soulcodingmatt.equilibrium.processor.orchestration;

import javax.lang.model.element.Element;

/**
 * Abstracts compiler diagnostic reporting so orchestration classes don't depend directly on {@link
 * javax.annotation.processing.Messager}.
 *
 * <p>The implementation in {@link EquilibriumProcessor} delegates to its private helper methods,
 * preserving the {@code emittedGenerationNotes} tracking logic in one place.
 */
public interface DiagnosticReporter {

  void note(Element element, String message);

  void error(Element element, String message);

  void error(String message);

  void warning(Element element, String message);

  void warning(String message);
}

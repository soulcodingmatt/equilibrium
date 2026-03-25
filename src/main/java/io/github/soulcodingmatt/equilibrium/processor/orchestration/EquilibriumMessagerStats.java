package io.github.soulcodingmatt.equilibrium.processor.orchestration;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Delegates to a {@link Messager} and counts {@link Diagnostic.Kind#ERROR} and {@link
 * Diagnostic.Kind#WARNING} diagnostics emitted through this processor.
 */
public final class EquilibriumMessagerStats implements Messager {

  private final Messager delegate;
  private int errorCount;
  private int warningCount;

  public EquilibriumMessagerStats(Messager delegate) {
    this.delegate = delegate;
  }

  /** The wrapped messager (e.g. for tests asserting delegation). */
  public Messager delegateMessager() {
    return delegate;
  }

  public int getErrorCount() {
    return errorCount;
  }

  public int getWarningCount() {
    return warningCount;
  }

  @Override
  public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
    tally(kind);
    delegate.printMessage(kind, msg);
  }

  @Override
  public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element element) {
    tally(kind);
    delegate.printMessage(kind, msg, element);
  }

  @Override
  public void printMessage(
      Diagnostic.Kind kind, CharSequence msg, Element element, AnnotationMirror annotationMirror) {
    tally(kind);
    delegate.printMessage(kind, msg, element, annotationMirror);
  }

  @Override
  public void printMessage(
      Diagnostic.Kind kind,
      CharSequence msg,
      Element element,
      AnnotationMirror annotationMirror,
      AnnotationValue annotationValue) {
    tally(kind);
    delegate.printMessage(kind, msg, element, annotationMirror, annotationValue);
  }

  private void tally(Diagnostic.Kind kind) {
    if (kind == Diagnostic.Kind.ERROR) {
      errorCount++;
    } else if (kind == Diagnostic.Kind.WARNING) {
      warningCount++;
    }
  }
}

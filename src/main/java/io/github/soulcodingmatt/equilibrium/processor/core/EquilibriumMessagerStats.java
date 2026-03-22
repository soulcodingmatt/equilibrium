package io.github.soulcodingmatt.equilibrium.processor.core;

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
  public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
    tally(kind);
    delegate.printMessage(kind, msg, e);
  }

  @Override
  public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
    tally(kind);
    delegate.printMessage(kind, msg, e, a);
  }

  @Override
  public void printMessage(
      Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
    tally(kind);
    delegate.printMessage(kind, msg, e, a, v);
  }

  private void tally(Diagnostic.Kind kind) {
    if (kind == Diagnostic.Kind.ERROR) {
      errorCount++;
    } else if (kind == Diagnostic.Kind.WARNING) {
      warningCount++;
    }
  }
}

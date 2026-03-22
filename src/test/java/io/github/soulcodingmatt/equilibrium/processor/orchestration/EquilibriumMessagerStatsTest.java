package io.github.soulcodingmatt.equilibrium.processor.orchestration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import org.junit.jupiter.api.Test;

class EquilibriumMessagerStatsTest {

  @Test
  void talliesErrorsAndWarnings_andDelegatesAllKinds() {
    List<Diagnostic.Kind> kinds = new ArrayList<>();
    List<String> messages = new ArrayList<>();
    Messager delegate =
        new Messager() {
          @Override
          public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            kinds.add(kind);
            messages.add(msg.toString());
          }

          @Override
          public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            kinds.add(kind);
            messages.add(msg.toString());
          }

          @Override
          public void printMessage(
              Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
            kinds.add(kind);
            messages.add(msg.toString());
          }

          @Override
          public void printMessage(
              Diagnostic.Kind kind,
              CharSequence msg,
              Element e,
              AnnotationMirror a,
              AnnotationValue v) {
            kinds.add(kind);
            messages.add(msg.toString());
          }
        };

    EquilibriumMessagerStats stats = new EquilibriumMessagerStats(delegate);
    assertSame(delegate, stats.delegateMessager());

    stats.printMessage(Diagnostic.Kind.ERROR, "e1");
    stats.printMessage(Diagnostic.Kind.WARNING, "w1");
    stats.printMessage(Diagnostic.Kind.NOTE, "n1");
    stats.printMessage(Diagnostic.Kind.ERROR, "e2", null);

    assertEquals(2, stats.getErrorCount());
    assertEquals(1, stats.getWarningCount());
    assertEquals(4, kinds.size());
    assertEquals(4, messages.size());
  }

  @Test
  void notesAndOtherKindsDoNotIncrementErrorOrWarningCounts() {
    AtomicInteger delegateCalls = new AtomicInteger();
    Messager delegate =
        new Messager() {
          private void tally() {
            delegateCalls.incrementAndGet();
          }

          @Override
          public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            tally();
          }

          @Override
          public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            tally();
          }

          @Override
          public void printMessage(
              Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
            tally();
          }

          @Override
          public void printMessage(
              Diagnostic.Kind kind,
              CharSequence msg,
              Element e,
              AnnotationMirror a,
              AnnotationValue v) {
            tally();
          }
        };
    EquilibriumMessagerStats stats = new EquilibriumMessagerStats(delegate);
    stats.printMessage(Diagnostic.Kind.NOTE, "x");
    stats.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "m");
    assertEquals(0, stats.getErrorCount());
    assertEquals(0, stats.getWarningCount());
    assertEquals(2, delegateCalls.get(), "stats should still forward to the delegate");
  }
}

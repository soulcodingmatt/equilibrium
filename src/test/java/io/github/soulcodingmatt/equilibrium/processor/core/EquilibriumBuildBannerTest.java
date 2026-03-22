package io.github.soulcodingmatt.equilibrium.processor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import org.junit.jupiter.api.Test;

/**
 * Locks in banner layout, summary table shape, aggregate line wording (including singular/plural),
 * and ANSI toggling for {@link EquilibriumBuildBanner}.
 */
class EquilibriumBuildBannerTest {

  private static final String DESCRIPTION =
      "Annotation-driven DTO, Record & VO generation from a single source of truth";

  /**
   * {@code equilibrium-build-version.txt} is filtered from {@code ${project.version}}; the banner
   * title must stay in sync when the POM version changes.
   */
  @Test
  void header_titleLineMatchesProjectVersionFromPom() throws IOException {
    String expected;
    try (InputStream in =
        EquilibriumBuildBanner.class.getResourceAsStream("/equilibrium-build-version.txt")) {
      assertNotNull(in, "Maven must copy filtered equilibrium-build-version.txt to classes");
      expected = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
    }
    assertFalse(expected.isEmpty());

    CollectingMessager messager = new CollectingMessager();
    EquilibriumBuildBanner.print(messager);
    int idx = 2 + countLogoResourceLines();
    assertEquals("Equilibrium " + expected, messager.notes().get(idx));
  }

  @Test
  void
      header_emitsBlankThenOuterRule_thenLogoWithUniformLineLengths_thenTitleDashDescriptionDash() {
    CollectingMessager messager = new CollectingMessager();
    EquilibriumBuildBanner.print(messager);

    List<String> notes = messager.notes();
    assertFalse(notes.isEmpty());
    assertEquals("", notes.get(0));

    String topRule = notes.get(1);
    assertTrue(isAllSameChar(topRule, '='), "top frame should be '='");
    int width = topRule.length();
    assertEquals(width, EquilibriumBuildBanner.headerContentWidthOrFallback());

    int logoLineCount = countLogoResourceLines();
    int idx = 2;
    assertEquals(idx + logoLineCount, indexAfterLogoBlock(notes, idx));

    String title = notes.get(idx + logoLineCount);
    assertTrue(title.startsWith("Equilibrium"), "title line");

    String dashAfterTitle = notes.get(idx + logoLineCount + 1);
    assertTrue(isAllSameChar(dashAfterTitle, '-'), "dash between title and description");
    assertEquals(width, dashAfterTitle.length());

    assertEquals(DESCRIPTION, notes.get(idx + logoLineCount + 2));

    String bottomRule = notes.get(idx + logoLineCount + 3);
    assertTrue(isAllSameChar(bottomRule, '-'), "bottom of header block should be '-'");
    assertEquals(width, bottomRule.length());
    assertEquals(idx + logoLineCount + 4, notes.size());
  }

  @Test
  void header_allLogoLinesHaveUniformLength_withinTopRuleWidth() {
    CollectingMessager messager = new CollectingMessager();
    EquilibriumBuildBanner.print(messager);
    List<String> notes = messager.notes();
    int logoLines = countLogoResourceLines();
    int topRuleLen = notes.get(1).length();
    int firstLogoLen = notes.get(2).length();
    assertTrue(firstLogoLen <= topRuleLen);
    for (int i = 0; i < logoLines; i++) {
      assertEquals(firstLogoLen, notes.get(2 + i).length(), "logo row " + i);
    }
  }

  @Test
  void summaryTable_hasExpectedStructure_andStatusColumnWidth() {
    CollectingMessager messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 14, 22, 0, 0, false);
    List<String> n = messager.notes();

    assertTrue(isAllSameChar(n.get(0), '-'), "leading dash line");
    int summaryInnerWidth = n.get(0).length();
    assertEquals("", n.get(1));
    assertEquals("Summary", n.get(2));
    assertTrue(isAllSameChar(n.get(3), '-'), "dash under Summary title");
    assertEquals(summaryInnerWidth, n.get(3).length(), "inner rules share one width");

    assertEquals(String.format("%-8s  %s", "Status", "Result"), n.get(4));
    String sep = n.get(5);
    assertTrue(sep.startsWith("--------"));
    assertTrue(sep.contains("  "));

    assertTrue(n.get(6).startsWith("[OK]"));
    assertTrue(n.get(6).contains("Processed 14 types"));
    assertMatchesStatusColumnWidth(n.get(6));

    assertTrue(n.get(7).contains("Generated 22 files"));
    assertTrue(n.get(8).contains("No errors"));
    assertTrue(n.get(9).contains("No warnings"));

    assertEquals("", n.get(10));
    assertEquals("Result: SUCCESS (14 types, 22 files)", n.get(11));
    assertTrue(isAllSameChar(n.get(12), '='), "closing outer rule");
    assertEquals(summaryInnerWidth, n.get(12).length(), "closing rule matches table width");
  }

  @Test
  void aggregateResult_success_omitsZeroWarnings() {
    CollectingMessager m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 14, 22, 0, 0, false);
    String resultLine =
        m.notes().stream().filter(s -> s.startsWith("Result:")).findFirst().orElse("");
    assertEquals("Result: SUCCESS (14 types, 22 files)", resultLine);
    assertFalse(resultLine.contains("warning"));
  }

  @Test
  void aggregateResult_successWithWarnings_singularAndPlural() {
    CollectingMessager m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 2, 3, 0, 1, false);
    assertContainsLine(m, "Result: SUCCESS WITH WARNINGS (2 types, 3 files, 1 warning)");

    m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 2, 3, 0, 2, false);
    assertContainsLine(m, "Result: SUCCESS WITH WARNINGS (2 types, 3 files, 2 warnings)");
  }

  @Test
  void aggregateResult_failure_errorsAndOptionalWarnings() {
    CollectingMessager m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 14, 22, 1, 0, false);
    assertContainsLine(m, "Result: FAILURE (14 types, 22 files, 1 error)");

    m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 14, 22, 2, 0, false);
    assertContainsLine(m, "Result: FAILURE (14 types, 22 files, 2 errors)");

    m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 14, 22, 2, 1, false);
    assertContainsLine(m, "Result: FAILURE (14 types, 22 files, 2 errors, 1 warning)");

    m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 14, 22, 1, 3, false);
    assertContainsLine(m, "Result: FAILURE (14 types, 22 files, 1 error, 3 warnings)");
  }

  @Test
  void aggregateResult_singularTypeAndFile() {
    CollectingMessager m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 1, 1, 0, 0, false);
    assertContainsLine(m, "Result: SUCCESS (1 type, 1 file)");
  }

  @Test
  void summaryRows_singularProcessedAndGeneratedPhrases() {
    CollectingMessager m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 1, 1, 0, 0, false);
    assertContainsLine(m, "Processed 1 type");
    assertContainsLine(m, "Generated 1 file");
  }

  @Test
  void summaryRows_reflectCounts_inStatusColumn() {
    CollectingMessager m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 5, 7, 2, 1, false);
    assertContainsLine(m, "Processed 5 types");
    assertContainsLine(m, "Generated 7 files");
    assertContainsLine(m, "2 errors");
    assertContainsLine(m, "1 warning");
  }

  @Test
  void ansiDisabled_containsNoEscapeSequences() {
    CollectingMessager m = new CollectingMessager();
    EquilibriumBuildBanner.print(m);
    EquilibriumBuildBanner.printGenerationSummary(m, 1, 1, 0, 0, false);
    for (String line : m.notes()) {
      assertFalse(line.contains("\u001b"), line);
    }
  }

  @Test
  void ansiEnabled_tagsAndAggregateUseColorCodes() {
    CollectingMessager m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 1, 1, 0, 0, true);
    String result = m.notes().stream().filter(s -> s.startsWith("Result:")).findFirst().orElse("");
    assertTrue(result.contains("\u001b[32m"), "SUCCESS should be green");
    assertTrue(result.contains("\u001b[0m"));

    m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 1, 1, 1, 0, true);
    result = m.notes().stream().filter(s -> s.startsWith("Result:")).findFirst().orElse("");
    assertTrue(result.contains("\u001b[31m"), "FAILURE should be red");

    m = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(m, 1, 1, 0, 1, true);
    result = m.notes().stream().filter(s -> s.startsWith("Result:")).findFirst().orElse("");
    assertTrue(result.contains("\u001b[33m"), "SUCCESS WITH WARNINGS should be yellow");
  }

  private static void assertContainsLine(CollectingMessager m, String expected) {
    assertTrue(
        m.notes().stream().anyMatch(line -> line.contains(expected)),
        () -> "expected substring not found; got:\n" + String.join("\n", m.notes()));
  }

  /** Status tag is left-padded to 8 chars, then two spaces, then result text (plain ANSI off). */
  private static void assertMatchesStatusColumnWidth(String row) {
    assertTrue(row.length() >= 10, () -> "row too short: " + row);
    assertEquals("  ", row.substring(8, 10), "gap after 8-char status column");
  }

  private static int indexAfterLogoBlock(List<String> notes, int startIdx) {
    int i = startIdx;
    int expectedLen = notes.get(i).length();
    while (i < notes.size()) {
      String s = notes.get(i);
      if (s.startsWith("Equilibrium")) {
        return i;
      }
      assertEquals(expectedLen, s.length(), "logo lines must share one width");
      i++;
    }
    throw new AssertionError("no title line");
  }

  private static int countLogoResourceLines() {
    try (var in = EquilibriumBuildBanner.class.getResourceAsStream("/equilibrium-logo.txt")) {
      assertNotNull(in);
      try (var br =
          new java.io.BufferedReader(
              new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))) {
        return (int) br.lines().count();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean isAllSameChar(String s, char c) {
    return !s.isEmpty() && s.chars().allMatch(ch -> ch == c);
  }

  private static final class CollectingMessager implements Messager {
    private final List<String> notes = new ArrayList<>();

    List<String> notes() {
      return notes;
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
      if (kind == Diagnostic.Kind.NOTE) {
        notes.add(msg.toString());
      }
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
      if (kind == Diagnostic.Kind.NOTE) {
        notes.add(msg.toString());
      }
    }

    @Override
    public void printMessage(
        Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
      printMessage(kind, msg, e);
    }

    @Override
    public void printMessage(
        Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
      printMessage(kind, msg, e);
    }
  }
}

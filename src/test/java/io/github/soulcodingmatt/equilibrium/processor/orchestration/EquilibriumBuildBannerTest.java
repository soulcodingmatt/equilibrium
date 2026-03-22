package io.github.soulcodingmatt.equilibrium.processor.orchestration;

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
    List<String> summaryNotes = messager.notes();

    assertTrue(isAllSameChar(summaryNotes.get(0), '-'), "leading dash line");
    int summaryInnerWidth = summaryNotes.get(0).length();
    assertEquals("", summaryNotes.get(1));
    assertEquals("Summary", summaryNotes.get(2));
    assertTrue(isAllSameChar(summaryNotes.get(3), '-'), "dash under Summary title");
    assertEquals(summaryInnerWidth, summaryNotes.get(3).length(), "inner rules share one width");

    assertEquals(String.format("%-8s  %s", "Status", "Result"), summaryNotes.get(4));
    String sep = summaryNotes.get(5);
    assertTrue(sep.startsWith("--------"));
    assertTrue(sep.contains("  "));

    assertTrue(summaryNotes.get(6).startsWith("[OK]"));
    assertTrue(summaryNotes.get(6).contains("Processed 14 types"));
    assertMatchesStatusColumnWidth(summaryNotes.get(6));

    assertTrue(summaryNotes.get(7).contains("Generated 22 files"));
    assertTrue(summaryNotes.get(8).contains("No errors"));
    assertTrue(summaryNotes.get(9).contains("No warnings"));

    assertEquals("", summaryNotes.get(10));
    assertEquals("Result: SUCCESS (14 types, 22 files)", summaryNotes.get(11));
    assertTrue(isAllSameChar(summaryNotes.get(12), '='), "closing outer rule");
    assertEquals(
        summaryInnerWidth, summaryNotes.get(12).length(), "closing rule matches table width");
  }

  @Test
  void aggregateResult_success_omitsZeroWarnings() {
    CollectingMessager messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 14, 22, 0, 0, false);
    String resultLine =
        messager.notes().stream().filter(line -> line.startsWith("Result:")).findFirst().orElse("");
    assertEquals("Result: SUCCESS (14 types, 22 files)", resultLine);
    assertFalse(resultLine.contains("warning"));
  }

  @Test
  void aggregateResult_successWithWarnings_singularAndPlural() {
    CollectingMessager messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 2, 3, 0, 1, false);
    assertContainsLine(messager, "Result: SUCCESS WITH WARNINGS (2 types, 3 files, 1 warning)");

    messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 2, 3, 0, 2, false);
    assertContainsLine(messager, "Result: SUCCESS WITH WARNINGS (2 types, 3 files, 2 warnings)");
  }

  @Test
  void aggregateResult_failure_errorsAndOptionalWarnings() {
    CollectingMessager messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 14, 22, 1, 0, false);
    assertContainsLine(messager, "Result: FAILURE (14 types, 22 files, 1 error)");

    messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 14, 22, 2, 0, false);
    assertContainsLine(messager, "Result: FAILURE (14 types, 22 files, 2 errors)");

    messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 14, 22, 2, 1, false);
    assertContainsLine(messager, "Result: FAILURE (14 types, 22 files, 2 errors, 1 warning)");

    messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 14, 22, 1, 3, false);
    assertContainsLine(messager, "Result: FAILURE (14 types, 22 files, 1 error, 3 warnings)");
  }

  @Test
  void aggregateResult_singularTypeAndFile() {
    CollectingMessager messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 1, 1, 0, 0, false);
    assertContainsLine(messager, "Result: SUCCESS (1 type, 1 file)");
  }

  @Test
  void summaryRows_singularProcessedAndGeneratedPhrases() {
    CollectingMessager messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 1, 1, 0, 0, false);
    assertContainsLine(messager, "Processed 1 type");
    assertContainsLine(messager, "Generated 1 file");
  }

  @Test
  void summaryRows_reflectCounts_inStatusColumn() {
    CollectingMessager messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 5, 7, 2, 1, false);
    assertContainsLine(messager, "Processed 5 types");
    assertContainsLine(messager, "Generated 7 files");
    assertContainsLine(messager, "2 errors");
    assertContainsLine(messager, "1 warning");
  }

  @Test
  void ansiDisabled_containsNoEscapeSequences() {
    CollectingMessager messager = new CollectingMessager();
    EquilibriumBuildBanner.print(messager);
    EquilibriumBuildBanner.printGenerationSummary(messager, 1, 1, 0, 0, false);
    for (String line : messager.notes()) {
      assertFalse(line.contains("\u001b"), line);
    }
  }

  @Test
  void ansiEnabled_tagsAndAggregateUseColorCodes() {
    CollectingMessager messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 1, 1, 0, 0, true);
    String result =
        messager.notes().stream().filter(line -> line.startsWith("Result:")).findFirst().orElse("");
    assertTrue(result.contains("\u001b[32m"), "SUCCESS should be green");
    assertTrue(result.contains("\u001b[0m"));

    messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 1, 1, 1, 0, true);
    result =
        messager.notes().stream().filter(line -> line.startsWith("Result:")).findFirst().orElse("");
    assertTrue(result.contains("\u001b[31m"), "FAILURE should be red");

    messager = new CollectingMessager();
    EquilibriumBuildBanner.printGenerationSummary(messager, 1, 1, 0, 1, true);
    result =
        messager.notes().stream().filter(line -> line.startsWith("Result:")).findFirst().orElse("");
    assertTrue(result.contains("\u001b[33m"), "SUCCESS WITH WARNINGS should be yellow");
  }

  private static void assertContainsLine(CollectingMessager messager, String expected) {
    assertTrue(
        messager.notes().stream().anyMatch(line -> line.contains(expected)),
        () -> "expected substring not found; got:\n" + String.join("\n", messager.notes()));
  }

  /** Status tag is left-padded to 8 chars, then two spaces, then result text (plain ANSI off). */
  private static void assertMatchesStatusColumnWidth(String row) {
    assertTrue(row.length() >= 10, () -> "row too short: " + row);
    assertEquals("  ", row.substring(8, 10), "gap after 8-char status column");
  }

  private static int indexAfterLogoBlock(List<String> notes, int startIdx) {
    int lineIndex = startIdx;
    int expectedLen = notes.get(lineIndex).length();
    while (lineIndex < notes.size()) {
      String line = notes.get(lineIndex);
      if (line.startsWith("Equilibrium")) {
        return lineIndex;
      }
      assertEquals(expectedLen, line.length(), "logo lines must share one width");
      lineIndex++;
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
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private static boolean isAllSameChar(String text, char expectedChar) {
    return !text.isEmpty() && text.chars().allMatch(ch -> ch == expectedChar);
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
    public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element element) {
      if (kind == Diagnostic.Kind.NOTE) {
        notes.add(msg.toString());
      }
    }

    @Override
    public void printMessage(
        Diagnostic.Kind kind,
        CharSequence msg,
        Element element,
        AnnotationMirror annotationMirror) {
      printMessage(kind, msg, element);
    }

    @Override
    public void printMessage(
        Diagnostic.Kind kind,
        CharSequence msg,
        Element element,
        AnnotationMirror annotationMirror,
        AnnotationValue annotationValue) {
      printMessage(kind, msg, element);
    }
  }
}

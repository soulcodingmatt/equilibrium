package io.github.soulcodingmatt.equilibrium.processor.orchestration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * Prints the Equilibrium ASCII logo and build summary as {@link Diagnostic.Kind#NOTE} lines so they
 * appear in Maven / Gradle compiler output.
 *
 * <p>Optional ANSI colors (see {@code equilibrium.banner.color}) color only the status tags; the
 * rest of each line is left at the default terminal color after {@code \u001b[0m}.
 */
public final class EquilibriumBuildBanner {

  private static final String RESOURCE = "/equilibrium-logo.txt";

  private static final String DESCRIPTION_LINE =
      "Annotation-driven DTO, Record & VO generation from a single source of truth";

  private static final String ANSI_RESET = "\u001b[0m";
  private static final String ANSI_GREEN = "\u001b[32m";
  private static final String ANSI_YELLOW = "\u001b[33m";
  private static final String ANSI_RED = "\u001b[31m";

  /**
   * Fixed width for the status column (widest tag is {@code [ERROR]} at 7; use 8 so the column
   * matches the {@code Status} header and keeps result text aligned).
   */
  private static final int STATUS_COL_WIDTH = 8;

  /** Two-column summary table: padded status cell, two spaces, result text. */
  private static final String SUMMARY_TWO_COLUMN_FORMAT = "%-" + STATUS_COL_WIDTH + "s  %s";

  private enum SummaryTag {
    OK,
    ERROR,
    WARN
  }

  private EquilibriumBuildBanner() {}

  public static void print(Messager messager) {
    List<String> logoLines = readLogoLines();
    if (logoLines.isEmpty()) {
      return;
    }
    List<String> titleLines = List.of(titleLineVersion(), DESCRIPTION_LINE);
    int width = computeHeaderContentWidth(logoLines, titleLines);
    String outerRule = "=".repeat(width);
    String innerRule = "-".repeat(width);

    messager.printMessage(Diagnostic.Kind.NOTE, "");
    messager.printMessage(Diagnostic.Kind.NOTE, outerRule);
    for (String line : formatLogoLinesForFrame(logoLines, width)) {
      messager.printMessage(Diagnostic.Kind.NOTE, line);
    }
    messager.printMessage(Diagnostic.Kind.NOTE, titleLineVersion());
    messager.printMessage(Diagnostic.Kind.NOTE, innerRule);
    messager.printMessage(Diagnostic.Kind.NOTE, DESCRIPTION_LINE);
    messager.printMessage(Diagnostic.Kind.NOTE, innerRule);
  }

  /**
   * Summary block after generation notes: framed table with {@code [OK]} / {@code [ERROR]} / {@code
   * [WARN]} tags (optionally colored). Width is at least the header width and wide enough for all
   * summary rows.
   */
  public static void printGenerationSummary(
      Messager messager,
      int processedTypes,
      int generatedFiles,
      int errorCount,
      int warningCount,
      boolean ansiColors) {
    int headerW =
        computeHeaderContentWidth(readLogoLines(), List.of(titleLineVersion(), DESCRIPTION_LINE));
    int contentW =
        Math.max(
            headerW > 0 ? headerW : 61,
            computeSummaryMinimumWidth(processedTypes, generatedFiles, errorCount, warningCount));

    String outerRule = "=".repeat(contentW);
    String innerRule = "-".repeat(contentW);
    int resultColWidth = Math.max(0, contentW - STATUS_COL_WIDTH - 2);

    messager.printMessage(Diagnostic.Kind.NOTE, innerRule);
    messager.printMessage(Diagnostic.Kind.NOTE, "");
    messager.printMessage(Diagnostic.Kind.NOTE, "Summary");
    messager.printMessage(Diagnostic.Kind.NOTE, innerRule);

    messager.printMessage(
        Diagnostic.Kind.NOTE, String.format(SUMMARY_TWO_COLUMN_FORMAT, "Status", "Result"));
    messager.printMessage(
        Diagnostic.Kind.NOTE,
        String.format(
            SUMMARY_TWO_COLUMN_FORMAT, "-".repeat(STATUS_COL_WIDTH), "-".repeat(resultColWidth)));

    messager.printMessage(
        Diagnostic.Kind.NOTE,
        summaryRow(ansiColors, SummaryTag.OK, processedPhrase(processedTypes)));
    messager.printMessage(
        Diagnostic.Kind.NOTE,
        summaryRow(ansiColors, SummaryTag.OK, generatedPhrase(generatedFiles)));
    messager.printMessage(
        Diagnostic.Kind.NOTE,
        summaryRow(
            ansiColors,
            errorCount == 0 ? SummaryTag.OK : SummaryTag.ERROR,
            errorsPhrase(errorCount)));
    messager.printMessage(
        Diagnostic.Kind.NOTE,
        summaryRow(
            ansiColors,
            warningCount == 0 ? SummaryTag.OK : SummaryTag.WARN,
            warningsPhrase(warningCount)));

    messager.printMessage(Diagnostic.Kind.NOTE, "");
    messager.printMessage(
        Diagnostic.Kind.NOTE,
        aggregateResultLine(ansiColors, processedTypes, generatedFiles, errorCount, warningCount));
    messager.printMessage(Diagnostic.Kind.NOTE, outerRule);
  }

  /** Same width as header rules (for alignment); falls back when the logo resource is absent. */
  public static int headerContentWidthOrFallback() {
    int contentWidth =
        computeHeaderContentWidth(readLogoLines(), List.of(titleLineVersion(), DESCRIPTION_LINE));
    return contentWidth > 0 ? contentWidth : 61;
  }

  private static int computeSummaryMinimumWidth(
      int processedTypes, int generatedFiles, int errorCount, int warningCount) {
    int minWidth = "Summary".length();
    minWidth =
        Math.max(minWidth, String.format(SUMMARY_TWO_COLUMN_FORMAT, "Status", "Result").length());
    minWidth = Math.max(minWidth, STATUS_COL_WIDTH + 2 + processedPhrase(processedTypes).length());
    minWidth = Math.max(minWidth, STATUS_COL_WIDTH + 2 + generatedPhrase(generatedFiles).length());
    minWidth = Math.max(minWidth, STATUS_COL_WIDTH + 2 + errorsPhrase(errorCount).length());
    minWidth = Math.max(minWidth, STATUS_COL_WIDTH + 2 + warningsPhrase(warningCount).length());
    minWidth =
        Math.max(
            minWidth,
            aggregateResultLine(false, processedTypes, generatedFiles, errorCount, warningCount)
                .length());
    return minWidth;
  }

  private static int computeHeaderContentWidth(List<String> logoLines, List<String> titleLines) {
    int width = 0;
    for (String line : logoLines) {
      width = Math.max(width, line.length());
    }
    for (String line : titleLines) {
      width = Math.max(width, line.length());
    }
    return width;
  }

  /**
   * Keeps logo line spacing consistent: left-align rows to each other, then center the whole block
   * in {@code frameWidth} (per-line centering breaks the art).
   */
  private static List<String> formatLogoLinesForFrame(List<String> logoLines, int frameWidth) {
    int logoMax = logoLines.stream().mapToInt(String::length).max().orElse(0);
    if (logoMax == 0) {
      return List.of();
    }
    if (frameWidth < logoMax) {
      List<String> narrow = new ArrayList<>(logoLines.size());
      for (String line : logoLines) {
        narrow.add(padRightToWidthForLogo(line, logoMax));
      }
      return narrow;
    }
    int blockPad = (frameWidth - logoMax) / 2;
    String left = " ".repeat(blockPad);
    List<String> out = new ArrayList<>(logoLines.size());
    for (String line : logoLines) {
      out.add(String.format("%s%s", left, padRightToWidthForLogo(line, logoMax)));
    }
    return out;
  }

  /**
   * Left-justifies {@code s} in a field of {@code width} (same result as {@code String.format("%-"
   * + width + "s", s)} for typical strings) without building a dynamic format pattern string.
   */
  static String padRightToWidthForLogo(String line, int width) {
    if (line.length() >= width) {
      return line;
    }
    return line + " ".repeat(width - line.length());
  }

  /**
   * Prefer {@link Package#getImplementationVersion()} (set on the built JAR). When the processor
   * runs from exploded {@code target/classes} (e.g. unit tests), that value is often null; fall
   * back to {@code equilibrium-build-version.txt} (filtered from {@code ${project.version}} at
   * build time).
   */
  private static String titleLineVersion() {
    String version = EquilibriumBuildBanner.class.getPackage().getImplementationVersion();
    if (version == null || version.isEmpty()) {
      version = readImplementationVersionFromResource();
    }
    if (version == null || version.isEmpty()) {
      return "Equilibrium";
    }
    return String.format("Equilibrium %s", version);
  }

  private static String readImplementationVersionFromResource() {
    try (InputStream in =
        EquilibriumBuildBanner.class.getResourceAsStream("/equilibrium-build-version.txt")) {
      if (in == null) {
        return null;
      }
      String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
      return raw.isEmpty() ? null : raw;
    } catch (IOException e) {
      return null;
    }
  }

  private static String processedPhrase(int count) {
    return "Processed " + (count == 1 ? "1 type" : count + " types");
  }

  private static String generatedPhrase(int count) {
    return "Generated " + (count == 1 ? "1 file" : count + " files");
  }

  private static String errorsPhrase(int errors) {
    if (errors == 0) {
      return "No errors";
    }
    return errors == 1 ? "1 error" : errors + " errors";
  }

  private static String warningsPhrase(int warnings) {
    if (warnings == 0) {
      return "No warnings";
    }
    return warnings == 1 ? "1 warning" : warnings + " warnings";
  }

  /**
   * One-line outcome after the status table. The parenthetical always lists types and files; error
   * and warning counts are appended only when {@code > 0}. Only the status word(s) are colored when
   * {@code ansi} is true.
   */
  private static String aggregateResultLine(
      boolean ansi, int types, int files, int errors, int warnings) {
    String suffix = " " + parentheticalCounts(types, files, errors, warnings);
    String prefix = "Result: ";
    if (errors > 0) {
      String word = "FAILURE";
      String colored = ansi ? ANSI_RED + word + ANSI_RESET : word;
      return prefix + colored + suffix;
    }
    if (warnings > 0) {
      String phrase = "SUCCESS WITH WARNINGS";
      String colored = ansi ? ANSI_YELLOW + phrase + ANSI_RESET : phrase;
      return prefix + colored + suffix;
    }
    String word = "SUCCESS";
    String colored = ansi ? ANSI_GREEN + word + ANSI_RESET : word;
    return prefix + colored + suffix;
  }

  /**
   * {@code (N types, M files)} plus {@code , K error(s)} and/or {@code , K warning(s)} only when
   * the corresponding count is positive.
   */
  private static String parentheticalCounts(int types, int files, int errors, int warnings) {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    sb.append(types == 1 ? "1 type" : types + " types");
    sb.append(", ");
    sb.append(files == 1 ? "1 file" : files + " files");
    if (errors > 0) {
      sb.append(", ");
      sb.append(errors == 1 ? "1 error" : errors + " errors");
    }
    if (warnings > 0) {
      sb.append(", ");
      sb.append(warnings == 1 ? "1 warning" : warnings + " warnings");
    }
    sb.append(")");
    return sb.toString();
  }

  private static String summaryRow(boolean ansi, SummaryTag tag, String resultText) {
    return formatStatusColumn(ansi, tag) + "  " + resultText;
  }

  /**
   * Pads plain tags to {@link #STATUS_COL_WIDTH} characters; ANSI output matches the same width.
   */
  private static String formatStatusColumn(boolean ansi, SummaryTag tag) {
    String plain =
        switch (tag) {
          case OK -> "[OK]";
          case ERROR -> "[ERROR]";
          case WARN -> "[WARN]";
        };
    if (!ansi) {
      return padRightToWidthForLogo(plain, STATUS_COL_WIDTH);
    }
    String colored =
        switch (tag) {
          case OK -> ANSI_GREEN + plain + ANSI_RESET;
          case ERROR -> ANSI_RED + plain + ANSI_RESET;
          case WARN -> ANSI_YELLOW + plain + ANSI_RESET;
        };
    int pad = STATUS_COL_WIDTH - plain.length();
    // repeat(0) yields ""; max guards a too-narrow STATUS_COL_WIDTH vs. plain tags.
    return colored + " ".repeat(Math.max(0, pad));
  }

  private static List<String> readLogoLines() {
    InputStream in = EquilibriumBuildBanner.class.getResourceAsStream(RESOURCE);
    if (in == null) {
      return List.of();
    }
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
      List<String> lines = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
      return lines;
    } catch (IOException e) {
      return List.of();
    }
  }
}

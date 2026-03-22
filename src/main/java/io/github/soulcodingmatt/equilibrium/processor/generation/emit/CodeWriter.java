package io.github.soulcodingmatt.equilibrium.processor.generation.emit;

import java.io.IOException;
import java.io.Writer;

/**
 * Small helper around {@link Writer} to make code emission easier and more readable. Manages
 * indentation and common line helpers.
 */
public final class CodeWriter {

  private final Writer out;
  private int indentLevel = 0;
  private String indentUnit = "    "; // 4 spaces
  private boolean lineStart = true;

  public CodeWriter(Writer out) {
    this.out = out;
  }

  public void indent() {
    indentLevel++;
  }

  public void dedent() {
    if (indentLevel > 0) {
      indentLevel--;
    }
  }

  public void write(String text) throws IOException {
    if (text == null || text.isEmpty()) {
      return;
    }
    int len = text.length();
    for (int i = 0; i < len; i++) {
      char c = text.charAt(i);
      if (lineStart) {
        writeIndent();
        lineStart = false;
      }
      out.write(c);
      if (c == '\n') {
        lineStart = true;
      }
    }
  }

  public void writeLine(String line) throws IOException {
    if (lineStart) {
      writeIndent();
    }
    out.write(line);
    out.write('\n');
    lineStart = true;
  }

  public void blankLine() throws IOException {
    out.write('\n');
    lineStart = true;
  }

  /** Writes a header (like a method or class header) and opens a block. */
  public void beginBlock(String header) throws IOException {
    writeLine(header + " {");
    indent();
  }

  /** Closes the current block. */
  public void endBlock() throws IOException {
    dedent();
    writeLine("}");
  }

  private void writeIndent() throws IOException {
    for (int i = 0; i < indentLevel; i++) {
      out.write(indentUnit);
    }
  }
}

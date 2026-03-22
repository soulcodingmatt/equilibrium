package io.github.soulcodingmatt.equilibrium.processor.generation.emit.imports;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Aggregates and filters imports for generated sources. */
public final class ImportManager {

  private final Set<String> imports = new HashSet<>();

  public void add(String fullyQualifiedClassName) {
    if (fullyQualifiedClassName != null && !fullyQualifiedClassName.isEmpty()) {
      imports.add(fullyQualifiedClassName);
    }
  }

  public void addAll(Collection<String> fullyQualifiedClassNames) {
    if (fullyQualifiedClassNames != null) {
      for (String f : fullyQualifiedClassNames) {
        add(f);
      }
    }
  }

  public Set<String> getFiltered() {
    return imports.stream()
        .filter(this::isValidImport)
        .collect(Collectors.toCollection(HashSet::new));
  }

  public void writeTo(Writer writer) throws IOException {
    for (String type : getFiltered()) {
      writer.write("import " + type + ";\n");
    }
    writer.write("\n");
  }

  /** Mirrors the validation in the previous monolith class. */
  private boolean isValidImport(String type) {
    if (type == null) return false;
    if (!type.contains(".")) return false;
    if (type.contains("<") || type.contains(">") || type.contains("?")) return false;
    // Allow java.lang only for nested/inner class import edge cases handled previously via
    // contains("$")
    return !type.startsWith("java.lang.") || type.contains("$");
  }
}

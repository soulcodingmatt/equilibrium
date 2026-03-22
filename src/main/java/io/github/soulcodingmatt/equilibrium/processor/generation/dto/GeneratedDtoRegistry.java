package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks simple DTO class names to fully qualified names for import resolution during generation.
 */
final class GeneratedDtoRegistry {

  private static final Map<String, String> ENTRIES = new HashMap<>();

  private GeneratedDtoRegistry() {}

  static void register(String simpleName, String fullQualifiedName) {
    ENTRIES.put(simpleName, fullQualifiedName);
  }

  static String lookup(String simpleName) {
    return ENTRIES.get(simpleName);
  }
}

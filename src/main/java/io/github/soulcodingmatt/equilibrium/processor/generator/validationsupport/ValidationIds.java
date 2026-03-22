package io.github.soulcodingmatt.equilibrium.processor.generator.validationsupport;

/** Shared id-matching for {@code ids} arrays on validation annotations. */
public final class ValidationIds {

  private ValidationIds() {}

  /** True when {@code ids} is empty (apply to all) or contains {@code id}. */
  public static boolean matchesIds(int[] validationIds, int id) {
    if (validationIds.length == 0) {
      return true;
    }
    for (int validationId : validationIds) {
      if (validationId == id) {
        return true;
      }
    }
    return false;
  }
}

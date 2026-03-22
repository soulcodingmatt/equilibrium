package io.github.soulcodingmatt.equilibrium.processor.generator.validationsupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidationIdsTest {

  @Test
  void matchesIds_emptyArray_appliesToAll() {
    assertTrue(ValidationIds.matchesIds(new int[0], 1));
    assertTrue(ValidationIds.matchesIds(new int[0], 99));
  }

  @Test
  void matchesIds_containsId_returnsTrue() {
    assertTrue(ValidationIds.matchesIds(new int[] {1, 2}, 2));
  }

  @Test
  void matchesIds_doesNotContainId_returnsFalse() {
    assertFalse(ValidationIds.matchesIds(new int[] {1, 2}, 3));
  }
}

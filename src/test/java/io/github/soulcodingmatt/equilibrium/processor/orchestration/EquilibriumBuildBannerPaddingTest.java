package io.github.soulcodingmatt.equilibrium.processor.orchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Locks in logo/status column padding before refactors of {@link EquilibriumBuildBanner}. */
class EquilibriumBuildBannerPaddingTest {

  @Test
  void padRightToWidthForLogo_matchesJavaLeftJustifiedField() {
    assertEquals(
        String.format("%-8s", "hi"), EquilibriumBuildBanner.padRightToWidthForLogo("hi", 8));
    assertEquals(
        String.format("%-3s", "ab"), EquilibriumBuildBanner.padRightToWidthForLogo("ab", 3));
  }

  @Test
  void padRightToWidthForLogo_doesNotTruncate_whenLongerThanWidth() {
    assertEquals(
        String.format("%-3s", "hello"), EquilibriumBuildBanner.padRightToWidthForLogo("hello", 3));
  }
}

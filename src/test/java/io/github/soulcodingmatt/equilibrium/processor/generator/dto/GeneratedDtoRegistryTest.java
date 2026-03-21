package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class GeneratedDtoRegistryTest {

  @Test
  void registerAndLookup_returnsFullQualifiedName() {
    String simple = "RegistryProbe" + UUID.randomUUID().toString().replace("-", "");
    String fqn = "com.probe.dto." + simple;
    GeneratedDtoRegistry.register(simple, fqn);
    assertEquals(fqn, GeneratedDtoRegistry.lookup(simple));
  }

  @Test
  void lookupUnknown_returnsNull() {
    String simple = "MissingDto" + UUID.randomUUID().toString().replace("-", "");
    assertNull(GeneratedDtoRegistry.lookup(simple));
  }

  @Test
  void dtoGeneratorFacade_matchesRegistryBehavior() {
    String simple = "FacadeProbe" + UUID.randomUUID().toString().replace("-", "");
    String fqn = "com.facade.dto." + simple;
    DtoGenerator.registerGeneratedDto(simple, fqn);
    assertEquals(fqn, DtoGenerator.lookupGeneratedDto(simple));
  }
}

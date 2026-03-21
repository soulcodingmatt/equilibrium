package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import java.util.Set;
import javax.lang.model.element.TypeElement;

/**
 * Describes which DTO to generate (class, package, naming, ignores, builder flag, annotation id).
 */
public record DtoGeneratorTarget(
    TypeElement classElement,
    String packageName,
    String dtoClassName,
    Set<String> ignoredFields,
    boolean builder,
    int dtoId) {}

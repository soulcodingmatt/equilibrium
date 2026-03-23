package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import java.util.Set;
import javax.lang.model.element.TypeElement;

/**
 * Describes which DTO to generate (class, package, naming, ignores, builder flag, annotation id).
 *
 * @param classElement source type to mirror
 * @param packageName target package for the generated class
 * @param dtoClassName simple name of the generated DTO class
 * @param ignoredFields field names excluded from generation
 * @param builder whether to add Lombok {@code @SuperBuilder}
 * @param dtoId {@code @GenerateDto} id for multi-DTO generation
 */
public record DtoGeneratorTarget(
    TypeElement classElement,
    String packageName,
    String dtoClassName,
    Set<String> ignoredFields,
    boolean builder,
    int dtoId) {}

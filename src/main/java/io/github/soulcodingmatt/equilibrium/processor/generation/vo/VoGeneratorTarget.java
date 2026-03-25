package io.github.soulcodingmatt.equilibrium.processor.generation.vo;

import java.util.Set;
import javax.lang.model.element.TypeElement;

/**
 * Immutable value object capturing all the inputs required to generate a single Value Object type.
 *
 * <p>Mirrors {@code DtoGeneratorTarget} in the DTO layer, making the VO generator's configuration
 * explicit and self-contained.
 *
 * @param classElement the source class being mapped
 * @param packageName the target package for the generated VO
 * @param voClassName the simple name of the generated VO class
 * @param ignoredFields field names to exclude from the generated VO
 * @param generateSetters whether to emit setter methods (false = immutable after construction)
 * @param voId the annotation ID that controls which per-field annotations apply
 */
public record VoGeneratorTarget(
    TypeElement classElement,
    String packageName,
    String voClassName,
    Set<String> ignoredFields,
    boolean generateSetters,
    int voId) {}

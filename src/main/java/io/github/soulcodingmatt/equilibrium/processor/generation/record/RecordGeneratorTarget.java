package io.github.soulcodingmatt.equilibrium.processor.generation.record;

import java.util.Set;
import javax.lang.model.element.TypeElement;

/**
 * Immutable value object capturing all the inputs required to generate a single record type.
 *
 * <p>Mirrors {@code DtoGeneratorTarget} in the DTO layer, making the record generator's
 * configuration explicit and self-contained.
 *
 * @param classElement the source class being mapped
 * @param packageName the target package for the generated record
 * @param recordClassName the simple name of the generated record class
 * @param ignoredFields field names to exclude from the generated record
 * @param recordId the annotation ID that controls which per-field annotations apply
 */
public record RecordGeneratorTarget(
    TypeElement classElement,
    String packageName,
    String recordClassName,
    Set<String> ignoredFields,
    int recordId) {}

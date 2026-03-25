package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import io.github.soulcodingmatt.equilibrium.processor.model.NestedMappingResolver;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;

/** Groups the parameters needed by {@link DtoImportPlanner#writeImports}. */
record DtoImportContext(
    boolean builder,
    int dtoId,
    List<VariableElement> fields,
    Set<String> extraImportsForInheritedDefaults,
    DtoBuilderDefaultSupport builderSupport,
    NestedMappingResolver nestedResolver,
    DtoValidationEmitter validationEmitter) {}

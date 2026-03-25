package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import io.github.soulcodingmatt.equilibrium.annotations.dto.NestedMapping;
import io.github.soulcodingmatt.equilibrium.processor.generation.emit.GeneratorUtility;
import io.github.soulcodingmatt.equilibrium.processor.generation.emit.imports.ImportManager;
import io.github.soulcodingmatt.equilibrium.processor.model.NestedMappingResolver;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.VariableElement;

/** Collects import lines required for a generated DTO source file. */
final class DtoImportPlanner {

  private DtoImportPlanner() {}

  static void writeImports(Writer writer, DtoImportContext context) throws IOException {
    boolean builder = context.builder();
    int dtoId = context.dtoId();
    List<VariableElement> fields = context.fields();
    Set<String> extraImportsForInheritedDefaults = context.extraImportsForInheritedDefaults();
    DtoBuilderDefaultSupport builderSupport = context.builderSupport();
    NestedMappingResolver nestedResolver = context.nestedResolver();
    DtoValidationEmitter validationEmitter = context.validationEmitter();

    Set<String> imports = new HashSet<>();
    Set<VariableElement> fieldsWithNestedMapping = new HashSet<>();

    for (VariableElement field : fields) {
      NestedMapping nestedMapping = field.getAnnotation(NestedMapping.class);

      if (nestedMapping != null) {
        fieldsWithNestedMapping.add(field);

        String dtoImport = nestedResolver.findDtoImportFromSourceClass(nestedMapping);
        if (dtoImport != null) {
          imports.add(dtoImport);
        }
      }
    }

    Set<String> fieldImports =
        fields.stream()
            .filter(field -> !fieldsWithNestedMapping.contains(field))
            .map(field -> field.asType().toString())
            .map(GeneratorUtility::extractBaseType)
            .filter(type -> type.contains("."))
            .collect(Collectors.toSet());
    imports.addAll(fieldImports);

    Set<String> validationImports = validationEmitter.collectImports(fields, dtoId);
    imports.addAll(validationImports);

    if (builder && builderSupport.hasBuilderDefaults(fields)) {
      imports.add("lombok.Builder");
    }

    imports.addAll(builderSupport.getBuilderDefaultImports(fields));
    imports.addAll(extraImportsForInheritedDefaults);

    ImportManager importManager = new ImportManager();
    importManager.addAll(imports);
    importManager.writeTo(writer);
  }
}

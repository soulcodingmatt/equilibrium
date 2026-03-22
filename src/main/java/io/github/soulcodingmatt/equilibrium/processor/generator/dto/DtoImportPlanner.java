package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import io.github.soulcodingmatt.equilibrium.annotations.dto.NestedMapping;
import io.github.soulcodingmatt.equilibrium.processor.generator.GeneratorUtility;
import io.github.soulcodingmatt.equilibrium.processor.generator.NestedMappingResolver;
import io.github.soulcodingmatt.equilibrium.processor.generator.ValidationSupport;
import io.github.soulcodingmatt.equilibrium.processor.generator.imports.ImportManager;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.VariableElement;

/** Collects import lines required for a generated DTO source file. */
public final class DtoImportPlanner {

  private DtoImportPlanner() {}

  public static void writeImports(
      Writer writer,
      boolean builder,
      int dtoId,
      List<VariableElement> fields,
      Set<String> extraImportsForInheritedDefaults,
      DtoBuilderDefaultSupport builderSupport,
      NestedMappingResolver nestedResolver)
      throws IOException {
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

    Set<String> validationImports = ValidationSupport.collectValidationImports(fields, dtoId);
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

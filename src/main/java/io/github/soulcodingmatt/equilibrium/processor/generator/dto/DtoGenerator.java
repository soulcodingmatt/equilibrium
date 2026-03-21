package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import io.github.soulcodingmatt.equilibrium.processor.generator.CodeWriter;
import io.github.soulcodingmatt.equilibrium.processor.generator.GeneratorUtility;
import io.github.soulcodingmatt.equilibrium.processor.generator.GeneratorUtility.FieldInclusionConfig;
import io.github.soulcodingmatt.equilibrium.processor.generator.GeneratorUtility.GeneratorType;
import io.github.soulcodingmatt.equilibrium.processor.generator.NestedMappingResolver;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

public class DtoGenerator {

  public static final String DTO_CLASS = "dtoClass=";

  public static void registerGeneratedDto(String simpleName, String fullQualifiedName) {
    GeneratedDtoRegistry.register(simpleName, fullQualifiedName);
  }

  public static String lookupGeneratedDto(String simpleName) {
    return GeneratedDtoRegistry.lookup(simpleName);
  }

  private final DtoGeneratorTarget target;
  private final TypeElement classElement;
  private final String packageName;
  private final String dtoClassName;
  private final Set<String> ignoredFields;
  private final boolean builder;
  private final int dtoId;
  private final DtoProcessorServices services;

  public DtoGenerator(DtoGeneratorTarget target, DtoProcessorServices services) {
    this.target = target;
    this.classElement = target.classElement();
    this.packageName = target.packageName();
    this.dtoClassName = target.dtoClassName();
    this.ignoredFields =
        target.ignoredFields() != null ? new HashSet<>(target.ignoredFields()) : new HashSet<>();
    this.builder = target.builder();
    this.dtoId = target.dtoId();
    this.services = services;
  }

  public void generate() throws IOException {

    List<VariableElement> fields = getIncludedFields();

    InheritedBuilderDefaultScan inherited =
        InheritedBuilderDefaultScanner.scan(services.trees(), builder, fields);

    NestedMappingResolver nestedResolver =
        new NestedMappingResolver(classElement, services.messager(), dtoClassName);

    DtoBuilderDefaultSupport builderSupport =
        new DtoBuilderDefaultSupport(builder, services.messager(), inherited.safeInitializers());

    JavaFileObject sourceFile =
        services.filer().createSourceFile(packageName + "." + dtoClassName, classElement);

    try (Writer writer = sourceFile.openWriter()) {
      CodeWriter code = new CodeWriter(writer);
      DtoClassWriter classWriter = new DtoClassWriter();

      classWriter.writeFileHeader(writer, packageName, builder);
      DtoImportPlanner.writeImports(
          writer,
          dtoClassName,
          builder,
          dtoId,
          fields,
          new HashSet<>(inherited.extraImports()),
          builderSupport,
          nestedResolver);

      classWriter.beginClass(code, dtoClassName, classElement, builder);

      DtoFieldWriter fieldWriter =
          new DtoFieldWriter(target, services.messager(), nestedResolver, builderSupport);

      for (VariableElement field : fields) {
        fieldWriter.writeField(writer, code, field);
      }

      classWriter.emitConstructor(
          code, dtoClassName, fields, nestedResolver::getTransformedFieldType);

      for (VariableElement field : fields) {
        String type = nestedResolver.getTransformedFieldType(field);
        String name = field.getSimpleName().toString();
        classWriter.emitGetter(code, type, name);
        classWriter.emitSetter(code, type, name);
      }

      classWriter.emitEqualsHashToString(writer, fields, dtoClassName, dtoClassName);

      code.endBlock();
    }
  }

  private List<VariableElement> getIncludedFields() {
    FieldInclusionConfig fieldConfig =
        new FieldInclusionConfig(GeneratorType.DTO, ignoredFields, dtoId);
    return GeneratorUtility.getIncludedFields(classElement, fieldConfig);
  }
}

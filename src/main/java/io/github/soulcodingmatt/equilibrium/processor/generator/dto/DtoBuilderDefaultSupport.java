package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import static io.github.soulcodingmatt.equilibrium.processor.generator.imports.TypeNames.*;

import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
import io.github.soulcodingmatt.equilibrium.processor.generator.DefaultValueResolver;
import io.github.soulcodingmatt.equilibrium.processor.generator.GeneratorUtility;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

/** Logic for {@link DtoBuilderDefault}, Lombok {@code @Builder.Default}, and related imports. */
public final class DtoBuilderDefaultSupport {

  private final boolean builder;
  private final Messager messager;
  private final Map<VariableElement, String> inheritedDefaultInitializers;

  public DtoBuilderDefaultSupport(
      boolean builder,
      Messager messager,
      Map<VariableElement, String> inheritedDefaultInitializers) {
    this.builder = builder;
    this.messager = messager;
    this.inheritedDefaultInitializers = inheritedDefaultInitializers;
  }

  public boolean hasBuilderDefaults(List<VariableElement> fields) {
    for (VariableElement field : fields) {
      if (field.getAnnotation(DtoBuilderDefault.class) != null) {
        return true;
      }
      if (builder && hasExistingBuilderDefault(field)) {
        return true;
      }
    }
    return false;
  }

  public Set<String> getBuilderDefaultImports(List<VariableElement> fields) {
    Set<String> imports = new HashSet<>();

    if (!builder) {
      return imports;
    }

    for (VariableElement field : fields) {
      DtoBuilderDefault builderDefault = field.getAnnotation(DtoBuilderDefault.class);
      if (builderDefault != null) {
        String fieldType = field.asType().toString();
        if (DefaultValueResolver.isCollectionType(fieldType)) {
          addCollectionImports(imports, fieldType);
        } else if (DefaultValueResolver.isOptionalType(fieldType)) {
          imports.add(JAVA_UTIL_OPTIONAL);
        }

        if (isEnumType(field)) {
          addEnumImports(imports, field);
        }
      }
    }

    return imports;
  }

  public void writeBuilderDefaultAnnotation(Writer writer, VariableElement field)
      throws IOException {
    if (!builder) {
      return;
    }

    DtoBuilderDefault builderDefault = field.getAnnotation(DtoBuilderDefault.class);
    if (builderDefault != null) {
      if (field.getModifiers().contains(Modifier.FINAL)) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            "@DtoBuilderDefault cannot be applied to final fields: " + field.getSimpleName(),
            field);
        return;
      }
      boolean willInherit =
          builderDefault.inherit() && inheritedDefaultInitializers.containsKey(field);
      boolean hasExplicit = hasExplicitDtoDefault(field, builderDefault);
      if (hasExplicit || willInherit) {
        writer.write("    @Builder.Default\n");
      }
      return;
    }

    if (hasExistingBuilderDefault(field) && inheritedDefaultInitializers.containsKey(field)) {
      writer.write("    @Builder.Default\n");
    }
  }

  public String getFieldDeclaration(VariableElement field, String transformedType, String name) {
    if (!builder) {
      return transformedType + " " + name;
    }

    DtoBuilderDefault builderDefault = field.getAnnotation(DtoBuilderDefault.class);
    if (builderDefault != null) {
      String defaultValue = getBuilderDefaultValue(field, builderDefault);
      if (defaultValue != null && !defaultValue.isEmpty()) {
        return transformedType + " " + name + " = " + defaultValue;
      }
    }

    if ((builderDefault == null || builderDefault.inherit()) && hasExistingBuilderDefault(field)) {
      String existingDefault = getExistingBuilderDefaultValue(field);
      if (existingDefault != null && !existingDefault.isEmpty()) {
        return transformedType + " " + name + " = " + existingDefault;
      }
    }

    return transformedType + " " + name;
  }

  private String getBuilderDefaultValue(VariableElement field, DtoBuilderDefault builderDefault) {
    String fieldType = field.asType().toString();

    String typeSpecificValue =
        DefaultValueResolver.getTypeSpecificValue(field, builderDefault, messager);
    if (typeSpecificValue != null) {
      return typeSpecificValue;
    }

    if (!builderDefault.inherit()) {
      return null;
    }

    String annotationValue = builderDefault.value();
    if (!annotationValue.isEmpty()) {
      return DefaultValueResolver.processAnnotationValue(
          field, fieldType, annotationValue, messager);
    }

    if (DefaultValueResolver.isCollectionType(fieldType)) {
      return DefaultValueResolver.getCollectionDefaultValue(fieldType);
    } else if (DefaultValueResolver.isOptionalType(fieldType)) {
      return "Optional.empty()";
    }

    messager.printMessage(
        Diagnostic.Kind.ERROR,
        "@DtoBuilderDefault requires a value for non-collection, non-Optional field: "
            + field.getSimpleName(),
        field);
    return null;
  }

  private boolean hasExplicitDtoDefault(VariableElement field, DtoBuilderDefault builderDefault) {
    return DefaultValueResolver.hasExplicitDtoDefault(field, builderDefault);
  }

  private boolean hasExistingBuilderDefault(VariableElement field) {
    return field.getAnnotationMirrors().stream()
        .anyMatch(mirror -> mirror.getAnnotationType().toString().equals("lombok.Builder.Default"));
  }

  private void addCollectionImports(Set<String> imports, String fieldType) {
    String baseType = GeneratorUtility.extractBaseType(fieldType);
    switch (baseType) {
      case JAVA_UTIL_LIST, "List" -> {
        imports.add(JAVA_UTIL_LIST);
        imports.add("java.util.ArrayList");
      }
      case JAVA_UTIL_SET, "Set" -> {
        imports.add(JAVA_UTIL_SET);
        imports.add("java.util.HashSet");
      }
      case JAVA_UTIL_MAP, "Map" -> {
        imports.add(JAVA_UTIL_MAP);
        imports.add("java.util.HashMap");
      }
      default -> throw new IllegalStateException("Unexpected value: " + baseType);
    }
  }

  private boolean isEnumType(VariableElement field) {
    return field.asType().getKind() == TypeKind.DECLARED
        && ((DeclaredType) field.asType()).asElement().getKind() == ElementKind.ENUM;
  }

  private void addEnumImports(Set<String> imports, VariableElement field) {
    if (isEnumType(field)) {
      DeclaredType declaredType = (DeclaredType) field.asType();
      TypeElement enumElement = (TypeElement) declaredType.asElement();
      String enumClassName = enumElement.getQualifiedName().toString();

      if (enumClassName.contains(".") && !enumClassName.startsWith("java.lang.")) {
        imports.add(enumClassName);
      }
    }
  }

  private String getExistingBuilderDefaultValue(VariableElement field) {
    return inheritedDefaultInitializers.get(field);
  }
}

package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import io.github.soulcodingmatt.equilibrium.annotations.dto.NestedMapping;
import io.github.soulcodingmatt.equilibrium.processor.generation.emit.CodeWriter;
import io.github.soulcodingmatt.equilibrium.processor.generation.emit.CustomObjectWarning;
import io.github.soulcodingmatt.equilibrium.processor.model.NestedMappingResolver;
import java.io.IOException;
import java.io.Writer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/** Emits a single DTO field: validation annotations, builder default, type, and declaration. */
public final class DtoFieldWriter {

  private final DtoGeneratorTarget target;
  private final Messager messager;
  private final NestedMappingResolver nestedResolver;
  private final DtoBuilderDefaultSupport builderSupport;
  private final DtoValidationEmitter validationEmitter;

  public DtoFieldWriter(
      DtoGeneratorTarget target,
      Messager messager,
      NestedMappingResolver nestedResolver,
      DtoBuilderDefaultSupport builderSupport,
      DtoValidationEmitter validationEmitter) {
    this.target = target;
    this.messager = messager;
    this.nestedResolver = nestedResolver;
    this.builderSupport = builderSupport;
    this.validationEmitter = validationEmitter;
  }

  public void writeField(Writer writer, CodeWriter code, VariableElement field) throws IOException {
    validationEmitter.emitFieldAnnotations(writer, field, target.dtoId());

    builderSupport.writeBuilderDefaultAnnotation(writer, field);

    String transformedType = nestedResolver.getTransformedFieldType(field);
    String name = field.getSimpleName().toString();

    TypeMirror fieldType = field.asType();
    NestedMapping nestedMapping = field.getAnnotation(NestedMapping.class);
    if (nestedMapping == null) {
      new CustomObjectWarning(messager, target.classElement(), target.dtoClassName())
          .check(field, fieldType);
    }

    String fieldDeclaration = builderSupport.getFieldDeclaration(field, transformedType, name);
    code.writeLine("private " + fieldDeclaration + ";");
    code.blankLine();
  }
}

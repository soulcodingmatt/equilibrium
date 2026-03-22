package io.github.soulcodingmatt.equilibrium.processor.generator;

import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Invokes {@link DefaultValueResolver} against {@code com.acme.probe.DefaultValueProbe} fields so
 * tests can assert behavior without the full DTO pipeline.
 */
@SupportedAnnotationTypes("*")
public class DefaultValueResolverHarnessProcessor extends AbstractProcessor {

  private static final String PROBE = "com.acme.probe.DefaultValueProbe";

  static volatile String typeSpecificInt;
  static volatile String typeSpecificLong;
  static volatile String typeSpecificShort;
  static volatile String typeSpecificByte;
  static volatile String typeSpecificFloat;
  static volatile String typeSpecificDouble;
  static volatile String typeSpecificBooleanTrue;
  static volatile String typeSpecificBooleanFalse;
  static volatile String typeSpecificChar;
  static volatile String typeSpecificIntegerWrapper;
  static volatile String typeSpecificString;
  static volatile String typeSpecificEnum;

  static volatile String legacyValuePrimitiveInt;
  static volatile String legacyValueIntegerWrapper;

  static volatile boolean hasOtherParamsEmptyDtoDefault;
  static volatile boolean hasExplicitEmptyOnPlainInt;
  static volatile boolean hasExplicitEmptyOnList;
  static volatile boolean hasExplicitEmptyOnOptional;
  static volatile boolean hasOtherParamsWithString;

  static void reset() {
    typeSpecificInt = null;
    typeSpecificLong = null;
    typeSpecificShort = null;
    typeSpecificByte = null;
    typeSpecificFloat = null;
    typeSpecificDouble = null;
    typeSpecificBooleanTrue = null;
    typeSpecificBooleanFalse = null;
    typeSpecificChar = null;
    typeSpecificIntegerWrapper = null;
    typeSpecificString = null;
    typeSpecificEnum = null;
    legacyValuePrimitiveInt = null;
    legacyValueIntegerWrapper = null;
    hasOtherParamsEmptyDtoDefault = false;
    hasExplicitEmptyOnPlainInt = false;
    hasExplicitEmptyOnList = false;
    hasExplicitEmptyOnOptional = false;
    hasOtherParamsWithString = false;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      return false;
    }
    for (Element root : roundEnv.getRootElements()) {
      if (!(root instanceof TypeElement te)) {
        continue;
      }
      if (!te.getQualifiedName().toString().contentEquals(PROBE)) {
        continue;
      }
      var messager = processingEnv.getMessager();
      for (VariableElement field : fieldsOf(te)) {
        String name = field.getSimpleName().toString();
        DtoBuilderDefault ann = field.getAnnotation(DtoBuilderDefault.class);
        switch (name) {
          case "intVal" ->
              typeSpecificInt = DefaultValueResolver.getTypeSpecificValue(field, ann, messager);
          case "longVal" ->
              typeSpecificLong = DefaultValueResolver.getTypeSpecificValue(field, ann, messager);
          case "shortVal" ->
              typeSpecificShort = DefaultValueResolver.getTypeSpecificValue(field, ann, messager);
          case "byteVal" ->
              typeSpecificByte = DefaultValueResolver.getTypeSpecificValue(field, ann, messager);
          case "floatVal" ->
              typeSpecificFloat = DefaultValueResolver.getTypeSpecificValue(field, ann, messager);
          case "doubleVal" ->
              typeSpecificDouble = DefaultValueResolver.getTypeSpecificValue(field, ann, messager);
          case "boolTrue" ->
              typeSpecificBooleanTrue =
                  DefaultValueResolver.getTypeSpecificValue(field, ann, messager);
          case "boolFalse" ->
              typeSpecificBooleanFalse =
                  DefaultValueResolver.getTypeSpecificValue(field, ann, messager);
          case "charVal" ->
              typeSpecificChar = DefaultValueResolver.getTypeSpecificValue(field, ann, messager);
          case "integerWrapper" ->
              typeSpecificIntegerWrapper =
                  DefaultValueResolver.getTypeSpecificValue(field, ann, messager);
          case "strVal" ->
              typeSpecificString = DefaultValueResolver.getTypeSpecificValue(field, ann, messager);
          case "hueVal" ->
              typeSpecificEnum = DefaultValueResolver.getTypeSpecificValue(field, ann, messager);
          case "legacyPrimitiveInt" -> {
            legacyValuePrimitiveInt =
                DefaultValueResolver.processAnnotationValue(
                    field, field.asType().toString(), ann.value(), messager);
          }
          case "legacyInteger" -> {
            legacyValueIntegerWrapper =
                DefaultValueResolver.processAnnotationValue(
                    field, field.asType().toString(), ann.value(), messager);
          }
          case "emptyDtoDefault" ->
              hasOtherParamsEmptyDtoDefault = DefaultValueResolver.hasAnyOtherParameterSet(ann);
          case "plainInt" -> {
            hasExplicitEmptyOnPlainInt = DefaultValueResolver.hasExplicitDtoDefault(field, ann);
          }
          case "emptyList" ->
              hasExplicitEmptyOnList = DefaultValueResolver.hasExplicitDtoDefault(field, ann);
          case "emptyOpt" ->
              hasExplicitEmptyOnOptional = DefaultValueResolver.hasExplicitDtoDefault(field, ann);
          case "withString" ->
              hasOtherParamsWithString = DefaultValueResolver.hasAnyOtherParameterSet(ann);
          default -> {
            /* Ignore fields not mapped by this harness (probe may add new fields later). */
          }
        }
      }
    }
    return false;
  }

  private static List<VariableElement> fieldsOf(TypeElement type) {
    List<VariableElement> out = new ArrayList<>();
    for (Element enclosed : type.getEnclosedElements()) {
      if (enclosed instanceof VariableElement ve) {
        out.add(ve);
      }
    }
    return out;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}

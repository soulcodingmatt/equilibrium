package io.github.soulcodingmatt.equilibrium.processor.generation.defaults;

import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
import javax.annotation.processing.Messager;
import javax.lang.model.element.VariableElement;

/**
 * Facade for {@code @DtoBuilderDefault} default value resolution; implementation lives in {@code
 * defaultvalue} subpackage classes.
 */
public final class DefaultValueResolver {

  private DefaultValueResolver() {
    throw new AssertionError("You should not be here!");
  }

  public static String getTypeSpecificValue(
      VariableElement field, DtoBuilderDefault builderDefault, Messager messager) {
    return DefaultValueTypeSpecificResolution.getTypeSpecificValue(field, builderDefault, messager);
  }

  public static boolean hasAnyOtherParameterSet(DtoBuilderDefault builderDefault) {
    return BuilderDefaultAnnotationState.hasAnyOtherParameterSet(builderDefault);
  }

  public static String processAnnotationValue(
      VariableElement field, String fieldType, String annotationValue, Messager messager) {
    return DefaultValueAnnotationParsing.processAnnotationValue(
        field, fieldType, annotationValue, messager);
  }

  public static String processStringValue(String annotationValue) {
    return DefaultValueStringLiterals.processStringValue(annotationValue);
  }

  public static String processPrimitiveValue(
      VariableElement field, String fieldType, String annotationValue, Messager messager) {
    return DefaultValueNumericParsing.processPrimitiveValue(
        field, fieldType, annotationValue, messager);
  }

  public static String processWrapperValue(
      VariableElement field, String fieldType, String annotationValue, Messager messager) {
    return DefaultValueNumericParsing.processWrapperValue(
        field, fieldType, annotationValue, messager);
  }

  public static String processEnumValue(
      VariableElement field, String annotationValue, Messager messager) {
    return DefaultValueEnumReference.processEnumValue(field, annotationValue, messager);
  }

  public static boolean isCollectionType(String fieldType) {
    return DefaultValueTypePredicates.isCollectionType(fieldType);
  }

  public static boolean isOptionalType(String fieldType) {
    return DefaultValueTypePredicates.isOptionalType(fieldType);
  }

  public static boolean isStringType(String fieldType) {
    return DefaultValueTypePredicates.isStringType(fieldType);
  }

  public static boolean isPrimitiveType(String fieldType) {
    return DefaultValueTypePredicates.isPrimitiveType(fieldType);
  }

  public static boolean isWrapperType(String fieldType) {
    return DefaultValueTypePredicates.isWrapperType(fieldType);
  }

  public static String getCollectionDefaultValue(String fieldType) {
    return DefaultValueCollectionDefaults.getCollectionDefaultValue(fieldType);
  }

  public static boolean hasExplicitDtoDefault(
      VariableElement field, DtoBuilderDefault builderDefault) {
    return BuilderDefaultAnnotationState.hasExplicitDtoDefault(field, builderDefault);
  }
}

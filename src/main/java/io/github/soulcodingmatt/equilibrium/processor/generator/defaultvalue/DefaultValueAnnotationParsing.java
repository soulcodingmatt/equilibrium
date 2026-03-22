package io.github.soulcodingmatt.equilibrium.processor.generator.defaultvalue;

import javax.annotation.processing.Messager;
import javax.lang.model.element.VariableElement;

/** Routes legacy {@code value()} text to the correct per-kind parser. */
public final class DefaultValueAnnotationParsing {

  private DefaultValueAnnotationParsing() {}

  public static String processAnnotationValue(
      VariableElement field, String fieldType, String annotationValue, Messager messager) {
    if (DefaultValueTypePredicates.isEnumField(field)) {
      return DefaultValueEnumReference.processEnumValue(field, annotationValue, messager);
    }
    if (DefaultValueTypePredicates.isStringType(fieldType)) {
      return DefaultValueStringLiterals.processStringValue(annotationValue);
    }
    if (DefaultValueTypePredicates.isPrimitiveType(fieldType)) {
      return DefaultValueNumericParsing.processPrimitiveValue(
          field, fieldType, annotationValue, messager);
    }
    if (DefaultValueTypePredicates.isWrapperType(fieldType)) {
      return DefaultValueNumericParsing.processWrapperValue(
          field, fieldType, annotationValue, messager);
    }
    return annotationValue;
  }
}

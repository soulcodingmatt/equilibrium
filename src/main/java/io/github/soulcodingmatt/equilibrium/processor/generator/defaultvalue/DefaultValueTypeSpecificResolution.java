package io.github.soulcodingmatt.equilibrium.processor.generator.defaultvalue;

import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
import javax.annotation.processing.Messager;
import javax.lang.model.element.VariableElement;

/** Maps type-specific {@link DtoBuilderDefault} attributes to Java initializer text. */
public final class DefaultValueTypeSpecificResolution {

  private DefaultValueTypeSpecificResolution() {}

  public static String getTypeSpecificValue(
      VariableElement field, DtoBuilderDefault builderDefault, Messager messager) {
    String fieldType = field.asType().toString();

    String primitive = DefaultValuePrimitiveResolution.tryResolve(builderDefault, fieldType);
    if (primitive != null) {
      return primitive;
    }

    if (DefaultValueTypePredicates.isStringType(fieldType)
        && !builderDefault.stringValue().isEmpty()) {
      return DefaultValueStringLiterals.processStringValue(builderDefault.stringValue());
    }

    if (DefaultValueTypePredicates.isEnumField(field) && !builderDefault.enumValue().isEmpty()) {
      return DefaultValueEnumReference.processEnumValue(
          field, builderDefault.enumValue(), messager);
    }

    return null;
  }
}

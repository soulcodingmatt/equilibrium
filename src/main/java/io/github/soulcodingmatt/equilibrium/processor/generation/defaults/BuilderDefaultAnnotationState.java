package io.github.soulcodingmatt.equilibrium.processor.generation.defaults;

import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
import javax.lang.model.element.VariableElement;

/**
 * Whether {@link DtoBuilderDefault} carries enough information to treat the field as having an
 * explicit default (including collection / optional empty defaults).
 */
public final class BuilderDefaultAnnotationState {

  private BuilderDefaultAnnotationState() {}

  public static boolean hasAnyOtherParameterSet(DtoBuilderDefault builderDefault) {
    return hasAnyTypedOrLegacyParameter(builderDefault);
  }

  public static boolean hasExplicitDtoDefault(
      VariableElement field, DtoBuilderDefault builderDefault) {
    String type = field.asType().toString();
    return hasAnyTypedOrLegacyParameter(builderDefault)
        || DefaultValueTypePredicates.isCollectionType(type)
        || DefaultValueTypePredicates.isOptionalType(type);
  }

  private static boolean hasAnyTypedOrLegacyParameter(DtoBuilderDefault builderDefault) {
    return !builderDefault.stringValue().isEmpty()
        || builderDefault.intValue() != Integer.MIN_VALUE
        || builderDefault.longValue() != Long.MIN_VALUE
        || builderDefault.shortValue() != Short.MIN_VALUE
        || builderDefault.byteValue() != Byte.MIN_VALUE
        || builderDefault.floatValue() != Float.MIN_VALUE
        || builderDefault.doubleValue() != Double.MIN_VALUE
        || builderDefault.booleanValue()
        || builderDefault.charValue() != '\0'
        || !builderDefault.enumValue().isEmpty()
        || !builderDefault.value().isEmpty();
  }
}

package io.github.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation that holds multiple {@link NestedMapping} annotations on a single field.
 *
 * <p>Note: {@link NestedMapping} applies the same DTO type to <em>all</em> generated DTO variants.
 * If you need <em>different</em> DTO types per {@code @GenerateDto} id, use {@link
 * NestedDtoMapping} / {@link NestedDtoMappings} instead, which support per-id scoping via their
 * {@code ids} parameter.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface NestedMappings {
  NestedMapping[] value();
}

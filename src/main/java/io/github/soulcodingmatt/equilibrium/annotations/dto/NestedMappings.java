package io.github.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for multiple nested DTO mappings.
 * Use this when you need different DTO classes for different DTO IDs on the same field.
 * 
 * Example:
 * <pre>
 * &#64;NestedMappings({
 *     &#64;NestedMapping(ids = 1, dtoClass = FunkyShitDto.class),
 *     &#64;NestedMapping(ids = 2, dtoClass = DoTheFunkyChickenDto.class)
 * })
 * private Body body;
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface NestedMappings {
    NestedMapping[] value();
} 
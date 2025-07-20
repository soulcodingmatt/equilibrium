package io.github.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a field to a specific DTO class for all generated DTOs.
 * This annotation can only be used once per field.
 * 
 * Example:
 * <pre>
 * &#64;NestedMapping(dtoClass = VoiceDto.class)
 * private Voice voice;
 * </pre>
 * 
 * The specified DTO class will be used for this field in all generated DTOs.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface NestedMapping {
    
    /**
     * The DTO class to use for this field in all generated DTOs.
     * 
     * @return the DTO class
     */
    Class<?> dtoClass();
} 
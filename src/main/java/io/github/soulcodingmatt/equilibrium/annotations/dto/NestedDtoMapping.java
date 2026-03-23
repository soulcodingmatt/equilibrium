package io.github.soulcodingmatt.equilibrium.annotations.dto;

import java.lang.annotation.*;

/**
 * Maps a field to different DTO classes for different DTO generation IDs using string-based class
 * names. This annotation avoids TypeMirror resolution issues that occur with multiple Class
 * references.
 *
 * <p>Example:
 *
 * <pre>
 * &#64;NestedDtoMapping(ids = 1, dtoClassName = "io.github.soulcodingmatt.customsamples.birds.dto.NestedBodyDto")
 * &#64;NestedDtoMapping(ids = 2, dtoClassName = "io.github.soulcodingmatt.customsamples.birds.dto.NestedBodyAltDto")
 * private Body body;
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(NestedDtoMappings.class)
public @interface NestedDtoMapping {

  /**
   * The IDs of the DTOs for which this mapping applies. If empty, applies to all DTOs.
   *
   * @return array of DTO IDs
   */
  int[] ids() default {};

  /**
   * The fully qualified class name of the DTO class to use for this mapping.
   *
   * @return the DTO class name as a string
   */
  String dtoClassName();
}

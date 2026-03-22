package io.github.soulcodingmatt.equilibrium.processor.util.validationconflict;

import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.experimental.validation.record.ValidateRecord;
import io.github.soulcodingmatt.equilibrium.experimental.validation.vo.ValidateVo;
import java.util.List;

/**
 * Collects which nested Jakarta validations are active on a single {@code @Validate*} annotation.
 */
public final class ValidationAnnotationCollector {

  private ValidationAnnotationCollector() {}

  public static List<ValidationInfo> collectActiveValidations(ValidateDto validateDto) {
    return ValidationDtoActiveAnnotations.collect(validateDto);
  }

  public static List<ValidationInfo> collectActiveRecordValidations(ValidateRecord validateRecord) {
    return ValidationRecordActiveAnnotations.collect(validateRecord);
  }

  public static List<ValidationInfo> collectActiveVoValidations(ValidateVo validateVo) {
    return ValidationVoActiveAnnotations.collect(validateVo);
  }
}

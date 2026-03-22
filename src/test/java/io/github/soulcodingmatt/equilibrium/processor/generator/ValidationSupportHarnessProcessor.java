package io.github.soulcodingmatt.equilibrium.processor.generator;

import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import io.github.soulcodingmatt.equilibrium.experimental.validation.record.ValidateRecord;
import io.github.soulcodingmatt.equilibrium.experimental.validation.vo.ValidateVo;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Runs {@link ValidationSupport} against compiled probe types so tests can assert imports and
 * emitted validation text without the full Equilibrium pipeline.
 */
@SupportedAnnotationTypes("*")
public class ValidationSupportHarnessProcessor extends AbstractProcessor {

  static volatile Set<String> dtoImportsForId1;
  static volatile Set<String> dtoImportsForId2;
  static volatile String dtoWriteTypeSafeBlock;

  static volatile Set<String> recordImportsForId1;
  static volatile String recordWriteTypeSafeBlock;

  static volatile Set<String> voImportsForId1;
  static volatile String voWriteTypeSafeBlock;

  static volatile boolean shouldApplyDtoId1;
  static volatile boolean shouldApplyDtoId2;

  private static final String DTO_PROBE = "com.acme.probe.ValidationDtoProbe";
  private static final String RECORD_PROBE = "com.acme.probe.ValidationRecordProbe";
  private static final String VO_PROBE = "com.acme.probe.ValidationVoProbe";

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      return false;
    }
    for (Element root : roundEnv.getRootElements()) {
      if (!(root instanceof TypeElement te)) {
        continue;
      }
      String qn = te.getQualifiedName().toString();
      if (qn.contentEquals(DTO_PROBE)) {
        captureDto(te);
      } else if (qn.contentEquals(RECORD_PROBE)) {
        captureRecord(te);
      } else if (qn.contentEquals(VO_PROBE)) {
        captureVo(te);
      }
    }
    return false;
  }

  private void captureDto(TypeElement te) {
    List<VariableElement> fields = fieldsOf(te);
    dtoImportsForId1 = ValidationSupport.collectValidationImports(fields, 1);
    dtoImportsForId2 = ValidationSupport.collectValidationImports(fields, 2);

    VariableElement first = findField(te, "withId1");
    ValidateDto[] dtos = first.getAnnotationsByType(ValidateDto.class);
    ValidateDto firstDto = dtos[0];
    shouldApplyDtoId1 = ValidationSupport.shouldApplyValidation(firstDto, 1);
    shouldApplyDtoId2 = ValidationSupport.shouldApplyValidation(firstDto, 2);

    try {
      StringWriter sw = new StringWriter();
      ValidationSupport.writeTypeSafeValidations(sw, firstDto);
      dtoWriteTypeSafeBlock = sw.toString();
    } catch (java.io.IOException e) {
      throw new AssertionError(e);
    }
  }

  private void captureRecord(TypeElement te) {
    List<VariableElement> fields = fieldsOf(te);
    recordImportsForId1 = ValidationSupport.collectRecordValidationImports(fields, 1);
    VariableElement first = findField(te, "component");
    ValidateRecord[] rs = first.getAnnotationsByType(ValidateRecord.class);
    try {
      StringWriter sw = new StringWriter();
      ValidationSupport.writeTypeSafeRecordValidations(sw, rs[0]);
      recordWriteTypeSafeBlock = sw.toString();
    } catch (java.io.IOException e) {
      throw new AssertionError(e);
    }
  }

  private void captureVo(TypeElement te) {
    List<VariableElement> fields = fieldsOf(te);
    voImportsForId1 = ValidationSupport.collectVoValidationImports(fields, 1);
    VariableElement first = findField(te, "field");
    ValidateVo[] vs = first.getAnnotationsByType(ValidateVo.class);
    try {
      StringWriter sw = new StringWriter();
      ValidationSupport.writeTypeSafeVoValidations(sw, vs[0]);
      voWriteTypeSafeBlock = sw.toString();
    } catch (java.io.IOException e) {
      throw new AssertionError(e);
    }
  }

  private static List<VariableElement> fieldsOf(TypeElement te) {
    List<VariableElement> out = new ArrayList<>();
    for (Element e : te.getEnclosedElements()) {
      if (e.getKind() == ElementKind.FIELD) {
        out.add((VariableElement) e);
      }
    }
    return out;
  }

  private static VariableElement findField(TypeElement te, String name) {
    for (Element e : te.getEnclosedElements()) {
      if (e.getKind() == ElementKind.FIELD && e.getSimpleName().contentEquals(name)) {
        return (VariableElement) e;
      }
    }
    throw new IllegalStateException("Field not found: " + name);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}

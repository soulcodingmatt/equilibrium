package io.github.soulcodingmatt.equilibrium.processor.validation.analysis;

import io.github.soulcodingmatt.equilibrium.experimental.validation.dto.ValidateDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Invokes {@link ValidationConflictUtil} on {@code com.acme.probe.ValidationConflictProbe} so tests
 * can lock in validation messages without the full {@code EquilibriumProcessor} pipeline.
 */
@SupportedAnnotationTypes("*")
public class ValidationConflictHarnessProcessor extends AbstractProcessor {

  static final String PROBE = "com.acme.probe.ValidationConflictProbe";

  static volatile List<String> positiveNegativeErrors;
  static volatile List<String> notNullOnPrimitiveErrors;
  static volatile List<String> notBlankOnIntErrors;

  static void reset() {
    positiveNegativeErrors = null;
    notNullOnPrimitiveErrors = null;
    notBlankOnIntErrors = null;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      return false;
    }
    for (Element root : roundEnv.getRootElements()) {
      if (!(root instanceof TypeElement te)) {
        continue;
      }
      if (!te.getQualifiedName().toString().contentEquals(PROBE)) {
        continue;
      }
      for (VariableElement field : fieldsOf(te)) {
        String name = field.getSimpleName().toString();
        ValidateDto[] dtos = field.getAnnotationsByType(ValidateDto.class);
        List<String> errors = ValidationConflictUtil.validateField(field, dtos);
        switch (name) {
          case "positiveNegative" -> positiveNegativeErrors = new ArrayList<>(errors);
          case "notNullOnPrimitive" -> notNullOnPrimitiveErrors = new ArrayList<>(errors);
          case "notBlankOnInt" -> notBlankOnIntErrors = new ArrayList<>(errors);
          default -> {
            // other probe fields reserved for future cases
          }
        }
      }
    }
    return false;
  }

  private static List<VariableElement> fieldsOf(TypeElement type) {
    List<VariableElement> out = new ArrayList<>();
    for (Element enclosed : type.getEnclosedElements()) {
      if (enclosed instanceof VariableElement ve) {
        out.add(ve);
      }
    }
    return out;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }
}

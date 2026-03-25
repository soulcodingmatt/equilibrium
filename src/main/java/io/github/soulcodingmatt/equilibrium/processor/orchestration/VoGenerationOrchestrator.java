package io.github.soulcodingmatt.equilibrium.processor.orchestration;

import static io.github.soulcodingmatt.equilibrium.processor.orchestration.GenerationOrchestratorSupport.*;

import io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVo;
import io.github.soulcodingmatt.equilibrium.annotations.vo.IgnoreVo;
import io.github.soulcodingmatt.equilibrium.processor.config.EquilibriumConfig;
import io.github.soulcodingmatt.equilibrium.processor.generation.vo.VoGenerator;
import io.github.soulcodingmatt.equilibrium.processor.generation.vo.VoGeneratorTarget;
import io.github.soulcodingmatt.equilibrium.processor.validation.codegen.VoValidationEmitterImpl;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;

/** Handles all Value Object-related code generation for a single source class. */
class VoGenerationOrchestrator {

  private static final String TYPE_LABEL = "VO";
  private static final String DISPLAY_NAME = "Value Object";

  private final EquilibriumConfig config;
  private final DiagnosticReporter reporter;
  private final Filer filer;

  VoGenerationOrchestrator(EquilibriumConfig config, DiagnosticReporter reporter, Filer filer) {
    this.config = config;
    this.reporter = reporter;
    this.filer = filer;
  }

  void processGenerateVos(TypeElement classElement) {
    GenerateVo[] annotations = classElement.getAnnotationsByType(GenerateVo.class);
    if (annotations.length == 0) {
      return;
    }

    List<ResolvedTarget> targets = resolveTargets(classElement, annotations);

    if (!validateUniqueCombinations(classElement, reporter, TYPE_LABEL, "GenerateVo", targets)) {
      return;
    }

    Set<Integer> validIds = collectExplicitIds(targets);

    validateIgnoreAnnotationIds(
        classElement,
        validIds,
        reporter,
        "IgnoreVo",
        "GenerateVo",
        field -> {
          IgnoreVo ann = field.getAnnotation(IgnoreVo.class);
          return ann != null ? ann.ids() : null;
        });

    for (GenerateVo annotation : annotations) {
      processGenerateVo(classElement, annotation);
    }
  }

  private void processGenerateVo(TypeElement classElement, GenerateVo annotation) {
    String packageName = config.validateAndGetPackage(annotation.pkg(), TYPE_LABEL);
    String className = resolveClassName(classElement, annotation.name(), config, TYPE_LABEL);
    String fullClassName = packageName + "." + className;
    Set<String> ignoredFields =
        collectIgnoredFields(annotation.ignore(), classElement, config, reporter);

    executeGeneration(
        classElement,
        reporter,
        DISPLAY_NAME,
        fullClassName,
        () -> {
          VoGenerator generator =
              new VoGenerator(
                  new VoGeneratorTarget(
                      classElement,
                      packageName,
                      className,
                      ignoredFields,
                      annotation.setters(),
                      annotation.id()),
                  filer,
                  new VoValidationEmitterImpl());
          generator.generate();
        });
  }

  private List<ResolvedTarget> resolveTargets(TypeElement classElement, GenerateVo[] annotations) {
    return Arrays.stream(annotations)
        .map(
            a ->
                new ResolvedTarget(
                    config.validateAndGetPackage(a.pkg(), TYPE_LABEL),
                    resolveClassName(classElement, a.name(), config, TYPE_LABEL),
                    a.id()))
        .toList();
  }
}

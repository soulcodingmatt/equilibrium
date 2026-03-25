package io.github.soulcodingmatt.equilibrium.processor.orchestration;

import static io.github.soulcodingmatt.equilibrium.processor.orchestration.GenerationOrchestratorSupport.*;

import com.sun.source.util.Trees;
import io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto;
import io.github.soulcodingmatt.equilibrium.annotations.dto.IgnoreDto;
import io.github.soulcodingmatt.equilibrium.processor.config.EquilibriumConfig;
import io.github.soulcodingmatt.equilibrium.processor.generation.dto.DtoGenerator;
import io.github.soulcodingmatt.equilibrium.processor.generation.dto.DtoGeneratorTarget;
import io.github.soulcodingmatt.equilibrium.processor.generation.dto.DtoProcessorServices;
import io.github.soulcodingmatt.equilibrium.processor.validation.codegen.DtoValidationEmitterImpl;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;

/** Handles all DTO-related code generation for a single source class. */
class DtoGenerationOrchestrator {

  private static final String TYPE_LABEL = "DTO";

  private final EquilibriumConfig config;
  private final DiagnosticReporter reporter;
  private final Filer filer;
  private final Trees trees;
  private final Messager messager;

  DtoGenerationOrchestrator(
      EquilibriumConfig config,
      DiagnosticReporter reporter,
      Filer filer,
      Trees trees,
      Messager messager) {
    this.config = config;
    this.reporter = reporter;
    this.filer = filer;
    this.trees = trees;
    this.messager = messager;
  }

  void processGenerateDtos(TypeElement classElement) {
    GenerateDto[] annotations = classElement.getAnnotationsByType(GenerateDto.class);
    if (annotations.length == 0) {
      return;
    }

    List<ResolvedTarget> targets = resolveTargets(classElement, annotations);

    if (!validateUniqueCombinations(classElement, reporter, TYPE_LABEL, "GenerateDto", targets)) {
      return;
    }

    Set<Integer> validIds = collectExplicitIds(targets);

    validateIgnoreAnnotationIds(
        classElement,
        validIds,
        reporter,
        "IgnoreDto",
        "GenerateDto",
        field -> {
          IgnoreDto ann = field.getAnnotation(IgnoreDto.class);
          return ann != null ? ann.ids() : null;
        });

    for (GenerateDto annotation : annotations) {
      processGenerateDto(classElement, annotation);
    }
  }

  /** Pre-registers all DTOs that will be generated to enable cross-references during generation. */
  void preRegisterAllDtos(Set<TypeElement> validElements) {
    for (TypeElement typeElement : validElements) {
      GenerateDto[] annotations = typeElement.getAnnotationsByType(GenerateDto.class);

      for (GenerateDto annotation : annotations) {
        try {
          String packageName = config.validateAndGetPackage(annotation.pkg(), TYPE_LABEL);
          String className = resolveClassName(typeElement, annotation.name(), config, TYPE_LABEL);
          DtoGenerator.registerGeneratedDto(className, packageName + "." + className);
        } catch (Exception e) {
          reporter.warning(
              "Failed to pre-register DTO from "
                  + typeElement.getSimpleName()
                  + ": "
                  + e.getMessage());
        }
      }
    }
  }

  private void processGenerateDto(TypeElement classElement, GenerateDto annotation) {
    String packageName = config.validateAndGetPackage(annotation.pkg(), TYPE_LABEL);
    String className = resolveClassName(classElement, annotation.name(), config, TYPE_LABEL);
    String fullClassName = packageName + "." + className;
    Set<String> ignoredFields =
        collectIgnoredFields(annotation.ignore(), classElement, config, reporter);

    executeGeneration(
        classElement,
        reporter,
        TYPE_LABEL,
        fullClassName,
        () -> {
          DtoGenerator generator =
              new DtoGenerator(
                  new DtoGeneratorTarget(
                      classElement,
                      packageName,
                      className,
                      ignoredFields,
                      annotation.builder(),
                      annotation.id()),
                  new DtoProcessorServices(filer, messager, trees),
                  new DtoValidationEmitterImpl());
          generator.generate();
          DtoGenerator.registerGeneratedDto(className, fullClassName);
        });
  }

  private List<ResolvedTarget> resolveTargets(TypeElement classElement, GenerateDto[] annotations) {
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

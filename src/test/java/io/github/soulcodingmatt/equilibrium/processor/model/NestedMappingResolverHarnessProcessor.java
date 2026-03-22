package io.github.soulcodingmatt.equilibrium.processor.model;

import io.github.soulcodingmatt.equilibrium.annotations.dto.NestedMapping;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Invokes {@link NestedMappingResolver} on a named probe type so unit tests can lock in behavior
 * without running the full Equilibrium processor.
 */
@SupportedAnnotationTypes("*")
public class NestedMappingResolverHarnessProcessor extends AbstractProcessor {

  static volatile String lastFindDtoImport;
  static volatile String lastTransformedScalar;
  static volatile String lastTransformedList;

  private static final String PROBE = "com.acme.probe.NestedMappingResolutionProbe";

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      return false;
    }
    for (Element root : roundEnv.getRootElements()) {
      if (root instanceof TypeElement te && te.getQualifiedName().contentEquals(PROBE)) {
        NestedMappingResolver resolver = new NestedMappingResolver(te, processingEnv.getMessager());
        for (Element enclosed : te.getEnclosedElements()) {
          if (enclosed.getKind() != ElementKind.FIELD) {
            continue;
          }
          VariableElement field = (VariableElement) enclosed;
          String name = field.getSimpleName().toString();
          NestedMapping nestedMapping = field.getAnnotation(NestedMapping.class);
          if (nestedMapping == null) {
            continue;
          }
          if (name.equals("voice")) {
            lastFindDtoImport = resolver.findDtoImportFromSourceClass(nestedMapping);
            lastTransformedScalar = resolver.getTransformedFieldType(field);
          } else if (name.equals("voices")) {
            lastTransformedList = resolver.getTransformedFieldType(field);
          }
        }
      }
    }
    return false;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}

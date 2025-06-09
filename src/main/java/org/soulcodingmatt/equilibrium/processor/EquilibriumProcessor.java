package org.soulcodingmatt.equilibrium.processor;

import com.google.auto.service.AutoService;
import org.soulcodingmatt.equilibrium.annotations.dto.GenerateDto;
import org.soulcodingmatt.equilibrium.processor.generator.DtoGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes({
    "org.soulcodingmatt.equilibrium.annotations.dto.GenerateDto",
    "org.soulcodingmatt.equilibrium.annotations.dto.IgnoreDto",
    "org.soulcodingmatt.equilibrium.annotations.common.IgnoreAll"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedOptions({
    "equilibrium.dto.package",
    "equilibrium.dto.postfix"
})
public class EquilibriumProcessor extends AbstractProcessor {
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private Filer filer;
    private Messager messager;
    private EquilibriumConfig config;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        config = new EquilibriumConfig(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            // Process @GenerateDto annotations
            for (Element element : roundEnv.getElementsAnnotatedWith(GenerateDto.class)) {
                if (element.getKind() != ElementKind.CLASS) {
                    error(element, "@GenerateDto can only be applied to classes");
                    continue;
                }
                processGenerateDto((TypeElement) element);
            }
        } catch (Exception e) {
            error(null, "Failed to process annotations: " + e.getMessage());
        }
        return true;
    }

    private void processGenerateDto(TypeElement classElement) {
        try {
            GenerateDto annotation = classElement.getAnnotation(GenerateDto.class);
            String packageName = config.validateAndGetPackage(annotation.packageName(), "DTO");
            String postfix = annotation.postfix().isEmpty() ? config.getDtoPostfix() : annotation.postfix();
            boolean builder = annotation.builder();

            // Create and run the DTO generator
            DtoGenerator generator = new DtoGenerator(classElement, packageName, postfix, builder, filer, messager);
            generator.generate();

            note(classElement, "Generated DTO class: " + packageName + "." + classElement.getSimpleName() + postfix);
        } catch (Exception e) {
            error(classElement, "Failed to generate DTO: " + e.getMessage());
        }
    }

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void note(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}

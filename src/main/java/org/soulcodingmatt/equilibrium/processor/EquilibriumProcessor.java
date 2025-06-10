package org.soulcodingmatt.equilibrium.processor;

import com.google.auto.service.AutoService;
import org.soulcodingmatt.equilibrium.annotations.dto.GenerateDto;
import org.soulcodingmatt.equilibrium.annotations.record.GenerateRecord;
import org.soulcodingmatt.equilibrium.processor.generator.DtoGenerator;
import org.soulcodingmatt.equilibrium.processor.generator.RecordGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes({
    "org.soulcodingmatt.equilibrium.annotations.dto.GenerateDto",
    "org.soulcodingmatt.equilibrium.annotations.dto.IgnoreDto",
    "org.soulcodingmatt.equilibrium.annotations.record.GenerateRecord",
    "org.soulcodingmatt.equilibrium.annotations.record.IgnoreRecord",
    "org.soulcodingmatt.equilibrium.annotations.common.IgnoreAll"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedOptions({
    "equilibrium.dto.package",
    "equilibrium.dto.postfix",
    "equilibrium.record.package",
    "equilibrium.record.postfix"
})
public class EquilibriumProcessor extends AbstractProcessor {
    private final Set<String> processedElements = new HashSet<>();
    
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
        // If processing is over, we haven't claimed any new annotations
        if (roundEnv.processingOver()) {
            return false;
        }

        // Check if any of our supported annotations are present
        boolean hasRelevantAnnotations = annotations.stream()
            .map(TypeElement::getQualifiedName)
            .map(Object::toString)
            .anyMatch(name -> name.startsWith("org.soulcodingmatt.equilibrium.annotations"));

        // If none of our annotations are present, don't claim them
        if (!hasRelevantAnnotations) {
            return false;
        }

        try {
            // Get valid class elements that need processing
            Set<TypeElement> validElements = getValidClassElements(roundEnv);
            
            // If no valid elements found, don't claim the annotations
            if (validElements.isEmpty()) {
                return false;
            }
            
            // Process each valid element
            for (TypeElement typeElement : validElements) {
                processElement(typeElement);
            }
            
            // We've processed our annotations, so claim them
            return true;
        } catch (Exception e) {
            error(null, "Failed to process annotations: " + e.getMessage());
            e.printStackTrace();
            // On error, don't claim the annotations so other processors might handle them
            return false;
        }
    }

    private Set<TypeElement> getValidClassElements(RoundEnvironment roundEnv) {
        Set<Element> elements = new HashSet<>();
        elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateDto.class));
        elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateRecord.class));

        return elements.stream()
            .filter(this::isValidClassElement)
            .map(TypeElement.class::cast)
            .collect(Collectors.toSet());
    }

    private boolean isValidClassElement(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            error(element, "Annotations can only be applied to classes");
            return false;
        }
        return true;
    }

    private void processElement(TypeElement typeElement) {
        String qualifiedName = typeElement.getQualifiedName().toString();
        
        // Skip if already processed
        if (processedElements.contains(qualifiedName)) {
            return;
        }
        processedElements.add(qualifiedName);

        // Generate DTO if needed
        GenerateDto dtoAnnotation = typeElement.getAnnotation(GenerateDto.class);
        if (dtoAnnotation != null) {
            processGenerateDto(typeElement);
        }

        // Generate Record if needed
        GenerateRecord recordAnnotation = typeElement.getAnnotation(GenerateRecord.class);
        if (recordAnnotation != null) {
            processGenerateRecord(typeElement);
        }
    }

    private void processGenerateDto(TypeElement classElement) {
        try {
            GenerateDto annotation = classElement.getAnnotation(GenerateDto.class);
            String packageName = config.validateAndGetPackage(annotation.packageName(), "DTO");
            String postfix = annotation.postfix().isEmpty() ? config.getDtoPostfix() : annotation.postfix();
            boolean builder = annotation.builder();

            // Create and run the DTO generator
            DtoGenerator generator = new DtoGenerator(classElement, packageName, postfix, builder, filer);
            generator.generate();

            note(classElement, "Generated DTO class: " + packageName + "." + classElement.getSimpleName() + postfix);
        } catch (Exception e) {
            error(classElement, "Failed to generate DTO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processGenerateRecord(TypeElement classElement) {
        try {
            GenerateRecord annotation = classElement.getAnnotation(GenerateRecord.class);
            String packageName = config.validateAndGetPackage(annotation.packageName(), "Record");
            String postfix = annotation.postfix().isEmpty() ? config.getRecordPostfix() : annotation.postfix();

            // Create and run the Record generator
            RecordGenerator generator = new RecordGenerator(classElement, packageName, postfix, filer);
            generator.generate();

            note(classElement, "Generated Record class: " + packageName + "." + classElement.getSimpleName() + postfix);
        } catch (Exception e) {
            error(classElement, "Failed to generate Record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void note(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}

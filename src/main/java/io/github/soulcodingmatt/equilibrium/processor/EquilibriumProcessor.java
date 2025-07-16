package io.github.soulcodingmatt.equilibrium.processor;

import com.google.auto.service.AutoService;
import io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto;
import io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDtos;
import io.github.soulcodingmatt.equilibrium.annotations.dto.IgnoreDto;
import io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecord;
import io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecords;
import io.github.soulcodingmatt.equilibrium.annotations.record.IgnoreRecord;
import io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVo;
import io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVos;
import io.github.soulcodingmatt.equilibrium.annotations.vo.IgnoreVo;
import io.github.soulcodingmatt.equilibrium.processor.generator.DtoGenerator;
import io.github.soulcodingmatt.equilibrium.processor.generator.RecordGenerator;
import io.github.soulcodingmatt.equilibrium.processor.generator.VoGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto",
        "io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDtos",
        "io.github.soulcodingmatt.equilibrium.annotations.dto.IgnoreDto",
        "io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecord",
        "io.github.soulcodingmatt.equilibrium.annotations.record.GenerateRecords",
        "io.github.soulcodingmatt.equilibrium.annotations.record.IgnoreRecord",
        "io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVo",
        "io.github.soulcodingmatt.equilibrium.annotations.vo.GenerateVos",
        "io.github.soulcodingmatt.equilibrium.annotations.vo.IgnoreVo",
        "io.github.soulcodingmatt.equilibrium.annotations.common.IgnoreAll"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedOptions({
        "equilibrium.dto.package",
        "equilibrium.dto.postfix",
        "equilibrium.record.package",
        "equilibrium.record.postfix",
        "equilibrium.vo.package",
        "equilibrium.vo.postfix"
})
public class EquilibriumProcessor extends AbstractProcessor {
    public static final String DUPLICATE_ID = "Duplicate ID ";
    public static final String DUPLICATES_WILL_BE_IGNORED = "' - duplicates will be ignored";
    public static final String RECORD = "Record";
    public static final String INVALID_FIELD_NAME_IN_IGNORE_LIST = "Invalid field name in ignore list: '";
    public static final String WILL_BE_SKIPPED = "' - will be skipped";
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
                .anyMatch(name -> name.startsWith("io.github.soulcodingmatt.equilibrium.annotations"));

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
            // Try to get the first valid element for better error context
            Set<TypeElement> validElements = getValidClassElements(roundEnv);
            if (!validElements.isEmpty()) {
                TypeElement contextElement = validElements.iterator().next();
                error(contextElement, "Failed to process annotations: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
            } else {
                error("Failed to process annotations: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
            }
            // On error, don't claim the annotations so other processors might handle them
            return false;
        }
    }

    private Set<TypeElement> getValidClassElements(RoundEnvironment roundEnv) {
        Set<Element> elements = new HashSet<>();
        elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateDto.class));
        elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateDtos.class));
        elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateRecord.class));
        elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateRecords.class));
        elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateVo.class));
        elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateVos.class));

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

        // Process multiple DTO annotations
        processGenerateDtos(typeElement);

        // Process multiple Record annotations
        processGenerateRecords(typeElement);

        // Process multiple VO annotations
        processGenerateVos(typeElement);
    }

    private void processGenerateDtos(TypeElement classElement) {
        // Get all @GenerateDto annotations (handles both single and multiple annotations)
        GenerateDto[] dtoAnnotations = classElement.getAnnotationsByType(GenerateDto.class);
        
        if (dtoAnnotations.length == 0) {
            return; // No DTO annotations found
        }
        
        // Validate unique combinations of package and postfix
        if (!validateUniqueDtoCombinations(classElement, dtoAnnotations)) {
            return; // Validation failed, error already logged
        }
        
        // Collect all valid IDs from DTO annotations for orphaned ID validation
        Set<Integer> validDtoIds = new HashSet<>();
        for (GenerateDto annotation : dtoAnnotations) {
            int id = annotation.id();
            if (id != -1) {
                validDtoIds.add(id);
            }
        }
        
        // Validate @IgnoreDto annotations for duplicate IDs and orphaned ID references
        validateIgnoreDtoAnnotations(classElement, validDtoIds);
        
        // Process each DTO annotation
        for (GenerateDto annotation : dtoAnnotations) {
            processGenerateDto(classElement, annotation);
        }
    }

    private boolean validateUniqueDtoCombinations(TypeElement classElement, GenerateDto[] annotations) {
        Set<String> uniqueCombinations = new HashSet<>();
        Set<Integer> usedIds = new HashSet<>();
        
        for (GenerateDto annotation : annotations) {
            String packageName = config.validateAndGetPackage(annotation.pkg(), "DTO");
            String className;
            
            // Check if custom name is provided
            if (!annotation.name().isEmpty()) {
                className = annotation.name();
            } else {
                // Fall back to postfix-based naming
                String postfix = config.validateAndGetPostfix("", "DTO");
                className = classElement.getSimpleName() + postfix;
            }
            
            String combination = packageName + "." + className;
            
            if (!uniqueCombinations.add(combination)) {
                error(classElement, "Duplicate DTO configuration would generate the same class: " + combination);
                return false;
            }
            
            // Validate unique IDs (only if ID is explicitly set)
            int id = annotation.id();
            if (id != -1 && !usedIds.add(id)) {
                    error(classElement, "Duplicate DTO ID: " + id + ". Each @GenerateDto annotation must have a unique ID.");
                    return false;
                }

        }
        
        return true;
    }

    private void processGenerateDto(TypeElement classElement, GenerateDto annotation) {
        try {
            String packageName = config.validateAndGetPackage(annotation.pkg(), "DTO");
            String className;
            boolean useCustomName = !annotation.name().isEmpty();
            
            if (useCustomName) {
                className = annotation.name();
            } else {
                String postfix = config.validateAndGetPostfix("", "DTO");
                className = classElement.getSimpleName() + postfix;
            }
            
            // Process ignore field names array and validate each field name
            Set<String> ignoredFields = new HashSet<>();
            for (String fieldName : annotation.ignore()) {
                if (config.isValidFieldName(fieldName)) {
                    ignoredFields.add(fieldName);
                } else if (!fieldName.isEmpty()) {
                    // Log warning for invalid field names but continue processing
                    messager.printMessage(Diagnostic.Kind.WARNING, 
                        INVALID_FIELD_NAME_IN_IGNORE_LIST + fieldName + WILL_BE_SKIPPED,
                        classElement);
                }
            }
            
            boolean builder = annotation.builder();

            // Create and run the DTO generator
            int dtoId = annotation.id();
            DtoGenerator generator = new DtoGenerator(classElement, packageName, className, ignoredFields, builder, dtoId, filer);
            generator.generate();

            note(classElement, "Generated DTO class: " + packageName + "." + className);
        } catch (Exception e) {
            error(classElement, "Failed to generate DTO: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
        }
    }

    private void validateIgnoreDtoAnnotations(TypeElement classElement, Set<Integer> validDtoIds) {
        // Check all fields for @IgnoreDto annotations and validate their ids arrays
        classElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.FIELD)
            .map(VariableElement.class::cast)
            .forEach(field -> {
                IgnoreDto ignoreDtoAnnotation = field.getAnnotation(IgnoreDto.class);
                if (ignoreDtoAnnotation != null) {
                    int[] ids = ignoreDtoAnnotation.ids();
                    Set<Integer> uniqueIds = new HashSet<>();
                    for (int id : ids) {
                        if (!uniqueIds.add(id)) {
                            messager.printMessage(Diagnostic.Kind.WARNING,
                                DUPLICATE_ID + id + " in @IgnoreDto annotation for field '" +
                                field.getSimpleName() + DUPLICATES_WILL_BE_IGNORED, field);
                        } else if (!validDtoIds.isEmpty() && !validDtoIds.contains(id)) {
                            // Only check for orphaned IDs if there are actually IDs defined in @GenerateDto annotations
                            messager.printMessage(Diagnostic.Kind.WARNING,
                                "ID " + id + " in @IgnoreDto annotation for field '" + 
                                field.getSimpleName() + "' does not correspond to any @GenerateDto annotation ID", field);
                        }
                    }
                }
            });
    }

    private void processGenerateRecords(TypeElement classElement) {
        // Get all @GenerateRecord annotations (handles both single and multiple annotations)
        GenerateRecord[] recordAnnotations = classElement.getAnnotationsByType(GenerateRecord.class);
        
        if (recordAnnotations.length == 0) {
            return; // No Record annotations found
        }
        
        // Validate unique combinations of package and postfix
        if (!validateUniqueRecordCombinations(classElement, recordAnnotations)) {
            return; // Validation failed, error already logged
        }
        
        // Collect all valid IDs from Record annotations for orphaned ID validation
        Set<Integer> validRecordIds = new HashSet<>();
        for (GenerateRecord annotation : recordAnnotations) {
            int id = annotation.id();
            if (id != -1) {
                validRecordIds.add(id);
            }
        }
        
        // Validate @IgnoreRecord annotations for duplicate IDs and orphaned ID references
        validateIgnoreRecordAnnotations(classElement, validRecordIds);
        
        // Process each Record annotation
        for (GenerateRecord annotation : recordAnnotations) {
            processGenerateRecord(classElement, annotation);
        }
    }

    private boolean validateUniqueRecordCombinations(TypeElement classElement, GenerateRecord[] annotations) {
        Set<String> uniqueCombinations = new HashSet<>();
        Set<Integer> usedIds = new HashSet<>();
        
        for (GenerateRecord annotation : annotations) {
            String packageName = config.validateAndGetPackage(annotation.pkg(), RECORD);
            String postfix = config.validateAndGetPostfix(annotation.postfix(), RECORD);
            String combination = packageName + "." + classElement.getSimpleName() + postfix;
            
            if (!uniqueCombinations.add(combination)) {
                error(classElement, "Duplicate Record configuration would generate the same class: " + combination);
                return false;
            }
            
            // Validate unique IDs (only if ID is explicitly set)
            int id = annotation.id();
            if (id != -1 && !usedIds.add(id)) {
                    error(classElement, "Duplicate Record ID: " + id + ". Each @GenerateRecord annotation must have a unique ID.");
                    return false;
                }

        }
        
        return true;
    }

    private void processGenerateRecord(TypeElement classElement, GenerateRecord annotation) {
        try {
            String packageName = config.validateAndGetPackage(annotation.pkg(), RECORD);
            String postfix = config.validateAndGetPostfix(annotation.postfix(), RECORD);
            
            // Process ignore field names array and validate each field name
            Set<String> ignoredFields = new HashSet<>();
            for (String fieldName : annotation.ignore()) {
                if (config.isValidFieldName(fieldName)) {
                    ignoredFields.add(fieldName);
                } else if (!fieldName.isEmpty()) {
                    // Log warning for invalid field names but continue processing
                    messager.printMessage(Diagnostic.Kind.WARNING, 
                        INVALID_FIELD_NAME_IN_IGNORE_LIST + fieldName + WILL_BE_SKIPPED,
                        classElement);
                }
            }
            
            // Create and run the Record generator
            int recordId = annotation.id();
            RecordGenerator generator = new RecordGenerator(classElement, packageName, postfix, ignoredFields, recordId, filer);
            generator.generate();

            note(classElement, "Generated Record class: " + packageName + "." + classElement.getSimpleName() + postfix);
        } catch (Exception e) {
            error(classElement, "Failed to generate Record: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
        }
    }

    private void validateIgnoreRecordAnnotations(TypeElement classElement, Set<Integer> validRecordIds) {
        // Check all fields for @IgnoreRecord annotations and validate their ids arrays
        classElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.FIELD)
            .map(VariableElement.class::cast)
            .forEach(field -> {
                IgnoreRecord ignoreRecordAnnotation = field.getAnnotation(IgnoreRecord.class);
                if (ignoreRecordAnnotation != null) {
                    int[] ids = ignoreRecordAnnotation.ids();
                    Set<Integer> uniqueIds = new HashSet<>();
                    for (int id : ids) {
                        if (!uniqueIds.add(id)) {
                            messager.printMessage(Diagnostic.Kind.WARNING,
                                DUPLICATE_ID + id + " in @IgnoreRecord annotation for field '" +
                                field.getSimpleName() + DUPLICATES_WILL_BE_IGNORED, field);
                        } else if (!validRecordIds.isEmpty() && !validRecordIds.contains(id)) {
                            // Only check for orphaned IDs if there are actually IDs defined in @GenerateRecord annotations
                            messager.printMessage(Diagnostic.Kind.WARNING,
                                "ID " + id + " in @IgnoreRecord annotation for field '" + 
                                field.getSimpleName() + "' does not correspond to any @GenerateRecord annotation ID", field);
                        }
                    }
                }
            });
    }

    private void processGenerateVos(TypeElement classElement) {
        // Get all @GenerateVo annotations (handles both single and multiple annotations)
        GenerateVo[] voAnnotations = classElement.getAnnotationsByType(GenerateVo.class);
        
        if (voAnnotations.length == 0) {
            return; // No VO annotations found
        }
        
        // Validate unique combinations of package and postfix
        if (!validateUniqueVoCombinations(classElement, voAnnotations)) {
            return; // Validation failed, error already logged
        }
        
        // Collect all valid IDs from VO annotations for orphaned ID validation
        Set<Integer> validVoIds = new HashSet<>();
        for (GenerateVo annotation : voAnnotations) {
            int id = annotation.id();
            if (id != -1) {
                validVoIds.add(id);
            }
        }
        
        // Validate @IgnoreVo annotations for duplicate IDs and orphaned ID references
        validateIgnoreVoAnnotations(classElement, validVoIds);
        
        // Process each VO annotation
        for (GenerateVo annotation : voAnnotations) {
            processGenerateVo(classElement, annotation);
        }
    }

    private boolean validateUniqueVoCombinations(TypeElement classElement, GenerateVo[] annotations) {
        Set<String> uniqueCombinations = new HashSet<>();
        Set<Integer> usedIds = new HashSet<>();
        
        for (GenerateVo annotation : annotations) {
            String packageName = config.validateAndGetPackage(annotation.pkg(), "VO");
            String postfix = config.validateAndGetPostfix(annotation.postfix(), "VO");
            String combination = packageName + "." + classElement.getSimpleName() + postfix;
            
            if (!uniqueCombinations.add(combination)) {
                error(classElement, "Duplicate VO configuration would generate the same class: " + combination);
                return false;
            }
            
            // Validate unique IDs (only if ID is explicitly set)
            int id = annotation.id();
            if (id != -1 && !usedIds.add(id)) {
                    error(classElement, "Duplicate VO ID: " + id + ". Each @GenerateVo annotation must have a unique ID.");
                    return false;
                }

        }
        
        return true;
    }

    private void processGenerateVo(TypeElement classElement, GenerateVo annotation) {
        try {
            String packageName = config.validateAndGetPackage(annotation.pkg(), "VO");
            String postfix = config.validateAndGetPostfix(annotation.postfix(), "VO");
            
            // Process ignore field names array and validate each field name
            Set<String> ignoredFields = new HashSet<>();
            for (String fieldName : annotation.ignore()) {
                if (config.isValidFieldName(fieldName)) {
                    ignoredFields.add(fieldName);
                } else if (!fieldName.isEmpty()) {
                    // Log warning for invalid field names but continue processing
                    messager.printMessage(Diagnostic.Kind.WARNING, 
                        INVALID_FIELD_NAME_IN_IGNORE_LIST + fieldName + WILL_BE_SKIPPED,
                        classElement);
                }
            }
            
            boolean generateSetter = annotation.setters();

            // Create and run the Value Object generator
            int voId = annotation.id();
            VoGenerator generator = new VoGenerator(classElement, packageName, postfix, 
                                                  ignoredFields, generateSetter, voId, filer);
            generator.generate();

            note(classElement, "Generated Value Object class: " + packageName + "." + classElement.getSimpleName() + postfix);
        } catch (Exception e) {
            error(classElement, "Failed to generate Value Object: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
        }
    }

    private void validateIgnoreVoAnnotations(TypeElement classElement, Set<Integer> validVoIds) {
        // Check all fields for @IgnoreVo annotations and validate their ids arrays
        classElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.FIELD)
            .map(VariableElement.class::cast)
            .forEach(field -> {
                IgnoreVo ignoreVoAnnotation = field.getAnnotation(IgnoreVo.class);
                if (ignoreVoAnnotation != null) {
                    int[] ids = ignoreVoAnnotation.ids();
                    Set<Integer> uniqueIds = new HashSet<>();
                    for (int id : ids) {
                        if (!uniqueIds.add(id)) {
                            messager.printMessage(Diagnostic.Kind.WARNING,
                                DUPLICATE_ID + id + " in @IgnoreVo annotation for field '" +
                                field.getSimpleName() + DUPLICATES_WILL_BE_IGNORED, field);
                        } else if (!validVoIds.isEmpty() && !validVoIds.contains(id)) {
                            // Only check for orphaned IDs if there are actually IDs defined in @GenerateVo annotations
                            messager.printMessage(Diagnostic.Kind.WARNING,
                                "ID " + id + " in @IgnoreVo annotation for field '" + 
                                field.getSimpleName() + "' does not correspond to any @GenerateVo annotation ID", field);
                        }
                    }
                }
            });
    }

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

    private void note(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}

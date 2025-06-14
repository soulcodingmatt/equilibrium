package com.soulcodingmatt.equilibrium.processor;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration class for the Equilibrium annotation processor.
 * Handles global settings from pom.xml and provides default values.
 */
public class EquilibriumConfig {
    private static final String PREFIX = "equilibrium.";
    private static final String DTO_PACKAGE = PREFIX + "dto.package";
    private static final String DTO_POSTFIX = PREFIX + "dto.postfix";
    private static final String RECORD_PACKAGE = PREFIX + "record.package";
    private static final String RECORD_POSTFIX = PREFIX + "record.postfix";
    private static final String VALUE_OBJECT_PACKAGE = PREFIX + "vo.package";
    private static final String VALUE_OBJECT_POSTFIX = PREFIX + "vo.postfix";

    private final Map<String, String> options;

    public EquilibriumConfig(ProcessingEnvironment processingEnv) {
        this.options = processingEnv.getOptions();
    }

    /**
     * Gets the global package for DTOs from the configuration.
     * @return Optional containing the package name, or empty if not configured
     */
    public Optional<String> getDtoPackage() {
        return Optional.ofNullable(options.get(DTO_PACKAGE));
    }

    /**
     * Gets the global postfix for DTOs from the configuration.
     * @return the configured postfix or "Dto" as default
     */
    public String getDtoPostfix() {
        return options.getOrDefault(DTO_POSTFIX, "Dto");
    }

    /**
     * Gets the global package for Records from the configuration.
     * @return Optional containing the package name, or empty if not configured
     */
    public Optional<String> getRecordPackage() {
        return Optional.ofNullable(options.get(RECORD_PACKAGE));
    }

    /**
     * Gets the global postfix for Records from the configuration.
     * @return the configured postfix or "Record" as default
     */
    public String getRecordPostfix() {
        return options.getOrDefault(RECORD_POSTFIX, "Record");
    }

    /**
     * Gets the global package for Value Objects from the configuration.
     * @return Optional containing the package name, or empty if not configured
     */
    public Optional<String> getVoPackage() {
        return Optional.ofNullable(options.get(VALUE_OBJECT_PACKAGE));
    }

    /**
     * Gets the global postfix for Value Objects from the configuration.
     * @return the configured postfix or "Vo" as default
     */
    public String getVoPostfix() {
        return options.getOrDefault(VALUE_OBJECT_POSTFIX, "Vo");
    }

    /**
     * Validates that either a global package is configured or a specific package
     * is provided in the annotation.
     * @param annotationPackage the package specified in the annotation
     * @param classType the type of class being generated (DTO, Record, or VO)
     * @return the package to use, or throws an exception if none is available
     */
    public String validateAndGetPackage(String annotationPackage, String classType) {
        if (!annotationPackage.isEmpty()) {
            return annotationPackage;
        }
        
        Optional<String> packageOpt = switch (classType) {
            case "DTO" -> getDtoPackage();
            case "Record" -> getRecordPackage();
            case "VO" -> getVoPackage();
            default -> Optional.empty();
        };
        
        return packageOpt.orElseThrow(() -> new IllegalStateException(
            String.format("Package not set for %ss. Either configure it globally in pom.xml using %s " +
                "or specify it in the annotation.", classType, 
                switch (classType) {
                    case "DTO" -> DTO_PACKAGE;
                    case "Record" -> RECORD_PACKAGE;
                    case "VO" -> VALUE_OBJECT_PACKAGE;
                    default -> throw new IllegalStateException("Unexpected value: " + classType);
                })));
    }
}

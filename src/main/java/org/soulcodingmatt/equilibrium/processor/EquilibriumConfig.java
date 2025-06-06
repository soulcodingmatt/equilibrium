package org.soulcodingmatt.equilibrium.processor;

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
     * Validates that either a global package is configured or a specific package
     * is provided in the annotation.
     * @param annotationPackage the package specified in the annotation
     * @return the package to use, or throws an exception if none is available
     */
    public String validateAndGetPackage(String annotationPackage, String classType) {
        if (!annotationPackage.isEmpty()) {
            return annotationPackage;
        }
        
        return getDtoPackage()
            .orElseThrow(() -> new IllegalStateException(
                String.format("Package not set for %ss. Either configure it globally in pom.xml using %s " +
                    "or specify it in the annotation.", classType, DTO_PACKAGE)));
    }
}

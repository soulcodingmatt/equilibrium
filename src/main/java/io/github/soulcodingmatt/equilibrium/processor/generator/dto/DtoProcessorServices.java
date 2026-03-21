package io.github.soulcodingmatt.equilibrium.processor.generator.dto;

import com.sun.source.util.Trees;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;

/**
 * Annotation-processing services required by {@link DtoGenerator} (file creation, diagnostics,
 * AST).
 */
public record DtoProcessorServices(Filer filer, Messager messager, Trees trees) {}

package io.github.soulcodingmatt.equilibrium.processor.generation.dto;

import com.sun.source.util.Trees;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;

/**
 * Annotation-processing services required by {@link DtoGenerator} (file creation, diagnostics,
 * AST).
 *
 * @param filer output file creation
 * @param messager diagnostic reporting
 * @param trees compiler AST for the current round
 */
public record DtoProcessorServices(Filer filer, Messager messager, Trees trees) {}

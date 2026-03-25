package io.github.soulcodingmatt.equilibrium.processor.testsupport;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Exposes the running JVM classpath as {@link File} entries for {@link
 * com.google.testing.compile.Compiler#withClasspath(Iterable)} (replacement for deprecated {@code
 * withClasspathFrom(ClassLoader)}).
 */
public final class CompileTestClasspath {

  private CompileTestClasspath() {}

  public static List<File> currentJvm() {
    return Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
        .map(File::new)
        .toList();
  }
}

package io.github.soulcodingmatt.equilibrium.processor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import org.junit.jupiter.api.Test;

class EquilibriumFilerStatsTest {

  @Test
  void incrementsOnlyOnSuccessfulCreateSourceFile() throws IOException {
    AtomicInteger delegateCreates = new AtomicInteger();
    JavaFileObject dummy =
        new SimpleJavaFileObject(URI.create("string:///X.java"), JavaFileObject.Kind.SOURCE) {};

    Filer delegate =
        new Filer() {
          @Override
          public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements)
              throws IOException {
            delegateCreates.incrementAndGet();
            return dummy;
          }

          @Override
          public JavaFileObject createClassFile(CharSequence name, Element... originatingElements) {
            return null;
          }

          @Override
          public FileObject createResource(
              Location location,
              CharSequence pkg,
              CharSequence relativeName,
              Element... originatingElements) {
            return null;
          }

          @Override
          public FileObject getResource(
              Location location, CharSequence pkg, CharSequence relativeName) throws IOException {
            return null;
          }
        };

    EquilibriumFilerStats stats = new EquilibriumFilerStats(delegate);
    assertSame(delegate, stats.delegateFiler());
    assertEquals(0, stats.getGeneratedJavaSourceCount());

    stats.createSourceFile("p.T", new Element[0]);
    assertEquals(1, stats.getGeneratedJavaSourceCount());
    assertEquals(1, delegateCreates.get());

    stats.createClassFile("p.T", new Element[0]);
    assertEquals(1, stats.getGeneratedJavaSourceCount());
  }
}

package io.github.soulcodingmatt.equilibrium.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EquilibriumProcessorTest {

    private EquilibriumProcessor processor;
    private TestMessager testMessager;
    private TestProcessingEnvironment testProcessingEnv;

    @BeforeEach
    void setUp() {
        processor = new EquilibriumProcessor();
        testMessager = new TestMessager();
        testProcessingEnv = new TestProcessingEnvironment(testMessager);
        
        // Initialize the processor
        processor.init(testProcessingEnv);
    }

    @Test
    void testErrorMethodWithElement() throws Exception {
        // Test the error(Element, String) method using reflection
        Method errorMethod = EquilibriumProcessor.class.getDeclaredMethod("error", Element.class, String.class);
        errorMethod.setAccessible(true);
        
        // Act
        errorMethod.invoke(processor, null, "Test error with element");
        
        // Assert
        assertEquals(1, testMessager.getErrorMessages().size());
        TestMessage errorMessage = testMessager.getErrorMessages().get(0);
        assertEquals(Diagnostic.Kind.ERROR, errorMessage.getKind());
        assertEquals("Test error with element", errorMessage.getMessage());
        assertNull(errorMessage.getElement());
    }

    @Test
    void testErrorMethodWithoutElement() throws Exception {
        // Test the error(String) method using reflection
        Method errorMethod = EquilibriumProcessor.class.getDeclaredMethod("error", String.class);
        errorMethod.setAccessible(true);
        
        // Act
        errorMethod.invoke(processor, "Test error without element");
        
        // Assert
        assertEquals(1, testMessager.getGeneralErrorMessages().size());
        assertEquals("Test error without element", testMessager.getGeneralErrorMessages().get(0));
    }

    @Test
    void testErrorMessageFormat() throws Exception {
        // Test that error messages include exception type information
        Method errorMethod = EquilibriumProcessor.class.getDeclaredMethod("error", String.class);
        errorMethod.setAccessible(true);
        
        // Create a test scenario that mimics the error handling in the processor
        RuntimeException testException = new RuntimeException("Test exception message");
        String expectedMessage = "Failed to process annotations: " + testException.getMessage() + 
                                " (" + testException.getClass().getSimpleName() + ")";
        
        // Act
        errorMethod.invoke(processor, expectedMessage);
        
        // Assert
        assertEquals(1, testMessager.getGeneralErrorMessages().size());
        String actualMessage = testMessager.getGeneralErrorMessages().get(0);
        assertTrue(actualMessage.contains("Failed to process annotations"));
        assertTrue(actualMessage.contains("Test exception message"));
        assertTrue(actualMessage.contains("RuntimeException"));
    }

    @Test
    void testNoteMethod() throws Exception {
        // Test the note method using reflection
        Method noteMethod = EquilibriumProcessor.class.getDeclaredMethod("note", Element.class, String.class);
        noteMethod.setAccessible(true);
        
        // Act
        noteMethod.invoke(processor, null, "Test note message");
        
        // Assert
        assertEquals(1, testMessager.getNoteMessages().size());
        TestMessage noteMessage = testMessager.getNoteMessages().get(0);
        assertEquals(Diagnostic.Kind.NOTE, noteMessage.getKind());
        assertEquals("Test note message", noteMessage.getMessage());
    }

    // Test helper classes
    
    private static class TestMessager implements Messager {
        private final List<TestMessage> errorMessages = new ArrayList<>();
        private final List<String> generalErrorMessages = new ArrayList<>();
        private final List<TestMessage> noteMessages = new ArrayList<>();

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            if (kind == Diagnostic.Kind.ERROR) {
                generalErrorMessages.add(msg.toString());
            }
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            if (kind == Diagnostic.Kind.ERROR) {
                errorMessages.add(new TestMessage(kind, msg.toString(), e));
            } else if (kind == Diagnostic.Kind.NOTE) {
                noteMessages.add(new TestMessage(kind, msg.toString(), e));
            }
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, 
                               javax.lang.model.element.AnnotationMirror a) {
            printMessage(kind, msg, e);
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, 
                               javax.lang.model.element.AnnotationMirror a, 
                               javax.lang.model.element.AnnotationValue v) {
            printMessage(kind, msg, e);
        }

        public List<TestMessage> getErrorMessages() { return errorMessages; }
        public List<String> getGeneralErrorMessages() { return generalErrorMessages; }
        public List<TestMessage> getNoteMessages() { return noteMessages; }
    }

    private static class TestMessage {
        private final Diagnostic.Kind kind;
        private final String message;
        private final Element element;

        public TestMessage(Diagnostic.Kind kind, String message, Element element) {
            this.kind = kind;
            this.message = message;
            this.element = element;
        }

        public Diagnostic.Kind getKind() { return kind; }
        public String getMessage() { return message; }
        public Element getElement() { return element; }
    }

    private static class TestProcessingEnvironment implements ProcessingEnvironment {
        private final Messager messager;
        private final Map<String, String> options = new HashMap<>();

        public TestProcessingEnvironment(Messager messager) {
            this.messager = messager;
        }

        @Override
        public Map<String, String> getOptions() { return options; }

        @Override
        public Messager getMessager() { return messager; }

        @Override
        public Filer getFiler() { return null; }

        @Override
        public Elements getElementUtils() { return null; }

        @Override
        public Types getTypeUtils() { return null; }

        @Override
        public SourceVersion getSourceVersion() { return SourceVersion.RELEASE_21; }

        @Override
        public Locale getLocale() { return Locale.getDefault(); }
    }
} 
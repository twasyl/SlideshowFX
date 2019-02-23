package com.twasyl.slideshowfx.snippet.executor.scala;

import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.twasyl.slideshowfx.snippet.executor.scala.ScalaSnippetExecutor.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class {@link ScalaSnippetExecutor}.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class ScalaSnippetExecutorTest {

    private final ScalaSnippetExecutor snippetExecutor = new ScalaSnippetExecutor();

    @Test
    public void noClassName() {
        final CodeSnippet snippet = new CodeSnippet();

        assertEquals("Snippet", snippetExecutor.determineClassName(snippet));
    }

    @Test
    public void emptyClassName() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(CLASS_NAME_PROPERTY, "");

        assertEquals("Snippet", snippetExecutor.determineClassName(snippet));
    }

    @Test
    public void givenClassName() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(CLASS_NAME_PROPERTY, "ScalaTest");

        assertEquals("ScalaTest", snippetExecutor.determineClassName(snippet));
    }

    @Test
    public void hasNoImports() {
        final CodeSnippet snippet = new CodeSnippet();

        assertFalse(snippetExecutor.hasImports(snippet));
    }

    @Test
    public void hasEmptyImports() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(IMPORTS_PROPERTY, "");

        assertFalse(snippetExecutor.hasImports(snippet));
    }

    @Test
    public void hasImports() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(IMPORTS_PROPERTY, "import p._");

        assertTrue(snippetExecutor.hasImports(snippet));
    }

    @Test
    public void mustNotBeWrappedInMain() {
        final CodeSnippet snippet = new CodeSnippet();

        assertFalse(snippetExecutor.mustBeWrappedIn(snippet, WRAP_IN_MAIN_PROPERTY));
    }

    @Test
    public void mustExplicitlyNotBeWrappedInMain() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(WRAP_IN_MAIN_PROPERTY, "false");

        assertFalse(snippetExecutor.mustBeWrappedIn(snippet, WRAP_IN_MAIN_PROPERTY));
    }

    @Test
    public void mustBeWrappedInMain() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(WRAP_IN_MAIN_PROPERTY, "true");

        assertTrue(snippetExecutor.mustBeWrappedIn(snippet, WRAP_IN_MAIN_PROPERTY));
    }

    @Test
    public void formatImportWithoutImportKeyword() {
        assertEquals("import mypackage", snippetExecutor.formatImportLine("mypackage"));
    }

    @Test
    public void formatImportWithImportKeyword() {
        assertEquals("import mypackage", snippetExecutor.formatImportLine("import mypackage"));
    }

    @Test
    public void importsWithAndWithoutKeyword() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(IMPORTS_PROPERTY, "import mypackage\nmysecondpackage");

        assertEquals("import mypackage\nimport mysecondpackage", snippetExecutor.getImports(snippet));
    }

    @Test
    public void buildSourceCodeWithoutImportsAndWithoutWrapInMainAndWithoutClassName() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.setCode("def main(args: Array[String]) {\n\tprintln(\"Hello\")\n}");

        final String expected = IOUtils.read(ScalaSnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/scala/buildSourceCodeWithoutImportsAndWithoutWrapInMainAndWithoutClassName_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCodeWithoutImportsAndWithoutWrapInMain() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(CLASS_NAME_PROPERTY, "TestScala");

        snippet.setCode("def main(args: Array[String]) {\n\tprintln(\"Hello\")\n}");

        final String expected = IOUtils.read(ScalaSnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/scala/buildSourceCodeWithoutImportsAndWithoutWrapInMain_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCodeWithoutWrapInMain() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(CLASS_NAME_PROPERTY, "TestScala");
        snippet.getProperties().put(IMPORTS_PROPERTY, "import mypackage\nmysecondpackage");

        snippet.setCode("def main(args: Array[String]) {\n\tprintln(\"Hello\")\n}");

        final String expected = IOUtils.read(ScalaSnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/scala/buildSourceCodeWithoutWrapInMain_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCode() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(CLASS_NAME_PROPERTY, "TestScala");
        snippet.getProperties().put(IMPORTS_PROPERTY, "import mypackage\nmysecondpackage");
        snippet.getProperties().put(WRAP_IN_MAIN_PROPERTY, "true");

        snippet.setCode("println(\"Hello\")");

        final String expected = IOUtils.read(ScalaSnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/scala/buildSourceCode_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }
}

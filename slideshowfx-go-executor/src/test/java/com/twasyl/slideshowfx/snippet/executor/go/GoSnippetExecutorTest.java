package com.twasyl.slideshowfx.snippet.executor.go;

import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.twasyl.slideshowfx.snippet.executor.go.GoSnippetExecutor.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class {@link GoSnippetExecutor}.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class GoSnippetExecutorTest {

    private final GoSnippetExecutor snippetExecutor = new GoSnippetExecutor();

    @Test
    public void noPackageName() {
        final CodeSnippet snippet = new CodeSnippet();

        assertEquals("slideshowfx", snippetExecutor.determinePackageName(snippet));
    }

    @Test
    public void emptyPackageName() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(PACKAGE_NAME_PROPERTY, "");

        assertEquals("slideshowfx", snippetExecutor.determinePackageName(snippet));
    }

    @Test
    public void givenPackageName() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(PACKAGE_NAME_PROPERTY, "sfx");

        assertEquals("sfx", snippetExecutor.determinePackageName(snippet));
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
        snippet.getProperties().put(IMPORTS_PROPERTY, "fmt");

        assertTrue(snippetExecutor.hasImports(snippet));
    }

    @Test
    public void mustNotBeWrappedInMain() {
        final CodeSnippet snippet = new CodeSnippet();

        assertFalse(snippetExecutor.mustBeWrappedInMain(snippet));
    }

    @Test
    public void mustExplicitlyNotBeWrappedInMain() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(WRAP_IN_MAIN_PROPERTY, "false");

        assertFalse(snippetExecutor.mustBeWrappedInMain(snippet));
    }

    @Test
    public void mustBeWrappedInMain() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(WRAP_IN_MAIN_PROPERTY, "true");

        assertTrue(snippetExecutor.mustBeWrappedInMain(snippet));
    }

    @Test
    public void formatImportWithoutDoubleQuotes() {
        assertEquals("\"fmt\"", snippetExecutor.formatImportLine("fmt"));
    }

    @Test
    public void formatImportWithDoubleQuotes() {
        assertEquals("\"fmt\"", snippetExecutor.formatImportLine("\"fmt\""));
    }

    @Test
    public void importsWithAndWithoutDoubleQuotes() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(IMPORTS_PROPERTY, "\"mypackage\"\nmysecondpackage");

        assertEquals("import (\n\t\"mypackage\"\n\t\"mysecondpackage\"\n)", snippetExecutor.getImports(snippet));
    }

    @Test
    public void buildSourceCodeWithoutImportsAndWithoutWrapInMainAndWithoutPackageName() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.setCode("func main() {\n\tfmt.Printf(\"Hello, world.\\n\")\n}");

        final String expected = IOUtils.read(GoSnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/go/buildSourceCodeWithoutImportsAndWithoutWrapInMainAndWithoutPackageName_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCodeWithoutImportsAndWithoutWrapInMain() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(PACKAGE_NAME_PROPERTY, "sfx");

        snippet.setCode("func main() {\n\tfmt.Printf(\"Hello, world.\\n\")\n}");

        final String expected = IOUtils.read(GoSnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/go/buildSourceCodeWithoutImportsAndWithoutWrapInMain_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCodeWithoutWrapInMain() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(PACKAGE_NAME_PROPERTY, "sfx");
        snippet.getProperties().put(IMPORTS_PROPERTY, "mypackage\nmysecondpackage");

        snippet.setCode("func main() {\n\tfmt.Printf(\"Hello, world.\\n\")\n}");

        final String expected = IOUtils.read(GoSnippetExecutor.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/go/buildSourceCodeWithoutWrapInMain_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCode() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(PACKAGE_NAME_PROPERTY, "sfx");
        snippet.getProperties().put(IMPORTS_PROPERTY, "mypackage\nmysecondpackage");
        snippet.getProperties().put(WRAP_IN_MAIN_PROPERTY, "true");

        snippet.setCode("fmt.Printf(\"Hello, world.\\n\")");

        final String expected = IOUtils.read(GoSnippetExecutor.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/go/buildSourceCode_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }
}

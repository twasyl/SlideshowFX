package com.twasyl.slideshowfx.snippet.executor.golo;

import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.twasyl.slideshowfx.snippet.executor.golo.GoloSnippetExecutor.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class {@link GoloSnippetExecutor}.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class GoloSnippetExecutorTest {

    private final GoloSnippetExecutor snippetExecutor = new GoloSnippetExecutor();

    @Test
    public void noModuleName() {
        final CodeSnippet snippet = new CodeSnippet();

        assertEquals("slideshowfx.Snippet", snippetExecutor.determineModuleName(snippet));
    }

    @Test
    public void emptyModuleName() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(MODULE_NAME_PROPERTY, "");

        assertEquals("slideshowfx.Snippet", snippetExecutor.determineModuleName(snippet));
    }

    @Test
    public void givenModuleName() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(MODULE_NAME_PROPERTY, "sfx.Golo");

        assertEquals("sfx.Golo", snippetExecutor.determineModuleName(snippet));
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
        snippet.getProperties().put(IMPORTS_PROPERTY, "import p.*");

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
    public void buildSourceCodeWithoutImportsAndWithoutWrapInMainAndWithoutModuleName() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.setCode("function main = |args| {\n\tprintln(\"Hello\")\n}");

        final String expected = IOUtils.read(GoloSnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/golo/buildSourceCodeWithoutImportsAndWithoutWrapInMainAndWithoutModuleName_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCodeWithoutImportsAndWithoutWrapInMain() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(MODULE_NAME_PROPERTY, "sfx.Golo");

        snippet.setCode("function main = |args| {\n\tprintln(\"Hello\")\n}");

        final String expected = IOUtils.read(GoloSnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/golo/buildSourceCodeWithoutImportsAndWithoutWrapInMain_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCodeWithoutWrapInMain() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(MODULE_NAME_PROPERTY, "sfx.Golo");
        snippet.getProperties().put(IMPORTS_PROPERTY, "import mypackage\nmysecondpackage");

        snippet.setCode("function main = |args| {\n\tprintln(\"Hello\")\n}");

        final String expected = IOUtils.read(GoloSnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/golo/buildSourceCodeWithoutWrapInMain_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCode() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(MODULE_NAME_PROPERTY, "sfx.Golo");
        snippet.getProperties().put(IMPORTS_PROPERTY, "import mypackage\nmysecondpackage");
        snippet.getProperties().put(WRAP_IN_MAIN_PROPERTY, "true");

        snippet.setCode("println(\"Hello\")");

        final String expected = IOUtils.read(GoloSnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/golo/buildSourceCode_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }
}

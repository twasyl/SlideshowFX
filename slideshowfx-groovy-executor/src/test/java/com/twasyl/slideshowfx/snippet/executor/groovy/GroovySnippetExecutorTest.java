package com.twasyl.slideshowfx.snippet.executor.groovy;

import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.twasyl.slideshowfx.snippet.executor.groovy.GroovySnippetExecutor.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class {@link GroovySnippetExecutor}.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class GroovySnippetExecutorTest {

    private final GroovySnippetExecutor snippetExecutor = new GroovySnippetExecutor();

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
        snippet.getProperties().put(CLASS_NAME_PROPERTY, "GroovyTest");

        assertEquals("GroovyTest", snippetExecutor.determineClassName(snippet));
    }

    @Test
    public void noScript() {
        final CodeSnippet codeSnippet = new CodeSnippet();

        assertFalse(snippetExecutor.makeScript(codeSnippet));
    }

    @Test
    public void makeScript() {
        final CodeSnippet codeSnippet = new CodeSnippet();
        codeSnippet.getProperties().put(MAKE_SCRIPT, "true");

        assertTrue(snippetExecutor.makeScript(codeSnippet));
    }

    @Test
    public void explicitlyNoScript() {
        final CodeSnippet codeSnippet = new CodeSnippet();
        codeSnippet.getProperties().put(MAKE_SCRIPT, "false");

        assertFalse(snippetExecutor.makeScript(codeSnippet));
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
    public void mustNotBeWrappedInMethodRunner() {
        final CodeSnippet snippet = new CodeSnippet();

        assertFalse(snippetExecutor.mustBeWrappedInMethodRunner(snippet));
    }

    @Test
    public void mustExplicitlyNotBeWrappedInMethodRunner() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(WRAP_IN_METHOD_RUNNER, "false");

        assertFalse(snippetExecutor.mustBeWrappedInMethodRunner(snippet));
    }

    @Test
    public void mustBeWrappedInMethodRunner() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(WRAP_IN_METHOD_RUNNER, "true");

        assertTrue(snippetExecutor.mustBeWrappedInMethodRunner(snippet));
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
    public void formatImportWithImportKeywordAndWithoutColumn() {
        assertEquals("import mypackage", snippetExecutor.formatImportLine("import mypackage"));
    }

    @Test
    public void importsWithAndWithoutKeyword() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(IMPORTS_PROPERTY, "import mypackage\nmysecondpackage");

        assertEquals("import mypackage\nimport mysecondpackage", snippetExecutor.getImports(snippet));
    }

    @Test
    public void buildSourceCodeWithoutImportsAndWithoutWrapInMethodRunnerAndWithoutClassName() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.setCode("def static main(String ... args) {\n\tprintln(\"Hello\")\n}");

        final String expected = IOUtils.read(GroovySnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/groovy/buildSourceCodeWithoutImportsAndWithoutWrapInMethodRunnerAndWithoutClassName_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCodeWithoutImportsAndWithoutWrapInMethodRunner() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(CLASS_NAME_PROPERTY, "TestGroovy");

        snippet.setCode("def static main(String ... args) {\n\tprintln(\"Hello\")\n}");

        final String expected = IOUtils.read(GroovySnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/groovy/buildSourceCodeWithoutImportsAndWithoutWrapInMethodRunner_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCodeWithoutWrapInMethodRunner() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(CLASS_NAME_PROPERTY, "TestGroovy");
        snippet.getProperties().put(IMPORTS_PROPERTY, "import mypackage\nmysecondpackage");

        snippet.setCode("def static main(String ... args) {\n\tprintln(\"Hello\")\n}");

        final String expected = IOUtils.read(GroovySnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/groovy/buildSourceCodeWithoutWrapInMethodRunner_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCodeWithoutWrapInMethodRunnerButMakeScript() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(CLASS_NAME_PROPERTY, "TestGroovy");
        snippet.getProperties().put(IMPORTS_PROPERTY, "import mypackage\nmysecondpackage");
        snippet.getProperties().put(MAKE_SCRIPT, "true");

        snippet.setCode("def run() {\n\tprintln(\"Hello\")\n}");

        final String expected = IOUtils.read(GroovySnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/groovy/buildSourceCodeWithoutWrapInMethodRunnerButMakeScript_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCode() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(CLASS_NAME_PROPERTY, "TestGroovy");
        snippet.getProperties().put(IMPORTS_PROPERTY, "import mypackage\nmysecondpackage");
        snippet.getProperties().put(WRAP_IN_METHOD_RUNNER, "true");

        snippet.setCode("println(\"Hello\")");

        final String expected = IOUtils.read(GroovySnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/groovy/buildSourceCode_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCodeAsScript() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(CLASS_NAME_PROPERTY, "TestGroovy");
        snippet.getProperties().put(IMPORTS_PROPERTY, "import mypackage\nmysecondpackage");
        snippet.getProperties().put(WRAP_IN_METHOD_RUNNER, "true");
        snippet.getProperties().put(MAKE_SCRIPT, "true");

        snippet.setCode("println(\"Hello\")");

        final String expected = IOUtils.read(GroovySnippetExecutorTest.class.getResourceAsStream("/com/twasyl/slideshowfx/snippet/executor/groovy/buildSourceCodeAsScript_expected.txt"));
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }
}

package com.twasyl.slideshowfx.snippet.executor.kotlin;

import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import org.junit.Test;

import java.io.IOException;

import static com.twasyl.slideshowfx.snippet.executor.kotlin.KotlinSnippetExecutor.*;
import static org.junit.Assert.*;

/**
 * Tests the class {@link KotlinSnippetExecutor}.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class KotlinSnippetExecutorTest {

    private final KotlinSnippetExecutor snippetExecutor = new KotlinSnippetExecutor();

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
        snippet.getProperties().put(IMPORTS_PROPERTY, "java.util.List");

        assertTrue(snippetExecutor.hasImports(snippet));
    }

    @Test
    public void hasNoPackage() {
        final CodeSnippet snippet = new CodeSnippet();

        assertFalse(snippetExecutor.hasPackage(snippet));
    }

    @Test
    public void hasEmptyPackage() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(PACKAGE_NAME_PROPERTY, "");

        assertFalse(snippetExecutor.hasPackage(snippet));
    }

    @Test
    public void hasPackage() {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(PACKAGE_NAME_PROPERTY, "SlideshowFX");

        assertTrue(snippetExecutor.hasPackage(snippet));
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
    public void formatPackageWithoutPackageKeyword() {
        assertEquals("package SlideshowFX", snippetExecutor.formatPackageName("SlideshowFX"));
    }

    @Test
    public void formatPackageWithPackageKeyword() {
        assertEquals("package SlideshowFX", snippetExecutor.formatPackageName("package SlideshowFX"));
    }

    @Test
    public void buildSourceCodeWithoutImportsAndWithoutWrapInMainAndWithoutPackage() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.setCode("fun main(args: Array<String>) {\n\tprintln(\"Hello\")\n}");

        final String expected = ResourceHelper.readResource("/com/twasyl/slideshowfx/snippet/executor/kotlin/buildSourceCodeWithoutImportsAndWithoutWrapInMainAndWithoutPackage_expected.txt");
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCodeWithoutImportsAndWithoutWrapInMain() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(PACKAGE_NAME_PROPERTY, "SlideshowFX");

        snippet.setCode("fun main(args: Array<String>) {\n\tprintln(\"Hello\")\n}");

        final String expected = ResourceHelper.readResource("/com/twasyl/slideshowfx/snippet/executor/kotlin/buildSourceCodeWithoutImportsAndWithoutWrapInMain_expected.txt");
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCodeWithoutWrapInMain() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(PACKAGE_NAME_PROPERTY, "SlideshowFX");
        snippet.getProperties().put(IMPORTS_PROPERTY, "import mypackage\nmysecondpackage");

        snippet.setCode("fun main(args: Array<String>) {\n\tprintln(\"Hello\")\n}");

        final String expected = ResourceHelper.readResource("/com/twasyl/slideshowfx/snippet/executor/kotlin/buildSourceCodeWithoutWrapInMain_expected.txt");
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }

    @Test
    public void buildSourceCode() throws IOException {
        final CodeSnippet snippet = new CodeSnippet();
        snippet.getProperties().put(PACKAGE_NAME_PROPERTY, "SlideshowFX");
        snippet.getProperties().put(IMPORTS_PROPERTY, "import mypackage\nmysecondpackage");
        snippet.getProperties().put(WRAP_IN_MAIN_PROPERTY, "true");

        snippet.setCode("println(\"Hello\")");

        final String expected = ResourceHelper.readResource("/com/twasyl/slideshowfx/snippet/executor/kotlin/buildSourceCode_expected.txt");
        assertEquals(expected, snippetExecutor.buildSourceCode(snippet));
    }
}

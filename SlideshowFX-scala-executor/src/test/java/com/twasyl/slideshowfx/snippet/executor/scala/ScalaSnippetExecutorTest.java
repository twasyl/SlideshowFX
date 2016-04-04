package com.twasyl.slideshowfx.snippet.executor.scala;

import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import org.junit.Test;

import static com.twasyl.slideshowfx.snippet.executor.scala.ScalaSnippetExecutor.CLASS_NAME_PROPERTY;
import static com.twasyl.slideshowfx.snippet.executor.scala.ScalaSnippetExecutor.IMPORTS_PROPERTY;
import static com.twasyl.slideshowfx.snippet.executor.scala.ScalaSnippetExecutor.WRAP_IN_MAIN_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the class {@link ScalaSnippetExecutor}.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
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
}

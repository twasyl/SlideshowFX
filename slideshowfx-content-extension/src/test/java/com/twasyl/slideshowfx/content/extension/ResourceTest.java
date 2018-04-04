package com.twasyl.slideshowfx.content.extension;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link Resource}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 2.0
 */
public class ResourceTest {

    @Test
    public void testJavaScriptFile() {
        final Resource resource = new Resource(ResourceType.JAVASCRIPT_FILE, "somewhere/file.js");
        final String expected = "<script type=\"text/javascript\" src=\"base/path/somewhere/file.js\">";
        assertEquals(expected, resource.buildHTMLString("base/path"));
    }

    @Test
    public void testCSSFile() {
        final Resource resource = new Resource(ResourceType.CSS_FILE, "somewhere/file.css");
        final String expected = "<link rel=\"stylesheet\" href=\"base/path/somewhere/file.css\">";
        assertEquals(expected, resource.buildHTMLString("base/path"));
    }

    @Test
    public void testScript() {
        final Resource resource = new Resource(ResourceType.SCRIPT, "var s = 42;");
        final String expected = "<script type=\"text/javascript\">var s = 42;</script>";
        assertEquals(expected, resource.buildHTMLString("base/path"));
    }

    @Test
    public void testStyle() {
        final Resource resource = new Resource(ResourceType.CSS, "h1 {\n\tcolor: red;\n}");
        final String expected = "<style>h1 {\n\tcolor: red;\n}</style>";
        assertEquals(expected, resource.buildHTMLString("base/path"));
    }
}

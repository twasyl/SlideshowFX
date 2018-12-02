package com.twasyl.slideshowfx.content.extension.image;

import com.twasyl.slideshowfx.content.extension.image.controllers.ImageContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.html.HtmlMarkup;
import com.twasyl.slideshowfx.markup.markdown.MarkdownMarkup;
import com.twasyl.slideshowfx.markup.textile.TextileMarkup;
import javafx.beans.property.SimpleBooleanProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@DisplayName("The ImageContentExtension class")
public class ImageContentExtensionTest {

    private static class ExpectedContent {
        String displayName;
        IMarkup markup;
        String width;
        String height;
        String expected;

        ExpectedContent(IMarkup markup) {
            this.markup = markup;
        }

        ExpectedContent withDisplayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        ExpectedContent withWidth(final String width) {
            this.width = width;
            return this;
        }

        ExpectedContent withHeight(final String height) {
            this.height = height;
            return this;
        }

        ExpectedContent withExpected(final String expected) {
            this.expected = expected;
            return this;
        }
    }

    File file = new File("image.png");
    IMarkup html = new HtmlMarkup();
    IMarkup textile = new TextileMarkup();
    IMarkup markdown = new MarkdownMarkup();

    ImageContentExtensionController mockController(final String width, final String height) {
        final ImageContentExtensionController controller = spy(ImageContentExtensionController.class);
        doReturn(new SimpleBooleanProperty(true)).when(controller).areInputsValid();
        doReturn(file).when(controller).getSelectedFile();
        doReturn(file.getAbsolutePath()).when(controller).getSelectedFileUrl();
        doReturn(width).when(controller).getImageWidth();
        doReturn(height).when(controller).getImageHeight();
        return controller;
    }

    ImageContentExtension mockContentExtension(final String width, final String height) {
        final ImageContentExtension extension = spy(ImageContentExtension.class);
        doReturn(mockController(width, height)).when(extension).getController();
        return extension;
    }

    @TestFactory
    @DisplayName("should generate content")
    Stream<DynamicTest> testingContentGeneration() {
        // @formatter:off
        return Stream.of(
                new ExpectedContent(html)
                        .withDisplayName("without dimensions in HTML")
                        .withExpected("<img src=\"" + file.getAbsolutePath() + "\" />"),
                new ExpectedContent(html)
                        .withDisplayName("with empty dimensions in HTML")
                        .withExpected("<img src=\"" + file.getAbsolutePath() + "\" />")
                        .withWidth("").withHeight(""),
                new ExpectedContent(html)
                        .withDisplayName("when specifying a width in HTML")
                        .withExpected("<img src=\"" + file.getAbsolutePath() + "\" width=\"150px\" />")
                        .withWidth("150px"),
                new ExpectedContent(html)
                        .withDisplayName("when specifying a height in HTML")
                        .withExpected("<img src=\"" + file.getAbsolutePath() + "\" height=\"200px\" />")
                        .withHeight("200px"),
                new ExpectedContent(html)
                        .withDisplayName("when specifying dimensions in HTML")
                        .withExpected("<img src=\"" + file.getAbsolutePath() + "\" width=\"150px\" height=\"200px\" />")
                        .withWidth("150px").withHeight("200px"),
                new ExpectedContent(textile)
                        .withDisplayName("without dimensions in Textile")
                        .withExpected("!" + file.getAbsolutePath() + "!"),
                new ExpectedContent(textile)
                        .withDisplayName("with empty dimensions in Textile")
                        .withExpected("!" + file.getAbsolutePath() + "!")
                        .withWidth("").withHeight(""),
                new ExpectedContent(textile)
                        .withDisplayName("when specifying a width in Textile")
                        .withExpected("!{width: 150px;}" + file.getAbsolutePath() + "!")
                        .withWidth("150px"),
                new ExpectedContent(textile)
                        .withDisplayName("when specifying a height in Textile")
                        .withExpected("!{height: 200px;}" + file.getAbsolutePath() + "!")
                        .withHeight("200px"),
                new ExpectedContent(textile)
                        .withDisplayName("when specifying dimensions in Textile")
                        .withExpected("!{width: 150px;height: 200px;}" + file.getAbsolutePath() + "!")
                        .withWidth("150px").withHeight("200px"),
                new ExpectedContent(markdown)
                        .withDisplayName("without dimensions in Markdown")
                        .withExpected("[](" + file.getAbsolutePath() + ")"),
                new ExpectedContent(markdown)
                        .withDisplayName("with empty dimensions in Markdown")
                        .withExpected("[](" + file.getAbsolutePath() + ")")
                        .withWidth("").withHeight(""),
                new ExpectedContent(markdown)
                        .withDisplayName("when specifying a width in Markdown")
                        .withExpected("<img src=\"" + file.getAbsolutePath() + "\" width=\"150px\" />")
                        .withWidth("150px"),
                new ExpectedContent(markdown)
                        .withDisplayName("when specifying a height in Markdown")
                        .withExpected("<img src=\"" + file.getAbsolutePath() + "\" height=\"200px\" />")
                        .withHeight("200px"),
                new ExpectedContent(markdown)
                        .withDisplayName("when specifying dimensions in Markdown")
                        .withExpected("<img src=\"" + file.getAbsolutePath() + "\" width=\"150px\" height=\"200px\" />")
                        .withWidth("150px").withHeight("200px")
        ).map(content -> {
            final ImageContentExtension extension = mockContentExtension(content.width, content.height);
            final Executable executable = () -> assertEquals(content.expected, extension.buildContentString(content.markup));
            return dynamicTest(content.displayName, executable);
        });
        // @formatter:on
    }
}

package com.twasyl.slideshowfx.controls;

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This control allows to define the content for a slide. It provides helper methods for inserting the current slide
 * content in the editor as well as getting it.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class SlideContentEditor extends BorderPane {
    private static final Logger LOGGER = Logger.getLogger(SlideContentEditor.class.getName());
    private final WebView browser = new WebView();

    public SlideContentEditor() {
        this.browser.getEngine().load(SlideContentEditor.class.getResource("/com/twasyl/slideshowfx/html/ace-file-editor.html").toExternalForm());
        this.setCenter(this.browser);
    }

    /**
     * This method retrieves the content of the Node allowing to define the content of the slide.
     * @return The text contained in the Node for defining content of the slide.
     */
    public String getContentEditorValue() {
        final String valueAsBase64 = (String) this.browser.getEngine().executeScript("getContent();");
        final byte[] valueAsBytes = Base64.getDecoder().decode(valueAsBase64);

        String value = null;

        try {
            value = new String(valueAsBytes, "UTF8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.INFO, "Can not get value for slide content", e);
        }

        return value;
    }

    /**
     * Set the value for this content editor. This method doesn't append the given {@code value} to the current one
     * present in this editor. In order to append the value use {@link #appendContentEditorValue(String)}.
     * @param value The new value of this editor
     */
    public void setContentEditorValue(final String value) {
        final String encodedValue = Base64.getEncoder().encodeToString(value.getBytes());

        this.browser.getEngine().executeScript(String.format("setContent('%1$s');", encodedValue));
    }

    /**
     * Append the given value to this content editor. The current caret position is taken in consideration in order to
     * append the value.
     * @param value The value to append to the content editor.
     */
    public void appendContentEditorValue(final String value) {
        final String encodedValue = Base64.getEncoder().encodeToString(value.getBytes());

        this.browser.getEngine().executeScript(String.format("appendContent('%1$s');", encodedValue));
    }

    /**
     * Select all text that is currently in the editor.
     */
    public void selectAll() {
        this.browser.getEngine().executeScript("selectAll();");
    }

    /**
     * Set the mode for the content editor. If {@code null} or an empty string is passed, plain text is set as mode.
     * @param mode The mode for the content editor.
     */
    public void setMode(String mode) {
        if(mode == null || mode.isEmpty()) {
            this.browser.getEngine().executeScript("setMode('ace/mode/plain_text');");
        } else {
            this.browser.getEngine().executeScript(String.format("setMode('%1$s');", mode));
        }
    }
}

package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.utils.PlatformHelper;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import com.twasyl.slideshowfx.utils.ZipUtils;
import com.twasyl.slideshowfx.utils.keys.KeyEventUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;

/**
 * This control allows to define the content for a slide. It provides helper methods for inserting the current slide
 * content in the editor as well as getting it.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class SlideContentEditor extends BorderPane {
    private static final Logger LOGGER = Logger.getLogger(SlideContentEditor.class.getName());

    private final WebView browser = new WebView();

    public SlideContentEditor() {
        this.browser.getEngine().load(this.prepareAndGetEditorPageURI());

        this.browser.setOnKeyPressed(event -> {
            final boolean isShortcutDown = event.isShortcutDown();
            if(isShortcutDown) {
                if(KeyEventUtils.isShortcutSequence("A", event)) SlideContentEditor.this.selectAll();
            }
        });

        this.registerEvent(ScrollEvent.SCROLL, event -> {
            if(event.isShortcutDown()) {
                this.browser.getEngine().executeScript(String.format("changeFontSize(%1$s);", event.getDeltaY()));
            }
        });

        this.setCenter(this.browser);
    }

    /**
     * Prepare the HTML page that is used to define and edit slides' content and return the {@link java.net.URI} of the
     * page in order to be loaded by a {@link WebView}.
     * @return The {@link java.net.URI} of the page to load.
     */
    private String prepareAndGetEditorPageURI() {
        final File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
        final File editorDir = new File(tempDirectory, "sfx-slide-content-editor");
        final File editorFile = new File(editorDir, "ace-file-editor.html");
        final String uri = editorFile.toURI().toASCIIString();

        if(!editorFile.exists()) {
            try(final InputStream editorZip = ResourceHelper.getInputStream("/com/twasyl/slideshowfx/sfx-slide-content-editor.zip")) {
                ZipUtils.unzip(editorZip, tempDirectory);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not extract the slide content editor ZIP", e);
            }
        }

        return uri;
    }

    /**
     * This method retrieves the content of the Node allowing to define the content of the slide.
     * @return The text contained in the Node for defining content of the slide.
     */
    public String getContentEditorValue() {
        final String valueAsBase64 = (String) this.browser.getEngine().executeScript("getContent();");
        final byte[] valueAsBytes = Base64.getDecoder().decode(valueAsBase64);

        String value = new String(valueAsBytes, getDefaultCharset());

        return value;
    }

    /**
     * This method retrieves the selected content of the Node allowing to define the content of the slide.
     * @return The text contained in the Node for defining content of the slide.
     */
    public String getSelectedContentEditorValue() {
        final String valueAsBase64 = (String) this.browser.getEngine().executeScript("getSelectedContent();");
        final byte[] valueAsBytes = Base64.getDecoder().decode(valueAsBase64);

        String value = new String(valueAsBytes, getDefaultCharset());

        return value;
    }

    /**
     * Set the value for this content editor. This method doesn't append the given {@code value} to the current one
     * present in this editor. In order to append the value use {@link #appendContentEditorValue(String)}.
     * @param value The new value of this editor
     */
    public void setContentEditorValue(final String value) {
        final String encodedValue = Base64.getEncoder().encodeToString(value.getBytes(getDefaultCharset()));

        this.browser.getEngine().executeScript(String.format("setContent('%1$s');", encodedValue));
    }

    /**
     * Append the given value to this content editor. The current caret position is taken in consideration in order to
     * append the value.
     * @param value The value to append to the content editor.
     */
    public void appendContentEditorValue(final String value) {
        final String encodedValue = Base64.getEncoder().encodeToString(value.getBytes(getDefaultCharset()));

        this.browser.getEngine().executeScript(String.format("appendContent('%1$s');", encodedValue));
    }

    /**
     * Select all text that is currently in the editor.
     */
    public void selectAll() {
        this.browser.getEngine().executeScript("selectAll();");
    }

    /**
     * Removes the selected text in the editor.
     */
    public void removeSelection() {
        this.browser.getEngine().executeScript("removeSelection();");
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

    /**
     * Make the editor requests the focus in the application. The text that is already present in the editor will be
     * fully selected and the editor will ask for the focus.
     */
    @Override
    public void requestFocus() {
        PlatformHelper.run(() -> {
            this.browser.requestFocus();
            this.browser.getEngine().executeScript("selectAll();");
            this.browser.getEngine().executeScript("requestEditorFocus();");
        });
    }

    /**
     * Register an handler to this browser.
     * @param eventType The type of event to register.
     * @param handler The handler of the event.
     */
    public <T extends Event> void  registerEvent(EventType<T> eventType, EventHandler<? super T> handler) {
        this.browser.addEventHandler(eventType, handler);
    }
}

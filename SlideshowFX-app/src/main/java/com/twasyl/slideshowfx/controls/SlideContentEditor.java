/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.utils.KeyEventUtils;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
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
 * @since SlideshowFX 1.0.0
 */
public class SlideContentEditor extends BorderPane {
    private static final Logger LOGGER = Logger.getLogger(SlideContentEditor.class.getName());

    private final WebView browser = new WebView();

    public SlideContentEditor() {
        this.browser.getEngine().load(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/html/ace-file-editor.html"));

        this.browser.setOnKeyPressed(event -> {
            final boolean isShortcutDown = event.isShortcutDown();
            if(isShortcutDown) {
                if(KeyEventUtils.is(KeyCode.A, event)) SlideContentEditor.this.selectAll();

                else if(KeyEventUtils.is(KeyCode.C, event)) {
                    final String selection = SlideContentEditor.this.getSelectedContentEditorValue();
                    if (selection != null) {
                        final ClipboardContent content = new ClipboardContent();
                        content.putString(selection);
                        Clipboard.getSystemClipboard().setContent(content);
                    }
                }

                else if(KeyEventUtils.is(KeyCode.X,  event)) {
                    final String selection = SlideContentEditor.this.getSelectedContentEditorValue();
                    if (selection != null) {
                        final ClipboardContent content = new ClipboardContent();
                        content.putString(selection);
                        Clipboard.getSystemClipboard().setContent(content);
                        SlideContentEditor.this.removeSelection();
                    }
                }

                else if(KeyEventUtils.is(KeyCode.V, event)) SlideContentEditor.this.appendContentEditorValue(Clipboard.getSystemClipboard().getString());
            }
        });

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
     * This method retrieves the selected content of the Node allowing to define the content of the slide.
     * @return The text contained in the Node for defining content of the slide.
     */
    public String getSelectedContentEditorValue() {
        final String valueAsBase64 = (String) this.browser.getEngine().executeScript("getSelectedContent();");
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

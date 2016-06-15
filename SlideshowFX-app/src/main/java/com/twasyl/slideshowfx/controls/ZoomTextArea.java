package com.twasyl.slideshowfx.controls;

import javafx.scene.control.TextArea;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Font;

/**
 * A simple text area allowing to zoom in and out using the mouse wheel.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class ZoomTextArea extends TextArea {

    public ZoomTextArea() {
        this.registerZoomEvent();
    }

    public ZoomTextArea(String text) {
        super(text);
        this.registerZoomEvent();
    }

    private void registerZoomEvent() {
        this.registerZoomEventByScroll();
    }

    private void registerZoomEventByScroll() {
        this.addEventHandler(ScrollEvent.SCROLL, event -> {
            if(event.isShortcutDown()) {
                changeFontSize(event.getDeltaY());
            }
        });
    }

    private void changeFontSize(final double factor) {
        final double delta = factor > 0 ? 1 : -1;
        final Font font = this.getFont();

        this.setFont(new Font(font.getName(), font.getSize() + delta));
    }
}

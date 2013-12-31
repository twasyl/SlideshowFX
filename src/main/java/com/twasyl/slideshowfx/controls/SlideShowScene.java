package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.app.SlideshowFX;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

import java.util.logging.Logger;

public class SlideShowScene extends Scene {

    private static final Logger LOGGER = Logger.getLogger(SlideShowScene.class.getName());

    private final ObjectProperty<WebView> browser = new SimpleObjectProperty<>();

    public SlideShowScene(WebView browser) {
        super(browser);
        this.browser.set(browser);

        this.browser.get().getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> stringWebEvent) {
                LOGGER.warning(stringWebEvent.getData());
            }
        });

        this.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                    keyEvent.consume();

                    SlideshowFX.setSlideShowActive(false);
                }
            }
        });
    }

    public ObjectProperty<WebView> browserProperty() { return browser; }
    public WebView getBrowser() { return this.browserProperty().get(); }
    public void setBrowser(WebView browser) { this.browser.set(browser); }

    public void sendKey(final KeyCode keyCode) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(keyCode.equals(KeyCode.LEFT)) browser.get().getEngine().executeScript(String.format("slideShowFXCalling(%1$s)", 37));
                else if(keyCode.equals(KeyCode.RIGHT)) browser.get().getEngine().executeScript(String.format("slideShowFXCalling(%1$s)", 39));
            }
        });
    }
}

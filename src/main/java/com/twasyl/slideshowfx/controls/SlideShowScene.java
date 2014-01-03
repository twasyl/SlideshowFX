package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.chat.Chat;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import java.util.logging.Logger;

public class SlideShowScene extends Scene {

    private static final Logger LOGGER = Logger.getLogger(SlideShowScene.class.getName());

    private final ObjectProperty<WebView> browser = new SimpleObjectProperty<>();
    private final ObjectProperty<WebView> chatBrowser = new SimpleObjectProperty<>();

    public SlideShowScene(WebView browser) {
        super(new StackPane());
        this.browser.set(browser);

        final StackPane stackPane = (StackPane) getRoot();
        stackPane.setAlignment(Pos.BOTTOM_LEFT);
        stackPane.getChildren().add(this.browser.get());

        this.chatBrowser.set(new WebView());

        this.chatBrowser.get().setPrefSize(530, 320);
        this.chatBrowser.get().setMinSize(530, 320);
        this.chatBrowser.get().setMaxSize(530, 320);
        this.chatBrowser.get().getEngine().load(String.format("http://%1$s:%2$s/slideshowfx/chat/presenter", Chat.getIp(), Chat.getPort()));

        // Make this scene available to JavaScript
        this.chatBrowser.get().getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State state, Worker.State state2) {
                if(state2 == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) chatBrowser.get().getEngine().executeScript("window");
                    window.setMember("scene", SlideShowScene.this);
                }
            }
        });

        stackPane.getChildren().add(this.chatBrowser.get());

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

    public void openChat(final boolean open) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                TranslateTransition translation = new TranslateTransition(Duration.millis(500), chatBrowser.get());

                if(open) {
                    translation.setByY(-300);
                } else {
                    translation.setByY(300);
                }

                translation.play();
            }
        });
    }
}

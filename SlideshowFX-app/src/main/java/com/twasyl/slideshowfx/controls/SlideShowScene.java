/*
 * Copyright 2014 Thierry Wasylczenko
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

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.chat.Chat;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the scene that will host the slideshow, as well as the chat. This scene will also provides methods for
 * showing and hiding a pointer on the slideshow.
 *
 * @author Thierry Wasylczenko
 */
public class SlideShowScene extends Scene {

    private static final Logger LOGGER = Logger.getLogger(SlideShowScene.class.getName());

    private final ObjectProperty<WebView> browser = new SimpleObjectProperty<>();
    private final ObjectProperty<WebView> chatBrowser = new SimpleObjectProperty<>();
    private final ObjectProperty<Circle> pointer = new SimpleObjectProperty<>();

    public SlideShowScene(WebView browser) {
        super(new StackPane());
        this.browser.set(browser);

        if(Chat.isOpened()) {
            chatBrowser.set(new WebView());
            chatBrowser.get().setPrefSize(500, 320);
            chatBrowser.get().setMaxSize(500, 320);
            chatBrowser.get().setMinSize(500, 320);
            chatBrowser.get().getEngine().load(String.format("http://%1$s:%2$s/slideshowfx/chat/presenter", Chat.getIp(), Chat.getPort()));

            chatBrowser.get().getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State state, Worker.State state2) {
                    if (state2 == Worker.State.SUCCEEDED) {
                        JSObject window = (JSObject) chatBrowser.get().getEngine().executeScript("window");
                        window.setMember("scene", SlideShowScene.this);
                    }
                }
            });
        }
        this.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                keyEvent.consume();

                SlideShowScene.this.exitSlideshow();
            } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                click();
            }
        });

        this.browser.get().getEngine().setOnError((WebErrorEvent event) -> {
            LOGGER.warning("An error occurred in the slideshow's browser:");
            LOGGER.warning(event.getMessage());
        });

        final StackPane root = getSceneRoot();
        root.setAlignment(Pos.TOP_LEFT);
        root.getChildren().add(this.browser.get());

        if(Chat.isOpened()) {
            root.getChildren().add(this.chatBrowser.get());
            /* This binding is only useful when the scene is displayed. The property is unbind when the chat is
            opened/closed */
            this.chatBrowser.get().translateYProperty().bind(root.heightProperty().subtract(this.chatBrowser.get().heightProperty()));
        }

        this.setCursor(null);
    }

    public ObjectProperty<WebView> browserProperty() { return browser; }
    public WebView getBrowser() { return this.browserProperty().get(); }
    public void setBrowser(WebView browser) { this.browser.set(browser); }

    public StackPane getSceneRoot() {
        return (StackPane) this.getRoot();
    }

    /**
     * Exit the slide show mode. This method calls SlideshowFX#setSlideShowActive and set the value to <code>false</code>.
     */
    public void exitSlideshow() {
        PlatformHelper.run(() -> {
            SlideshowFX.setSlideShowActive(false);
        });
    }

    /**
     * Send a key to the HTML5 presentation. Currently only the LEFT and RIGHT keycodes are implemented.
     * @param keyCode the key code to send to the HTML5 presentation.
     */
    public void sendKey(final KeyCode keyCode) {
        PlatformHelper.run(() -> {
            if (keyCode.equals(KeyCode.LEFT))
                browser.get().getEngine().executeScript(String.format("slideshowFXLeap('%1$s')", "LEFT"));
            else if (keyCode.equals(KeyCode.RIGHT))
                browser.get().getEngine().executeScript(String.format("slideshowFXLeap('%1$s')", "RIGHT"));
        });
    }

    /**
     * Display or hide the presenter chat on the presentation. The translateY property of the chat is unboud if needed.
     * @param open if true, displays the chat, hide it otherwise.
     */
    public void displayChat(final boolean open) {
        PlatformHelper.run(() -> {
            if(SlideShowScene.this.chatBrowser.get().translateYProperty().isBound()) {
                SlideShowScene.this.chatBrowser.get().translateYProperty().unbind();
            }

            TranslateTransition translation = new TranslateTransition(Duration.millis(500), chatBrowser.get());

            if (open) translation.setByY(-300);
            else translation.setByY(300);

            translation.play();
        });
    }

    /**
     * Show a circular red pointer on the presentation.
     * @param x The X position of the pointer. The coordinate is considered as the center of the pointer.
     * @param y The Y position of the pointer. The coordinate is considered as the center of the pointer.
     */
    public void showPointer(double x, double y) {
        PlatformHelper.run(() -> {
            if (SlideShowScene.this.pointer.get() == null) {
                SlideShowScene.this.pointer.set(new Circle(10d, new Color(1, 0, 0, 0.5)));
            }

            if (!SlideShowScene.this.getSceneRoot().getChildren().contains(SlideShowScene.this.pointer.get())) {
                SlideShowScene.this.pointer.get().setLayoutX(0);
                SlideShowScene.this.pointer.get().setLayoutY(0);
                SlideShowScene.this.getSceneRoot().getChildren().add(SlideShowScene.this.pointer.get());
            }

            SlideShowScene.this.pointer.get().setTranslateX(x - SlideShowScene.this.pointer.get().getRadius());
            SlideShowScene.this.pointer.get().setTranslateY(y - SlideShowScene.this.pointer.get().getRadius());
        });
    }

    /**
     * Hides the pointer which is displayed on the HTML5 presentation.
     */
    public void hidePointer() {
        if(this.pointer.get() != null) {
            PlatformHelper.run(() -> {
                ((StackPane) SlideShowScene.this.getRoot()).getChildren().remove(SlideShowScene.this.pointer.get());
            });
        }
    }

    /**
     * Performs a click on the scene where the pointer is located. This method uses the {@link java.awt.Robot} class to
     * move the mouse at the location of the pointer and perform a click. The coordinates used for the click are the center
     * of the pointer and takes into account a multiple screen environment.
     */
    public void click() {
        if(this.pointer.get() != null && this.getSceneRoot().getChildren().contains(this.pointer.get())) {

            /**
             * In a multi screen environment, we need to apply a delta on the coordinates. Indeed
             * the location of the window (X and Y) takes in consideration this environment. For instance
             * if you have 3 screens positioned horizontally and the main screen is the middle one:
             * <ul>
             *     <li>If the app is displayed on left screen, the X coordinate of the window will be negative
             *     (according the width of the screen)</li>
             *     <li>If the app is displayed on the right screen, the X coordinate of the window will be positive
             *     (according the width of the screen)</li>
             *     <li>If the app is displayed on the middle screen, the X coordinate will be 0</li>
             * </ul>
             */

            double clickX = this.pointer.get().getTranslateX() + this.pointer.get().getRadius();
            clickX += super.getWindow().getX();

            double clickY = this.pointer.get().getTranslateY() + this.pointer.get().getRadius();
            clickY += super.getWindow().getY();

            /**
             * The pointer has to be removed because if not, the click is performed on it, and not on elements
             * of the scene. It will be added when click is performed.
             */
            SlideShowScene.this.getSceneRoot().getChildren().remove(SlideShowScene.this.pointer.get());

            try {
                Robot robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
                robot.mouseMove((int) clickX, (int) clickY);
                robot.mousePress(InputEvent.BUTTON1_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
            } catch (AWTException e) {
                LOGGER.log(Level.WARNING, "Can not simulate click", e);
            } finally {
                SlideShowScene.this.getSceneRoot().getChildren().add(SlideShowScene.this.pointer.get());
            }
        }
    }
}

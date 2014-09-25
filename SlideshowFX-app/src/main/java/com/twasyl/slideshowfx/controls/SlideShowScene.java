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
import com.twasyl.slideshowfx.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.beans.quizz.QuizzResult;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

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
    private static SlideShowScene singleton = null;

    private final ObjectProperty<WebView> browser = new SimpleObjectProperty<>();
    private final ObjectProperty<Circle> pointer = new SimpleObjectProperty<>();

    private final ChatPanel chatPanel = new ChatPanel();
    private final QuizzPanel quizzPanel = new QuizzPanel();
    private final CollapsibleToolPane collapsibleToolPane = new CollapsibleToolPane();

    public SlideShowScene(WebView browser) {
        super(new StackPane());
        SlideShowScene.singleton = this;

        this.browser.set(browser);

        this.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                keyEvent.consume();

                SlideShowScene.this.exitSlideshow();
            } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                click();
            }
        });

        final StackPane root = getSceneRoot();
        root.setAlignment(Pos.TOP_LEFT);
        root.getChildren().add(this.browser.get());

       if(SlideshowFXServer.getSingleton() != null) {
           this.initializeChatPanel();

           final ImageView qrCode = new ImageView(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/images/qrcode.png"));
           final ImageView chatImage = new ImageView(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/images/chat.png"));
           final ImageView quizzImage = new ImageView(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/images/quizz.png"));

           this.collapsibleToolPane.addContent(qrCode, new QRCodePanel())
                                    .addContent(chatImage, this.chatPanel)
                                    .addContent(quizzImage, this.quizzPanel);

           root.getChildren().add(this.collapsibleToolPane);
        }

        this.setCursor(null);
    }

    /**
     * Retrieve the chat history and display it in the {@link #chatPanel}.
     */
    private void initializeChatPanel() {
        final JsonObject request = new JsonObject()
                .putString("service", "slideshowfx.chat.attendee.history")
                .putObject("data", new JsonObject());

        final JsonElement history = SlideshowFXServer.getSingleton().callService(request.encode());

        if(history != null) {
            if(history.isArray()) {
                JsonArray messages = history.asArray();

                for(Object message : messages) {
                    this.publishMessage((JsonObject) message);
                }
            }
        }
    }

    /**
     * Get the instance of the SlideShowScene.
     * @return The instance of the SlideShowScene or null if none.
     */
    public static SlideShowScene getSingleton() { return singleton; }

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
            SlideShowScene.singleton = null;
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
     * This method publish the given <code>chatMessage</code> to the presenter.
     * @param chatMessage The message to publish.
     * @throws java.lang.NullPointerException If the message is null
     */
    public void publishMessage(JsonObject chatMessage) {
        if(chatMessage == null) throw new NullPointerException("The message to publish can not be null");

        PlatformHelper.run(() -> {
            this.chatPanel.addMessage(ChatMessage.build(chatMessage.encode(), null));
        });
    }

    /**
     * This method publish the given {@link com.twasyl.slideshowfx.beans.quizz.QuizzResult} to the scene.
     * @param result The result to publish.
     * @throws java.lang.NullPointerException If the result is null
     */
    public void publishQuizzResult(QuizzResult result) {
        if(result == null) throw new NullPointerException("The QuizzResult to publish can not be null");

        this.quizzPanel.setQuizzResult(result);
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

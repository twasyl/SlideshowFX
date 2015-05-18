/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controls.slideshow;

import com.twasyl.slideshowfx.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.beans.quizz.QuizzResult;
import com.twasyl.slideshowfx.controls.*;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A pane that displays a presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class SlideshowPane extends StackPane {
    private static final Logger LOGGER = Logger.getLogger(SlideshowPane.class.getName());

    private static SlideshowPane singleton = null;

    private final Context context;
    private final ObjectProperty<PresentationBrowser> browser = new SimpleObjectProperty<>();
    private final ObjectProperty<Circle> pointer = new SimpleObjectProperty<>();

    private final ChatPanel chatPanel = new ChatPanel();
    private final QuizzPanel quizzPanel = new QuizzPanel();
    private final CollapsibleToolPane collapsibleToolPane = new CollapsibleToolPane();

    /**
     * Creates a SlideshowPane object for the given {@code context}. The slideshow will be started at the
     * given {@link Context#getStartAtSlideId()}.
     *
     * @param context The context to create the {@link SlideshowPane}.
     * @see com.twasyl.slideshowfx.controls.slideshow.Context
     */
    public SlideshowPane(Context context) {
        super();

        this.setAlignment(Pos.TOP_LEFT);
        this.getStylesheets().add(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/css/Default.css"));

        this.context = context;

        if(SlideshowFXServer.getSingleton() != null) {
            this.initializeChatPanel();
            this.initializeCollapsibleToolPane();
        }
        this.initializeBrowser();

        this.setCursor(null);
    }

    /**
     * Initialize the browser that displays the presentation.
     */
    private final void initializeBrowser() {
        this.browser.set(new PresentationBrowser());
        this.browser.get().setBackend(this);
        this.getChildren().add(this.browser.get());
    }

    /**
     * Initialize the pane that contains all buttons related when the server is running (chat, QR code, quiz).
     */
    private final void initializeCollapsibleToolPane() {
        final FontAwesomeIconView qrCodeIcon = new FontAwesomeIconView(FontAwesomeIcon.QRCODE);
        qrCodeIcon.setGlyphSize(32);
        qrCodeIcon.setGlyphStyle("-fx-fill: app-color-orange");

        final FontAwesomeIconView chatIcon = new FontAwesomeIconView(FontAwesomeIcon.COMMENTS_ALT);
        chatIcon.setGlyphSize(32);
        chatIcon.setGlyphStyle("-fx-fill: app-color-orange");

        final FontAwesomeIconView quizzIcon = new FontAwesomeIconView(FontAwesomeIcon.QUESTION);
        quizzIcon.setGlyphSize(32);
        quizzIcon.setGlyphStyle("-fx-fill: app-color-orange");

        this.collapsibleToolPane.addContent(qrCodeIcon, new QRCodePanel())
                .addContent(chatIcon, this.chatPanel)
                .addContent(quizzIcon, this.quizzPanel);

        this.getChildren().add(this.collapsibleToolPane);
    }

    /**
     * Get the instance of {@link SlideshowPane} that is currently displayed.
     * @return The instance of {@link SlideshowPane} currently displayed or {@code null} if there's none.
     */
    public static SlideshowPane getSingleton() { return singleton; }

    /**
     * Closes the slideshow pane. It takes care to set to {@code null} the singleton for this class.
     */
    public void close() {
        SlideshowPane.singleton = null;
    }

    /**
     * This method is called by the presentation in order to execute a code snippet. The executor is identified by the
     * {@code snippetExecutorCode} and retrieved in the OSGi context to get the {@link com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor}
     * instance that will execute the code.
     * The code to execute is passed to this method in Base64 using the {@code base64CodeSnippet} parameter. The execution
     * result will be pushed back to the presentation in the HTML element {@code consoleOutputId}.
     *
     * @param snippetExecutorCode The unique identifier of the executor that will execute the code.
     * @param base64CodeSnippet The code snippet to execute, given in Base64.
     * @param consoleOutputId The HTML element that will be updated with the execution result.
     */
    public void executeCodeSnippet(final String snippetExecutorCode, final String base64CodeSnippet, final String consoleOutputId) {

        if(snippetExecutorCode != null) {
            final Optional<ISnippetExecutor> snippetExecutor = OSGiManager.getInstalledServices(ISnippetExecutor.class)
                    .stream()
                    .filter(executor -> snippetExecutorCode.equals(executor.getCode()))
                    .findFirst();

            if(snippetExecutor.isPresent()) {
                try {
                    final CodeSnippet codeSnippetDecoded = CodeSnippet.toObject(new String(Base64.getDecoder().decode(base64CodeSnippet), "UTF8"));
                    final ObservableList<String> consoleOutput = snippetExecutor.get().execute(codeSnippetDecoded);

                    consoleOutput.addListener((ListChangeListener<String>) change -> {
                        // Push the execution result to the presentation.
                        PlatformHelper.run(() -> {
                            while (change.next()) {
                                if (change.wasAdded()) {
                                    change.getAddedSubList()
                                            .stream()
                                            .forEach(line -> this.browser.get().updateCodeSnippetConsole(consoleOutputId, line));
                                }
                            }
                            change.reset();
                        });
                    });
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.SEVERE, "Can not decode code snippet", e);
                }
            }
        }
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

    public ObjectProperty<PresentationBrowser> browserProperty() { return browser; }
    public PresentationBrowser getBrowser() { return this.browserProperty().get(); }
    public void setBrowser(PresentationBrowser browser) { this.browser.set(browser); }

    /**
     * Send a key to the HTML5 presentation. Currently only the LEFT and RIGHT keycodes are implemented.
     * @param keyCode the key code to send to the HTML5 presentation.
     */
    public void sendKey(final KeyCode keyCode) {
        PlatformHelper.run(() -> {
            if (keyCode.equals(KeyCode.LEFT))
                this.browser.get().previousSlide();
            else if (keyCode.equals(KeyCode.RIGHT))
                this.browser.get().nextSlide();
        });
    }

    /**
     * This method publish the given <code>chatMessage</code> to the presenter.
     * @param chatMessage The message to publish.
     * @throws java.lang.NullPointerException If the message is null
     */
    public void publishMessage(JsonObject chatMessage) {
        if(chatMessage == null) throw new NullPointerException("The message to publish can not be null");

        PlatformHelper.run(() -> this.chatPanel.addMessage(ChatMessage.build(chatMessage.encode(), null)));
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
            if (this.pointer.get() == null) {
                this.pointer.set(new Circle(10d, new Color(1, 0, 0, 0.5)));
            }

            if (!this.getChildren().contains(this.pointer.get())) {
                this.pointer.get().setLayoutX(0);
                this.pointer.get().setLayoutY(0);
                this.getChildren().add(this.pointer.get());
            }

            this.pointer.get().setTranslateX(x - this.pointer.get().getRadius());
            this.pointer.get().setTranslateY(y - this.pointer.get().getRadius());
        });
    }

    /**
     * Hides the pointer which is displayed on the HTML5 presentation.
     */
    public void hidePointer() {
        if(this.pointer.get() != null) {
            PlatformHelper.run(() -> this.getChildren().remove(this.pointer.get()));
        }
    }

    /**
     * Performs a click on the scene where the pointer is located. This method uses the {@link java.awt.Robot} class to
     * move the mouse at the location of the pointer and perform a click. The coordinates used for the click are the center
     * of the pointer and takes into account a multiple screen environment.
     */
    public void click() {
        PlatformHelper.run(() -> {
            if (this.pointer.get() != null && this.getChildren().contains(this.pointer.get())) {

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
                clickX += this.getLayoutX();

                double clickY = this.pointer.get().getTranslateY() + this.pointer.get().getRadius();
                clickY += this.getLayoutY();

                /**
                 * The pointer has to be removed because if not, the click is performed on it, and not on elements
                 * of the scene.
                 */
                this.hidePointer();

                try {
                    Robot robot = new Robot();
                    robot.mouseMove((int) clickX, (int) clickY);
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                } catch (AWTException e) {
                    LOGGER.log(Level.WARNING, "Can not simulate click", e);
                }
            }
        });
    }
}

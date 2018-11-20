package com.twasyl.slideshowfx.controls.slideshow;

import com.twasyl.slideshowfx.controls.*;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.icons.FontAwesome;
import com.twasyl.slideshowfx.icons.Icon;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.server.beans.quiz.QuizResult;
import com.twasyl.slideshowfx.server.bus.Actor;
import com.twasyl.slideshowfx.server.bus.EventBus;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

import java.util.Base64;
import java.util.Optional;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.server.service.AbstractSlideshowFXService.*;
import static com.twasyl.slideshowfx.server.service.ISlideshowFXServices.SERVICE_CHAT_ATTENDEE_HISTORY;
import static com.twasyl.slideshowfx.server.service.PresenterChatService.SERVICE_CHAT_PRESENTER_ON_MESSAGE;
import static com.twasyl.slideshowfx.server.service.QuizService.SERVICE_QUIZ_ON_RESULT;
import static javafx.geometry.HPos.RIGHT;

/**
 * A pane that displays a presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
public class SlideshowPane extends StackPane implements Actor {
    private static final Logger LOGGER = Logger.getLogger(SlideshowPane.class.getName());

    private final ObjectProperty<PresentationBrowser> browser = new SimpleObjectProperty<>();

    private final ChatPanel chatPanel = new ChatPanel();
    private final QuizPanel quizPanel = new QuizPanel();
    private final CollapsibleToolPane collapsibleToolPane = new CollapsibleToolPane();

    /**
     * Creates a SlideshowPane object for the given {@code context}. The slideshow will be started at the
     * given {@link Context#getStartAtSlideId()}.
     *
     * @see Context
     */
    public SlideshowPane() {
        super();

        EventBus.getInstance()
                .subscribe(SERVICE_QUIZ_ON_RESULT, this)
                .subscribe(SERVICE_CHAT_PRESENTER_ON_MESSAGE, this);

        this.getStyleClass().add("slideshow-pane");
        this.setAlignment(Pos.TOP_LEFT);

        this.initializeBrowser();

        if (SlideshowFXServer.getSingleton() != null) {
            this.initializeChatPanel();
            this.initializeCollapsibleToolPane();
        }

        this.setCursor(null);
    }

    @Override
    public boolean supportsMessage(Object message) {
        return message != null && (message instanceof QuizResult || message instanceof JsonObject);
    }

    @Override
    public void onMessage(Object message) {
        if (message instanceof QuizResult) {
            this.publishQuizResult((QuizResult) message);
        } else if (message instanceof JsonObject) {

            final JsonObject jsonMessage = (JsonObject) message;

            if ("chat-message".equals(jsonMessage.getString(JSON_KEY_BROADCAST_MESSAGE_TYPE))) {
                final JsonObject content = jsonMessage.getJsonObject(JSON_KEY_MESSAGE);

                if (content != null) {
                    this.publishMessage(ChatMessage.build(content.encode(), null));
                }
            }
        }
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
        final FontAwesome qrCodeIcon = new FontAwesome(Icon.QRCODE);
        final FontAwesome chatIcon = new FontAwesome(Icon.COMMENTS_O);
        final FontAwesome quizIcon = new FontAwesome(Icon.QUESTION);

        this.collapsibleToolPane.setPosition(RIGHT);

        this.collapsibleToolPane.addContent(qrCodeIcon, new QRCodePanel())
                .addContent(chatIcon, this.chatPanel)
                .addContent(quizIcon, this.quizPanel);

        this.getChildren().add(this.collapsibleToolPane);
    }

    /**
     * This method is called by the presentation in order to execute a code snippet. The executor is identified by the
     * {@code snippetExecutorCode} and retrieved in the OSGi context to get the {@link ISnippetExecutor}
     * instance that will execute the code.
     * The code to execute is passed to this method in Base64 using the {@code base64CodeSnippet} parameter. The execution
     * result will be pushed back to the presentation in the HTML element {@code consoleOutputId}.
     *
     * @param snippetExecutorCode The unique identifier of the executor that will execute the code.
     * @param base64CodeSnippet   The code snippet to execute, given in Base64.
     * @param consoleOutputId     The HTML element that will be updated with the execution result.
     */
    public void executeCodeSnippet(final String snippetExecutorCode, final String base64CodeSnippet, final String consoleOutputId) {

        if (snippetExecutorCode != null) {
            final Optional<ISnippetExecutor> snippetExecutor = OSGiManager.getInstance().getInstalledServices(ISnippetExecutor.class)
                    .stream()
                    .filter(executor -> snippetExecutorCode.equals(executor.getCode()))
                    .findFirst();

            if (snippetExecutor.isPresent()) {
                final String decodedSnippet = new String(Base64.getDecoder().decode(base64CodeSnippet), GlobalConfiguration.getDefaultCharset());
                final CodeSnippet codeSnippetDecoded = CodeSnippet.toObject(decodedSnippet);
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
            }
        }
    }

    /**
     * Retrieve the chat history and display it in the {@link #chatPanel}.
     */
    private void initializeChatPanel() {
        final JsonObject request = new JsonObject()
                .put(JSON_KEY_SERVICE, SERVICE_CHAT_ATTENDEE_HISTORY)
                .put(JSON_KEY_DATA, new JsonObject());

        final JsonArray history = SlideshowFXServer.getSingleton().callService(request.encode())
                .getJsonArray(JSON_KEY_CONTENT);

        if (history != null) {
            for (Object message : history) {
                this.publishMessage(ChatMessage.build(((JsonObject) message).encode(), null));
            }
        }
    }

    public ObjectProperty<PresentationBrowser> browserProperty() {
        return browser;
    }

    public PresentationBrowser getBrowser() {
        return this.browserProperty().get();
    }

    public void setBrowser(PresentationBrowser browser) {
        this.browser.set(browser);
    }

    /**
     * This method publish the given <code>chatMessage</code> to the presenter.
     *
     * @param chatMessage The message to publish.
     * @throws NullPointerException If the message is null
     */
    public void publishMessage(ChatMessage chatMessage) {
        if (chatMessage == null) throw new NullPointerException("The message to publish can not be null");

        PlatformHelper.run(() -> this.chatPanel.addMessage(chatMessage));
    }

    /**
     * This method publish the given {@link QuizResult} to the scene.
     *
     * @param result The result to publish.
     * @throws NullPointerException If the result is null
     */
    public void publishQuizResult(QuizResult result) {
        if (result == null) throw new NullPointerException("The QuizResult to publish can not be null");

        this.quizPanel.setQuizResult(result);
    }
}

package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessageSource;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessageStatus;
import io.vertx.core.json.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.util.function.Consumer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allow to use Twitter in the chat.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class TwitterService extends AbstractSlideshowFXService {
    private static final Logger LOGGER = Logger.getLogger(TwitterService.class.getName());

    private Configuration twitterConfiguration;
    private Twitter twitter = null;
    private TwitterStream twitterStream = null;

    private final ObjectProperty<RequestToken> requestToken = new SimpleObjectProperty<>();
    private final ObjectProperty<AccessToken> accessToken = new SimpleObjectProperty<>();

    @Override
    public void start() {
        final String hashtag = SlideshowFXServer.getSingleton().getTwitterHashtag();

        this.twitterConfiguration = new ConfigurationBuilder()
                .setOAuthConsumerKey("5luxVGxswd42RgTfbF02g")
                .setOAuthConsumerSecret("winWDhMbeJZ4m66gABqpohkclLDixnyeOINuVtPWs")
                .build();

        this.accessToken.addListener((value, oldValue, newValue) -> {
            this.launchTwitter();
        });

        if(hashtag != null && !hashtag.isEmpty()) {
            if(this.accessToken.get() == null) this.connect();
            else this.launchTwitter();
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Can not stop the TwitterService properly", e);
        }

        if(this.twitterStream != null) {
            try {
                new Thread(() -> {
                    this.twitterStream.shutdown();
                }).start();
            } catch(Exception e) {
                LOGGER.log(Level.SEVERE, "Can not stop the Twitter stream", e);
            }
        }
    }

    /**
     * Connect to the Twitter service by asking the user to log in. Once the connection is successful, the {@link #accessToken}
     * is udpated.
     */
    private void connect() {
        this.twitter = TwitterFactory.getSingleton();

        try {
            this.twitter.setOAuthConsumer(this.twitterConfiguration.getOAuthConsumerKey(),
                    this.twitterConfiguration.getOAuthConsumerSecret());
        } catch(IllegalStateException e) {
            LOGGER.fine("Consumer keys already set up");
        }

        try {
            this.requestToken.set(this.twitter.getOAuthRequestToken());
            final String authUrl = this.requestToken.get().getAuthorizationURL();

            Platform.runLater(() -> {

                final WebView twitterBrowser = new WebView();
                final Scene scene = new Scene(twitterBrowser);
                final Stage stage = new Stage();

                twitterBrowser.getEngine().load(authUrl);

                twitterBrowser.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        if (twitterBrowser.getEngine().getDocument().getDocumentURI().equals("https://api.twitter.com/oauth/authorize")) {
                            String pinCode = twitterBrowser.getEngine().getDocument().getElementsByTagName("kbd").item(0).getTextContent();

                            try {
                                TwitterService.this.accessToken.set(twitter.getOAuthAccessToken(requestToken.get(), pinCode));
                                twitter.verifyCredentials();
                            } catch (TwitterException e) {
                                LOGGER.log(Level.SEVERE, "Error while connecting to Twitter", e);
                            }

                            stage.close();
                        }
                    }
                });

                stage.setScene(scene);
                stage.show();
            });
        } catch (TwitterException | IllegalStateException e) {
            LOGGER.fine("Seems to be already connected to Twitter");
            try {
                this.accessToken.set(this.twitter.getOAuthAccessToken());
            } catch (TwitterException e1) {
                LOGGER.log(Level.SEVERE, "Can not connect to Twitter", e1);
            }
        }
    }

    /**
     * Start the {@link TwitterStream}.
     */
    private void launchTwitter() {
        if(this.accessToken.get() != null) {
            final FilterQuery query = new FilterQuery();
            query.track(new String[]{SlideshowFXServer.getSingleton().getTwitterHashtag()});

            this.twitterStream = new TwitterStreamFactory(this.twitterConfiguration).getInstance(this.accessToken.get());
            this.twitterStream.onStatus(this.buildTwitterStatusConsumer());
            this.twitterStream.filter(query);
        }
    }

    private Consumer<Status> buildTwitterStatusConsumer() {
        final Consumer<Status> statusConsumer = status -> {
            final ChatMessage chatMessage = new ChatMessage();
            chatMessage.setId(System.currentTimeMillis() + "");
            chatMessage.setSource(ChatMessageSource.TWITTER);
            chatMessage.setStatus(ChatMessageStatus.NEW);
            chatMessage.setAuthor("@" + status.getUser().getScreenName());
            chatMessage.setContent(status.getText());

            final JsonObject jsonTweet = chatMessage.toJSON();

            System.out.println(jsonTweet.toString());
            TwitterService.this.vertx.eventBus().publish(SERVICE_CHAT_ATTENDEE_MESSAGE_ADD, jsonTweet);
            TwitterService.this.vertx.eventBus().publish(SERVICE_CHAT_PRESENTER_MESSAGE_ADD, jsonTweet);
        };

        return statusConsumer;
    }
}

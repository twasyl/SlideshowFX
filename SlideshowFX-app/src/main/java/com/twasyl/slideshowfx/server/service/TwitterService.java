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

package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.beans.chat.ChatMessageSource;
import com.twasyl.slideshowfx.beans.chat.ChatMessageStatus;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import io.vertx.core.json.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allow to use Twitter in the chat.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
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

        if(hashtag != null && !hashtag.isEmpty()) {
            this.connect();
            this.accessToken.addListener((value, oldValue, newValue) -> {
                if (newValue != null) {
                    FilterQuery query = new FilterQuery();
                    query.track(new String[]{hashtag});

                    this.twitterStream = new TwitterStreamFactory(this.twitterConfiguration).getInstance(this.accessToken.get());
                    this.twitterStream.addListener(this.buildTwitterStreamListener());
                    this.twitterStream.filter(query);
                }
            });
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
                this.twitterStream.shutdown();
            } catch(Exception e) {
                LOGGER.log(Level.SEVERE, "Can not stop the Twitter stream", e);
            }
        }
    }

    private void connect() {
        this.twitter = TwitterFactory.getSingleton();

        try {
            this.twitter.setOAuthConsumer(this.twitterConfiguration.getOAuthConsumerKey(),
                    this.twitterConfiguration.getOAuthConsumerSecret());
        } catch(IllegalStateException e) {
            LOGGER.fine("Consumer keys already set up");
        }

        try {
            this.requestToken.set(twitter.getOAuthRequestToken());
            final String authUrl = this.requestToken.get().getAuthorizationURL();

            Platform.runLater(() -> {

                final WebView twitterBrowser = new WebView();
                final Scene scene = new Scene(twitterBrowser);
                final Stage stage = new Stage();

                twitterBrowser.getEngine().load(authUrl);

                twitterBrowser.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State state, Worker.State state2) {
                        if (state2 == Worker.State.SUCCEEDED) {
                            if (twitterBrowser.getEngine().getDocument().getDocumentURI().equals("https://api.twitter.com/oauth/authorize")) {
                                String pinCode = twitterBrowser.getEngine().getDocument().getElementsByTagName("kbd").item(0).getTextContent();

                                try {
                                    TwitterService.this.accessToken.set(twitter.getOAuthAccessToken(requestToken.get(), pinCode));
                                    twitter.verifyCredentials();
                                } catch (TwitterException e) {
                                    e.printStackTrace();
                                }

                                stage.close();
                            }
                        }
                    }
                });

                stage.setScene(scene);
                stage.show();
            });
        } catch (TwitterException | IllegalStateException e) {
            LOGGER.fine("Seems to be already connected to Twitter");
        }
    }

    private StatusListener buildTwitterStreamListener() {
        final StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                final ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(System.currentTimeMillis() + "");
                chatMessage.setSource(ChatMessageSource.TWITTER);
                chatMessage.setStatus(ChatMessageStatus.NEW);
                chatMessage.setAuthor("@" + status.getUser().getScreenName());
                chatMessage.setContent(status.getText());

                final JsonObject jsonTweet = chatMessage.toJSON();

                TwitterService.this.vertx.eventBus().publish(SERVICE_CHAT_ATTENDEE_MESSAGE_ADD, jsonTweet);
                TwitterService.this.vertx.eventBus().publish(SERVICE_CHAT_PRESENTER_MESSAGE_ADD, jsonTweet);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int i) {

            }

            @Override
            public void onScrubGeo(long l, long l1) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }
        };

        return listener;
    }
}

package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessageSource;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessageStatus;
import com.twasyl.slideshowfx.server.exceptions.TwitterException;
import io.vertx.core.json.JsonObject;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.server.exceptions.TwitterException.ErrorCodes.NOT_AUTHENTICATED;
import static com.twasyl.slideshowfx.server.exceptions.TwitterException.ErrorCodes.UNAUTHORIZED_APPLICATION;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.WARNING;

/**
 * This class allow to use Twitter in the chat.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class TwitterService extends AbstractSlideshowFXService {
    private static final Logger LOGGER = Logger.getLogger(TwitterService.class.getName());

    protected static final String PROPERTY_PREFIX = "service.twitter.";
    protected static final String ACCESS_TOKEN_PARAMETER = PROPERTY_PREFIX + "accessToken";
    protected static final String ACCESS_TOKEN_SECRET_PARAMETER = PROPERTY_PREFIX + "accessTokenSecret";
    protected static final String HMAC_SHA1 = "HMAC-SHA1";

    protected long nonce;
    protected long timestampInSeconds;

    protected String consumerKey;
    protected String consumerSecret;

    protected String oauthToken;
    protected String oauthTokenSecret;

    protected String pinCode;

    protected String accessToken;
    protected String accessTokenSecret;

    private HttpURLConnection currentConnection;
    private Thread statusesConsumer;

    public TwitterService() {
        this.timestampInSeconds = System.currentTimeMillis() / 1000;
        this.nonce = this.timestampInSeconds + (new Random()).nextInt();
        this.loadTokens();
    }

    /**
     * Load the access token and the access token secret from the configuration.
     */
    protected void loadTokens() {
        this.consumerKey = GlobalConfiguration.getTwitterConsumerKey();
        this.consumerSecret = GlobalConfiguration.getTwitterConsumerSecret();
        this.accessToken = GlobalConfiguration.getProperty(ACCESS_TOKEN_PARAMETER);
        this.accessTokenSecret = GlobalConfiguration.getProperty(ACCESS_TOKEN_SECRET_PARAMETER);
    }

    /**
     * Save the access token and the access token secret to the configuration.
     */
    protected void saveTokens() {
        GlobalConfiguration.setProperty(ACCESS_TOKEN_PARAMETER, this.accessToken);
        GlobalConfiguration.setProperty(ACCESS_TOKEN_SECRET_PARAMETER, this.accessTokenSecret);
    }

    /**
     * Check if the credentials to authenticated the user are valid. The check consists in :
     * <p>
     * <ul>
     * <li>verifying if both access token and acces token secret aren't {@code null} nor empty;</li>
     * <li>if both access token and access token secret can be used to verify the credentials on Twitter (using the {@link #buildVerifyCredentialsURL()})</li>
     * </ul>
     *
     * @return {@code true} if the credentials allow to authenticate to Twitter, {@code false} otherwise.
     */
    protected boolean canAuthenticate() {
        boolean canAuthenticate = this.accessToken != null && this.accessTokenSecret != null
                && !this.accessToken.isEmpty() && !this.accessTokenSecret.isEmpty();

        if (canAuthenticate) {
            final HttpURLConnection connection = this.buildVerifyCredentialsURL();

            try {
                canAuthenticate = connection.getResponseCode() == 200;
            } catch (IOException e) {
                LOGGER.log(WARNING, "Can not determine if the user is authenticated", e);
                canAuthenticate = false;
            } finally {
                connection.disconnect();
            }
        }

        return canAuthenticate;
    }

    /**
     * Start the authentication process to Twitter.
     *
     * @throws TwitterException If the user can be authenticated, or don't allow the application to access Twitter.
     */
    protected void authenticate() throws TwitterException {
        this.currentConnection = this.buildRequestTokenURL();

        try {
            int responseCode = this.currentConnection.getResponseCode();

            if (200 == responseCode) {
                String response = readResponse(this.currentConnection);

                String[] tokens = response.split("&");

                if (tokens.length >= 3) {
                    LOGGER.info("Response content: " + response);
                    final String oauthTokenKey = "oauth_token=";
                    final String oauthTokenSecret = "oauth_token_secret=";

                    Arrays.stream(tokens)
                            .filter(token -> token.startsWith(oauthTokenKey) || token.startsWith(oauthTokenSecret))
                            .forEach(token -> {
                                if (token.startsWith(oauthTokenKey)) {
                                    this.oauthToken = token.substring(oauthTokenKey.length());
                                } else if (token.startsWith(oauthTokenSecret)) {
                                    this.oauthTokenSecret = token.substring(oauthTokenSecret.length());
                                }
                            });
                    LOGGER.info("OAuth token: " + this.oauthToken);
                    LOGGER.info("OAuth token secret: " + this.oauthTokenSecret);

                    try {
                        this.pinCode = obtainPinCode().get();
                        LOGGER.fine("PIN code: " + this.pinCode);

                        if (this.pinCode != null && !this.pinCode.isEmpty()) {
                            this.currentConnection = this.buildAccessTokenURL();
                            responseCode = this.currentConnection.getResponseCode();

                            if (200 == responseCode) {
                                response = readResponse(this.currentConnection);
                                tokens = response.split("&");

                                Arrays.stream(tokens)
                                        .filter(token -> token.startsWith(oauthTokenKey) || token.startsWith(oauthTokenSecret))
                                        .forEach(token -> {
                                            if (token.startsWith(oauthTokenKey)) {
                                                this.accessToken = token.substring(oauthTokenKey.length());
                                            } else if (token.startsWith(oauthTokenSecret)) {
                                                this.accessTokenSecret = token.substring(oauthTokenSecret.length());
                                            }
                                        });

                                this.oauthToken = null;
                                this.oauthTokenSecret = null;

                                LOGGER.fine("OAuth access token: " + this.accessToken);
                                LOGGER.fine("OAuth access token secret: " + this.accessTokenSecret);

                                this.saveTokens();
                            }
                        } else {
                            throw new TwitterException(NOT_AUTHENTICATED, "Can not obtain PIN code");
                        }
                    } catch (Exception e) {
                        throw new TwitterException(NOT_AUTHENTICATED, "Unable to authenticate", e);
                    }
                } else {
                    LOGGER.fine("Invalid response: " + response);
                    throw new TwitterException(NOT_AUTHENTICATED);
                }
            } else {
                LOGGER.fine("Response code for request token: " + responseCode + ", Message: " + this.currentConnection.getResponseMessage());
                throw new TwitterException(NOT_AUTHENTICATED);
            }
        } catch (IOException e) {
            LOGGER.log(WARNING, "Can not authenticate", e);
            throw new TwitterException(NOT_AUTHENTICATED, "Can not authentication", e);
        } finally {
            this.currentConnection.disconnect();
            this.currentConnection = null;
        }
    }

    /**
     * Build the connection with correct headers to obtain a request token.
     *
     * @return A properly ready to use connection to obtain a request token.
     */
    protected HttpURLConnection buildRequestTokenURL() {
        try {
            final URL url = new URL("https://api.twitter.com/oauth/request_token");

            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", this.buildAuthorizationHeaderValue(connection));
            return connection;
        } catch (IOException e) {
            LOGGER.log(WARNING, "Error when building the request token URL", e);
        }

        return null;
    }

    /**
     * Build the connection with correct headers to obtain a PIN code and authenticate the user.
     *
     * @return A properly ready to use connection to obtain a PIN code and authenticate the user.
     */
    protected HttpURLConnection buildAuthenticateURL() {
        try {
            final URL url = new URL("https://api.twitter.com/oauth/authenticate?oauth_token=" + this.oauthToken);

            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            return connection;
        } catch (IOException e) {
            LOGGER.log(WARNING, "Error when building the access token URL", e);
        }

        return null;
    }

    /**
     * Build the connection with correct headers and body to obtain an access token.
     *
     * @return A properly ready to use connection to obtain an access token.
     */
    protected HttpURLConnection buildAccessTokenURL() {
        try {
            final URL url = new URL("https://api.twitter.com/oauth/access_token");
            final String body = "oauth_verifier=" + encode(this.pinCode, UTF_8.toString());

            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", this.buildAuthorizationHeaderValue(connection));
            connection.setRequestProperty("Content-Length", String.valueOf(body.length()));
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setDoOutput(true);

            try (final OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body.getBytes());
                outputStream.flush();
            }

            return connection;
        } catch (IOException e) {
            LOGGER.log(WARNING, "Error when building the access token URL", e);
        }

        return null;
    }

    /**
     * Build the connection with correct headers to verify an access token and access token secret.
     *
     * @return A properly ready to use connection to verify an access token and access token secret.
     */
    protected HttpURLConnection buildVerifyCredentialsURL() {
        try {
            final URL url = new URL("https://api.twitter.com/1.1/account/verify_credentials.json");

            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", this.buildAuthorizationHeaderValue(connection));

            return connection;
        } catch (IOException e) {
            LOGGER.log(WARNING, "Error when building the access token URL", e);
        }

        return null;
    }

    /**
     * Build the connection with correct headers to obtain and filter the Twitter statuses for a track term.
     *
     * @return A properly ready to use connection to obtain and filter the Twitter statuses for a track term.
     */
    protected HttpURLConnection buildStatusesURL() {
        try {
            final URL url = new URL("https://stream.twitter.com/1.1/statuses/filter.json?track=" + SlideshowFXServer.getSingleton().getTwitterHashtag());

            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", this.buildAuthorizationHeaderValue(connection));
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoInput(true);

            return connection;
        } catch (IOException e) {
            LOGGER.log(WARNING, "Error when building the access token URL", e);
        }

        return null;
    }

    /**
     * Start a WebView for allowing the user to authenticate to Twitter and obtain a PIN code.
     * If the user properly authenticate and authorize the application, the PIN code will be returned. Otherwise the
     * return {@link CompletableFuture} will complete exceptionally.
     *
     * @return a {@link CompletableFuture} with the PIN code.
     */
    protected CompletableFuture<String> obtainPinCode() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.supplyAsync(() -> {
            Platform.runLater(() -> {

                final HttpURLConnection authenticateURL = this.buildAuthenticateURL();
                final WebView twitterBrowser = new WebView();
                final Scene scene = new Scene(twitterBrowser);
                final Stage stage = new Stage();

                twitterBrowser.getEngine().load(authenticateURL.getURL().toExternalForm());

                twitterBrowser.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        final HTMLDocument document = (HTMLDocument) twitterBrowser.getEngine().getDocument();

                        if (document.getDocumentURI().equals("https://api.twitter.com/oauth/authorize")) {

                            final HTMLElement body = document.getBody();
                            final String bodyClasses = body.getClassName();

                            if (bodyClasses.contains("oauth") && bodyClasses.contains("cancelled")) {
                                future.completeExceptionally(new TwitterException(UNAUTHORIZED_APPLICATION));
                                stage.close();
                            } else {
                                final Element oauth_pin = document.getElementById("oauth_pin");

                                if (oauth_pin != null) {
                                    final NodeList elements = oauth_pin.getElementsByTagName("kbd");

                                    if (elements != null && elements.getLength() > 0) {
                                        future.complete(elements.item(0).getTextContent());
                                        stage.close();
                                    } else {
                                        future.completeExceptionally(new TwitterException(UNAUTHORIZED_APPLICATION));
                                        stage.close();
                                    }
                                }
                            }
                        }
                    } else if (newState == Worker.State.FAILED) {
                        stage.close();
                        future.completeExceptionally(new TwitterException(NOT_AUTHENTICATED));
                    }
                });

                stage.setScene(scene);
                stage.show();
            });

            try {
                return future.get();
            } catch (Exception e1) {
                LOGGER.log(Level.SEVERE, "Error when getting the PIN code", e1);
                return null;
            }
        });

        return future;
    }

    /**
     * Reads the response from a connection.
     *
     * @param connection The connection to read the response.
     * @return The body of the connection.
     * @throws IOException
     */
    protected String readResponse(HttpURLConnection connection) throws IOException {
        final StringBuilder response = new StringBuilder();

        try (final BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;

            while ((line = input.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    /**
     * This method will check if {@link #accessToken} is not {@code null} and not empty. If so, it will check if
     * {@link #oauthToken} is not {@code null} and not empty. If so, return null;
     *
     * @return The oauth_token or {@code null}?
     */
    protected String getOauthToken() {
        final String token;

        if (this.accessToken != null && !this.accessToken.isEmpty()) {
            token = this.accessToken;
        } else if (this.oauthToken != null && !this.oauthToken.isEmpty()) {
            token = this.oauthToken;
        } else {
            token = null;
        }

        return token;
    }

    /**
     * This method will build the correct <b>Authorization</b> header for the given connection.
     *
     * @param connection The connection for which create the <b>Authorization</b> header.
     * @return The value of the <b>Authorization</b> header.
     * @throws UnsupportedEncodingException
     */
    protected String buildAuthorizationHeaderValue(final HttpURLConnection connection) throws UnsupportedEncodingException {
        final StringBuilder value = new StringBuilder("OAuth ")
                .append("oauth_consumer_key=\"").append(consumerKey).append("\", ")
                .append("oauth_nonce=\"").append(this.nonce).append("\", ")
                .append("oauth_signature_method=\"").append(HMAC_SHA1).append("\", ")
                .append("oauth_signature=\"").append(encode(buildSignature(connection), UTF_8.toString())).append("\", ")
                .append("oauth_timestamp=\"").append(this.timestampInSeconds).append("\", ");


        final String token = getOauthToken();

        if (token != null) {
            value.append("oauth_token=\"").append(encode(token, UTF_8.toString())).append("\", ");
        }

        value.append("oauth_version=\"1.0\"");

        LOGGER.fine("Authorization header: " + value.toString());

        return value.toString();
    }

    /**
     * Build the OAuth <b>oauth_signature</b> parameter to be included in the <b>Authorization</b> header.
     *
     * @param connection The connection for which build the <b>oauth_signature</b> parameter.
     * @return The value of the <b>oauth_signature</b>.
     */
    protected String buildSignature(HttpURLConnection connection) {
        try {
            final URL url = connection.getURL();
            final String rawURL = url.getProtocol() + "://" + url.getAuthority() + url.getPath();
            final String baseString = new StringBuilder(connection.getRequestMethod()).append("&")
                    .append(encode(rawURL, UTF_8.toString())).append("&")
                    .append(encode(getBaseParamQueryString(connection), UTF_8.toString()))
                    .toString();

            LOGGER.fine("Signature base string: " + baseString);
            Mac mac = Mac.getInstance("HmacSHA1");

            final String keyString;
            if (this.accessTokenSecret != null) {
                keyString = this.consumerSecret + '&' + encode(this.accessTokenSecret, UTF_8.toString());
            } else {
                keyString = this.consumerSecret + '&';
            }

            final SecretKeySpec secretKey = new SecretKeySpec(keyString.getBytes(), HMAC_SHA1);
            mac.init(secretKey);

            byte[] byteHMAC = mac.doFinal(baseString.getBytes());
            final String signature = Base64.getEncoder().encodeToString(byteHMAC).trim();

            LOGGER.fine("Signature: " + signature);
            return signature;
        } catch (InvalidKeyException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to authenticate against Twitter", e);
        }
    }

    /**
     * Build the parameter string to be used in the process of creating the signature. This method will take care of
     * using the correct oauth token as well as include the query string of the given connection.
     *
     * @param connection The connection for which create the parameter string.
     * @return The base parameter query string.
     * @see #buildSignature(HttpURLConnection)
     */
    protected String getBaseParamQueryString(HttpURLConnection connection) {
        final StringBuilder queryString = new StringBuilder("oauth_consumer_key=").append(consumerKey).append("&")
                .append("oauth_nonce=").append(nonce).append("&")
                .append("oauth_signature_method=").append(HMAC_SHA1).append("&")
                .append("oauth_timestamp=").append(timestampInSeconds).append("&");

        try {
            final String token = this.getOauthToken();

            if (token != null) {
                queryString.append("oauth_token=").append(encode(token, UTF_8.toString())).append("&");
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(WARNING, "Unsupported encoding", e);
        }

        queryString.append("oauth_version=1.0");

        final String urlQueryString = connection.getURL().getQuery();
        if (urlQueryString != null && urlQueryString.contains("track")) {
            try {
                queryString.append("&track=").append(encode(SlideshowFXServer.getSingleton().getTwitterHashtag(), UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(WARNING, "Can not add the track element", e);
            }
        }
        LOGGER.fine("Base parameter string: " + queryString);

        return queryString.toString();
    }

    @Override
    public void start() {
        final String twitterHashtag = SlideshowFXServer.getSingleton().getTwitterHashtag();

        if (twitterHashtag != null && !twitterHashtag.trim().isEmpty()) {

            boolean authenticated = canAuthenticate();

            if (!authenticated) {
                try {
                    authenticate();
                    authenticated = true;
                } catch (TwitterException e) {
                    LOGGER.log(WARNING, "The user is not authenticated", e);
                }
            }

            if (authenticated) {
                final Runnable work = () -> {
                    this.currentConnection = this.buildStatusesURL();

                    try {
                        final int responseCode = this.currentConnection.getResponseCode();

                        if (200 == responseCode) {
                            final StringBuilder tweet = new StringBuilder();
                            final InputStream inputStream = this.currentConnection.getInputStream();
                            final byte[] buffer = new byte[512];
                            int bytesRead;

                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                final String str = new String(buffer, 0, bytesRead, UTF_8);
                                tweet.append(str);

                                if (str.endsWith("\n") && !tweet.toString().trim().isEmpty()) {
                                    broadcastTweet(tweet.toString().trim());
                                    tweet.setLength(0);
                                }
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error when reading tweets", e);
                    } finally {
                        LOGGER.fine("Disconnected from the streaming API");
                    }
                };

                this.statusesConsumer = new Thread(work);
                this.statusesConsumer.start();
            }
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();

            if (this.statusesConsumer != null && this.statusesConsumer.isAlive()) {
                this.statusesConsumer.interrupt();
            }

            if (this.currentConnection != null) {
                this.currentConnection.disconnect();
                this.currentConnection = null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Can not stop the TwitterService properly", e);
        }
    }

    /**
     * Broadcast a given Tweet to the event bus.
     *
     * @param tweet The tweet to broadcast.
     */
    protected void broadcastTweet(final String tweet) {
        LOGGER.fine("Broadcasting Tweet: " + tweet);
        final JsonObject jsonTweet = new JsonObject(tweet);

        final ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(jsonTweet.getString("timestamp_ms"));
        chatMessage.setSource(ChatMessageSource.TWITTER);
        chatMessage.setStatus(ChatMessageStatus.NEW);
        chatMessage.setAuthor("@" + jsonTweet.getJsonObject("user").getString("screen_name"));
        chatMessage.setContent(jsonTweet.getString("text"));

        final JsonObject jsonMessage = chatMessage.toJSON();

        TwitterService.this.vertx.eventBus().publish(SERVICE_CHAT_ATTENDEE_MESSAGE_ADD, jsonMessage);
        TwitterService.this.vertx.eventBus().publish(SERVICE_CHAT_PRESENTER_MESSAGE_ADD, jsonMessage);
    }
}

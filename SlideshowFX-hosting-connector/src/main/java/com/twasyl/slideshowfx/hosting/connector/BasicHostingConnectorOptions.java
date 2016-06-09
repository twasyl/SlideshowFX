package com.twasyl.slideshowfx.hosting.connector;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Basic options for an hosting connector. In order to connect to a cloud service, a connector will usually need
 * the consumer key and secret as well as the redirect URI value.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 */
public class BasicHostingConnectorOptions implements IHostingConnectorOptions {
    private final StringProperty consumerKey = new SimpleStringProperty();
    private final StringProperty consumerSecret = new SimpleStringProperty();
    private final StringProperty redirectUri = new SimpleStringProperty();

    public StringProperty consumerKeyProperty() { return this.consumerKey; }
    public String getConsumerKey() { return this.consumerKey.get(); }
    public void setConsumerKey(String consumerKey) { this.consumerKey.set(consumerKey); }

    public StringProperty consumerSecretProperty() { return this.consumerSecret; }
    public String getConsumerSecret() { return this.consumerSecret.get(); }
    public void setConsumerSecret(String consumerSecret) { this.consumerSecret.set(consumerSecret); }

    public StringProperty redirectUriProperty() { return this.redirectUri; }
    public String getRedirectUri() { return this.redirectUri.get(); }
    public void setRedirectUri(String redirectUri) { this.redirectUri.set(redirectUri); }
}

/*
 * Copyright 2016 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.hosting.connector;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Basic options for an hosting connector. In order to connect to a cloud service, a connector will usually need
 * the consumer key and secret as well as the redirect URI value.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
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

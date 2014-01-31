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

package com.twasyl.slideshowfx.utils;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import java.io.Serializable;

public class TwitterUtils implements Serializable {

    public static class AuthenticationInfo {
        private OAuthService service;
        private Token accessToken;

        private AuthenticationInfo(OAuthService service, Token accessToken) {
            this.service = service;
            this.accessToken = accessToken;
        }

        public Token getAccessToken() {
            return accessToken;
        }

        public OAuthService getService() {
            return service;
        }
    }

    public static String getConsumerKey() {
        return "5luxVGxswd42RgTfbF02g";
    }

    public static String getConsumerSecret() {
        return "winWDhMbeJZ4m66gABqpohkclLDixnyeOINuVtPWs";
    }

    public static AuthenticationInfo login() {

        OAuthService service = new ServiceBuilder().provider(TwitterApi.SSL.class)
                .apiKey(TwitterUtils.getConsumerKey())
                .apiSecret(TwitterUtils.getConsumerSecret())
                .callback("oob").build();

       /* Token accessToken = new Token(getAccessToken(),
                getAccessTokenSecret()); */

        return new AuthenticationInfo(service, null);
    }
}

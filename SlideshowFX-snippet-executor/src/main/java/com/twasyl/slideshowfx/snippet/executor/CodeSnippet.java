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

package com.twasyl.slideshowfx.snippet.executor;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represent a code snippet that can be executed.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class CodeSnippet implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(CodeSnippet.class.getName());

    private String code;
    private final Map<String, String> properties = new HashMap<>();

    public CodeSnippet() {
    }

    /**
     * Get the code of this snippet that has been entered by the user.
     * @return The code of this snippet.
     */
    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    /**
     * Get the map of properties associated to this code snippet. This method never returns {@code null} but can return
     * an empty Map.
     * @return The properties of this code snippet.
     */
    public Map<String, String> getProperties() { return properties; }

    /**
     * Gets a property identified by its {@code propertyName}.
     * @param propertyName The name of the property to get the value.
     * @return The value of the property or {@code null} if it hasn't been found.
     */
    public String getProperty(final String propertyName) {
        return this.properties.get(propertyName);
    }

    /**
     * Define a new property identified by its {@code propertyName} and {@code value}.
     * @param propertyName The name of the property to define.
     * @param value The value of the property to define.
     * @return This code snippet in order to provide a fluent API.
     */
    public CodeSnippet putProperty(final String propertyName, final String value) {
        this.properties.put(propertyName, value);
        return this;
    }

    /**
     * Convert this code snippet to a JSON string. The JSON is created using the following syntax:
     * {@code {
     *     "code" : "code encoded in Base64",
     *     "properties": [
     *          {
     *              "name" : "name of the property",
     *              "value" : "value of the property encoded in Base64"
     *          },
     *          {
     *              "name" : "name of another property",
     *              "value" : "value of the property encoded in Base64"
     *          }
     *     ]
     * }}
     *
     * @return The JSON representation of the properties.
     */
    public String toJson() {
        final JsonObject objectJson = new JsonObject();
        objectJson.put("code", Base64.getEncoder().encodeToString(this.code.getBytes()));

        final JsonArray propertiesJson = new JsonArray();

        this.properties.forEach((propertyName, propertyValue) -> {
            propertiesJson.add(new JsonObject()
                    .put("name", propertyName)
                    .put("value", Base64.getEncoder().encodeToString(propertyValue.getBytes())));
        });

        objectJson.put("properties", propertiesJson);

        return objectJson.encode();
    }

    public static CodeSnippet toObject(final String jsonString) {
        final CodeSnippet snippet = new CodeSnippet();

        if(jsonString != null) {
            JsonObject document = new JsonObject(jsonString);

            try {
                snippet.setCode(new String(Base64.getDecoder().decode(document.getString("code").getBytes()), "UTF8"));
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.SEVERE, "Can not decode the code of the snippet");
            }

            JsonArray properties = document.getJsonArray("properties");

            if(properties != null && properties.size() > 0) {
                properties.forEach(property -> {
                    try {
                        snippet.properties.put(((JsonObject) property).getString("name"),
                                new String(Base64.getDecoder().decode(((JsonObject) property).getString("value").getBytes()), "UTF8"));
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.log(Level.SEVERE, "Can not decode a property value", e);
                    }
                });
            }
        }

        return snippet;
    }
}

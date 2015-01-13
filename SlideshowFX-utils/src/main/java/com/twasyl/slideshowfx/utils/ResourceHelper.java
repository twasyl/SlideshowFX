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

package com.twasyl.slideshowfx.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides utility methods for reading resource's content.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class ResourceHelper {
    private static final Logger LOGGER = Logger.getLogger(ResourceHelper.class.getName());

    /**
     * This method reads the content of the resource identified by its URL and return it in a String.
     * @param url The URL of the resource to read the content.
     * @return The String representing the content of the resource
     */
    public static String readResource(String url) {
        final StringBuilder builder = new StringBuilder();

        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceHelper.class.getResourceAsStream(url)))) {
            reader.lines().forEach(line -> builder.append(line).append("\n"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not read the resource content", e);
        }

        return builder.toString();
    }

    /**
     * This method get the external form of the given <code>url</code>.
     * @param url The URL of the resource to get the external form.
     * @return The string representing the external form of the given <code>url</code>
     */
    public static String getExternalForm(String url) {
        return ResourceHelper.class.getResource(url).toExternalForm();
    }

    /**
     * This method get the InputStream for the given internal resource <code>url</code>.
     * @param url The URL of the resource to get the InputStream.
     * @return The InputStream for the given <code>url</code.>
     */
    public static InputStream getInputStream(String url) { return ResourceHelper.class.getResourceAsStream(url); }
}

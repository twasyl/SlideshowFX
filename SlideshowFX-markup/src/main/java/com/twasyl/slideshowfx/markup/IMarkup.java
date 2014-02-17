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

package com.twasyl.slideshowfx.markup;

/**
 * Defines the contract to be considered as a supported markup language for SlideshowFX.
 * @author Thierry Wasylczenko
 */
public interface IMarkup {
    /**
     * Get the code for the markup.
     * @return The code of the markup.
     */
    String getCode();

    /**
     * Get the name of the markup.
     * @return The name of the markup.
     */
    String getName();

    /**
     * Convert the given string written in the markup syntax as HTML.
     * @param markupString The string written in the markup syntax to convert as HTML.
     * @return The HTML representation of the given String.
     * @throws IllegalArgumentException If the given string is null or empty this exception is thrown.
     */
    String convertAsHtml(String markupString) throws IllegalArgumentException;
}

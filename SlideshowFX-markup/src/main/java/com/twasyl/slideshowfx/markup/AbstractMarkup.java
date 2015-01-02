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
 * This class provides a default implementation for {@link com.twasyl.slideshowfx.markup.IMarkup}.
 * A basic implementation of a markup language should use this class instead of <code>IMarkup</code>. <code>IMarkup</code>
 * should only be used for more complex markup language.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public abstract class AbstractMarkup implements IMarkup {
    protected String code;
    protected String name;
    protected String aceMode;

    protected AbstractMarkup(String code, String name, String aceMode) {
        this.code = code;
        this.name = name;
        this.aceMode = aceMode;
    }

    @Override public String getName() { return this.name; }

    @Override public String getCode() { return this.code; }

    @Override public String getAceMode() { return this.aceMode; }
}

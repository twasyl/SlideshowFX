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

package com.twasyl.slideshowfx.markup.markdown;

import com.github.rjeschke.txtmark.Processor;
import com.twasyl.slideshowfx.markup.AbstractMarkup;

/**
 * This class implements the Markdown syntax.
 * This markup language is identified byt the code <code>MARKDOWN</code> which is returned by {@link com.twasyl.slideshowfx.markup.IMarkup#getCode()}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class MarkdownMarkup extends AbstractMarkup {

    public MarkdownMarkup() { super("MARKDOWN", "Markdown", "ace/mode/markdown"); }

    /**
     * This methods convert the given <code>markupString</code> to HTML.
     * This method assumes the given String is in the correct mardodwn format.
     *
     * @param markupString The string written in the markup syntax to convert as HTML.
     * @return the HTML representation of the markdown string.
     * @throws IllegalArgumentException If <code>markupString</code> is null, this exception is thrown.
     */
    @Override
    public String convertAsHtml(String markupString) throws IllegalArgumentException {
        if(markupString == null) throw new IllegalArgumentException("Can not convert " + getName() + " to HTML : the String is null");
        return Processor.process("[$PROFILE$]: extended\n" + markupString);
    }
}

package com.twasyl.slideshowfx.markup;

import com.twasyl.slideshowfx.plugin.AbstractPlugin;

/**
 * This class provides a default implementation for {@link com.twasyl.slideshowfx.markup.IMarkup}.
 * A basic implementation of a markup language should use this class instead of <code>IMarkup</code>. <code>IMarkup</code>
 * should only be used for more complex markup language.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public abstract class AbstractMarkup extends AbstractPlugin implements IMarkup {
    protected String code;
    protected String aceMode;

    protected AbstractMarkup(String code, String name, String aceMode) {
        super(name);
        this.code = code;
        this.aceMode = aceMode;
    }

    @Override public String getCode() { return this.code; }

    @Override public String getAceMode() { return this.aceMode; }
}

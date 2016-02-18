package com.twasyl.slideshowfx.utils.io;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;

import java.io.*;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;

/**
 * Class extending {@link BufferedWriter} that writes to an {@link OutputStream} with the encoding defined by
 * {@link GlobalConfiguration#getDefaultCharset()}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class DefaultCharsetWriter extends BufferedWriter {

    public DefaultCharsetWriter(final File file) throws FileNotFoundException {
        this(new FileOutputStream(file));
    }

    public DefaultCharsetWriter(final OutputStream out) {
        super(new OutputStreamWriter(out, getDefaultCharset()));
    }
}

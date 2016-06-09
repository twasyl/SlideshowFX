package com.twasyl.slideshowfx.utils.io;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;

import java.io.*;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;

/**
 * Class extending {@link BufferedReader} that reads from an {@link InputStream} with the encoding defined by
 * {@link GlobalConfiguration#getDefaultCharset()}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class DefaultCharsetReader extends BufferedReader {

    public DefaultCharsetReader(final File file) throws FileNotFoundException {
        this(new FileInputStream(file));
    }

    public DefaultCharsetReader(final InputStream in) {
        super(new InputStreamReader(in, getDefaultCharset()));
    }

    public DefaultCharsetReader(final Reader reader) {
        super(reader);
    }
}

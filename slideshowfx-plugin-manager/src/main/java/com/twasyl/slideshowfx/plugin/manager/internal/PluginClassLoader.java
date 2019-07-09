package com.twasyl.slideshowfx.plugin.manager.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Class loader for loading a SlideshowFX plugin. The classloader will put all Jar files to the classpath to ensure the
 * plugin can be loaded properly.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class PluginClassLoader extends URLClassLoader {

    private PluginClassLoader(URL[] urls) {
        super(urls);
    }

    /**
     * Creates a new instance of {@link PluginClassLoader} for the given {@link PluginFile file}.
     *
     * @param file The plugin file to load.
     * @return An instance of {@link PluginClassLoader}.
     * @throws IOException
     */
    public static PluginClassLoader newInstance(final PluginFile file) throws IOException {
        if (file == null) throw new NullPointerException("The plugin can not be null");

        try (final ZipFile zipFile = new ZipFile(file)) {
            final URL[] urls = zipFile.stream()
                    .filter(onlyFiles())
                    .map(entry -> entryToUrl(file, entry))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
                    .toArray(new URL[0]);

            return new PluginClassLoader(urls);
        }
    }

    private static Predicate<ZipEntry> onlyFiles() {
        return entry -> !entry.isDirectory();
    }

    private static URL entryToUrl(final PluginFile file, final ZipEntry entry) {
        return fileToUrl(new File(file.getExplodedDir(), entry.getName()));
    }

    private static URL fileToUrl(final File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }
}

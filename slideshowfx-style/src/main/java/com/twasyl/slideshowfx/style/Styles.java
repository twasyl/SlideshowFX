package com.twasyl.slideshowfx.style;

import javafx.scene.Parent;

import java.net.URL;

/**
 * Class for abstracting the styling the SlideshowFX application.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class Styles {

    /**
     * Apply the application style to the given parent.
     *
     * @param parent The parent to apply the style on.
     */
    public static void applyApplicationStyle(final Parent parent) {
        if (parent == null) {
            throw new NullPointerException("The parent to apply the application style is null");
        }

        parent.getStylesheets().add(getApplicationStyle().toExternalForm());
    }

    /**
     * Return the {@link URL} of the application stylesheet. This stylesheet can then be applied to any other
     * {@link Parent UI element}.
     *
     * @return The URL corresponding to the application stylesheet.
     */
    public static URL getApplicationStyle() {
        return Styles.class.getResource("/com/twasyl/slideshowfx/style/css/application.css");
    }

    /**
     * Get the CSS stylesheet for the {@code empty-webview.html} file.
     *
     * @return The URL corresponding to the empty webview stylesheet.
     */
    public static URL getEmptyWebViewStyle() {
        return Styles.class.getResource("/com/twasyl/slideshowfx/style/css/empty-webview.css");
    }
}

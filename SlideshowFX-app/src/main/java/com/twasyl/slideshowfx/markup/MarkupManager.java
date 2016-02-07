package com.twasyl.slideshowfx.markup;

import com.twasyl.slideshowfx.osgi.OSGiManager;

import java.util.List;
import java.util.Optional;

/**
 * This class allows operations on supported markup syntax. It also has helper methods accessing {@link com.twasyl.slideshowfx.osgi.OSGiManager}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class MarkupManager {

    public static List<IMarkup> getInstalledMarkupSyntax() {
        return OSGiManager.getInstalledServices(IMarkup.class);
    }

    /**
     * Test if the given {@code contentCode} is supported.
     * @param contentCode The code of the {@link com.twasyl.slideshowfx.markup.IMarkup} to test if it is supported.
     * @return {@code true} if there is an OSGi bundle having the given code, {@code false} otherwise.
     */
    public static boolean isContentSupported(final String contentCode) {
        boolean supported = false;

        List<IMarkup> services = MarkupManager.getInstalledMarkupSyntax();

        if(services != null) {
            Optional<IMarkup> iMarkup =  services.stream()
                                                  .filter(service -> contentCode.equals(service.getCode()))
                                                  .findFirst();

            supported = iMarkup.isPresent();
        }

        return supported;
    }
}

package com.twasyl.slideshowfx.io;

import javafx.stage.FileChooser;

/**
 * This interface provides {@link javafx.stage.FileChooser.ExtensionFilter}s to be used in the SlideshowFX app.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public interface SlideshowFXExtensionFilter {

    public static FileChooser.ExtensionFilter TEMPLATE_FILTER = new FileChooser.ExtensionFilter("TemplateConfiguration files", "*.sfxt");

    public static FileChooser.ExtensionFilter PRESENTATION_FILES = new FileChooser.ExtensionFilter("Presentation files", "*.sfx");

    public static FileChooser.ExtensionFilter PLUGIN_FILES = new FileChooser.ExtensionFilter("Plugin files", "*.jar");
}

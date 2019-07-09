package com.twasyl.slideshowfx.io;

import com.twasyl.slideshowfx.plugin.manager.internal.PluginFile;
import javafx.stage.FileChooser;

/**
 * This interface provides {@link javafx.stage.FileChooser.ExtensionFilter}s to be used in the SlideshowFX app.
 *
 * @author Thierry Wasylczenko
 * @version 1.1-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public interface SlideshowFXExtensionFilter {

    FileChooser.ExtensionFilter TEMPLATE_FILTER = new FileChooser.ExtensionFilter("TemplateConfiguration files", "*.sfxt");

    FileChooser.ExtensionFilter PRESENTATION_FILES = new FileChooser.ExtensionFilter("Presentation files", "*.sfx");

    FileChooser.ExtensionFilter PLUGIN_FILES = new FileChooser.ExtensionFilter("Plugin files", "*" + PluginFile.EXTENSION);
}

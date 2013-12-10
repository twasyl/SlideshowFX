package com.twasyl.slideshowfx.io;

import javafx.stage.FileChooser;

public interface SlideshowFXExtensionFilter {

    public static FileChooser.ExtensionFilter TEMPLATE_FILTER = new FileChooser.ExtensionFilter("Template files", "*.sfxt");

    public static FileChooser.ExtensionFilter PRESENTATION_FILES = new FileChooser.ExtensionFilter("Presentation files", "*.sfx");
}

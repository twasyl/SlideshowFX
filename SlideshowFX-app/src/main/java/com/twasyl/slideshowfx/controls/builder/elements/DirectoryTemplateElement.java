package com.twasyl.slideshowfx.controls.builder.elements;

import javafx.stage.DirectoryChooser;

/**
 * The DirectoryTemplateElement allows to choose a directory as value.
 * It extends {@link com.twasyl.slideshowfx.controls.builder.elements.FileTemplateElement}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class DirectoryTemplateElement extends FileTemplateElement {

    public DirectoryTemplateElement(String name) {
        super(name);

        this.browseButton.setOnAction(event -> {
            final DirectoryChooser chooser = new DirectoryChooser();
            if(this.getWorkingPath() != null) {
                chooser.setInitialDirectory(this.getWorkingPath().toFile());
            }

            this.validateChosenFile(chooser.showDialog(null));
        });
    }
}

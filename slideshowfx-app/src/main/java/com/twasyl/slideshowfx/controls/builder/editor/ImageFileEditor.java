package com.twasyl.slideshowfx.controls.builder.editor;

import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * This class is an editor that is used to display images.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class ImageFileEditor extends AbstractFileEditor<ImageView> {
    private static final Logger LOGGER = Logger.getLogger(ImageFileEditor.class.getName());

    public ImageFileEditor() {
        super();

        final ImageView view = new ImageView();
        final ScrollPane scrollPane = new ScrollPane();

        this.setFileContent(view);
        this.setEditorScrollPane(scrollPane);
    }

    public ImageFileEditor(File file) {
        this();
        this.setFile(file);
    }

    @Override
    public void updateFileContent() {
        if (getFile() == null) throw new NullPointerException("The fileProperty is null");

        final Image image;
        try {
            image = new Image(this.getFile().toURI().toURL().toExternalForm());
            this.getFileContent().setImage(image);
        } catch (MalformedURLException e) {
            LOGGER.log(WARNING, "Invalid image URL", e);
        }
    }

    /**
     * This method does nothing at the moment because editing images isn't possible in SlideshowFX.
     */
    @Override
    public void saveContent() {
        // An image can not be edited
    }
}

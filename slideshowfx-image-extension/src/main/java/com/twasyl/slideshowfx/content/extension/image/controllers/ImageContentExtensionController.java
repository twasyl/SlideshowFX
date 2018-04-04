package com.twasyl.slideshowfx.content.extension.image.controllers;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtensionController;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the controller for the {@code com.twasyl.slideshowfx.content.extension.images.fxmlImageContentExtension.fxml}.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
public class ImageContentExtensionController extends AbstractContentExtensionController {
    private static final Logger LOGGER = Logger.getLogger(ImageContentExtensionController.class.getName());
    public static final FileChooser.ExtensionFilter IMAGES_FILES = new FileChooser.ExtensionFilter("Image files", "*.png", "*.bmp", "*.jpg", "*.jpeg", "*.gif", "*.svg");
    public static final FileFilter IMAGE_FILTER = new FileFilter() {
        private final String[] extensions = new String[]{".png", ".bmp", ".gif", ".jpg", ".jpeg", ".svg"};

        @Override
        public boolean accept(File pathname) {
            boolean accept = false;

            int index = 0;
            while (!accept && index < extensions.length) {
                accept = pathname.getName().endsWith(extensions[index++]);
            }

            return accept;
        }
    };

    @FXML
    private FlowPane imagesPane;
    @FXML
    private ImageView preview;

    private final ToggleGroup imagesGroup = new ToggleGroup();

    @FXML
    private void chooseNewFile(ActionEvent event) {

        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(IMAGES_FILES);

        File imageFile = chooser.showOpenDialog(null);

        if (imageFile != null) {

            final OSGiManager manager = OSGiManager.getInstance();
            File targetFile = new File((File) manager.getPresentationProperty(OSGiManager.PRESENTATION_RESOURCES_FOLDER), imageFile.getName());

            if (targetFile.exists()) {
                // If the file exists, add a timestamp to the source
                targetFile = new File((File) manager.getPresentationProperty(OSGiManager.PRESENTATION_RESOURCES_FOLDER), System.currentTimeMillis() + imageFile.getName());
            }

            try {
                Files.copy(imageFile.toPath(), targetFile.toPath());

                final ToggleButton newFileButton = this.addFile(targetFile);
                newFileButton.setSelected(true);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not copy the image file", e);
            }
        }
    }

    /**
     * This methods looks for all resources that are images in the resources directory and return the list of files.
     *
     * @return The list of files that are images or an empty list if there is none.
     */
    private List<File> lookupResources() {
        final List<File> images = new ArrayList<>();

        final File resourcesFolder = (File) OSGiManager.getInstance().getPresentationProperty(OSGiManager.PRESENTATION_RESOURCES_FOLDER);
        final File[] files = resourcesFolder.listFiles(IMAGE_FILTER);

        if (files != null) {
            Arrays.stream(files).forEach(images::add);
        }

        return images;
    }

    /**
     * This method adds a file to the list of resources. The file is added as user data to the node displaying the file.
     * The created button also defines a listener on the {@link ToggleButton#selectedProperty()} so
     * that when the button is selected, the image associated to it is displayed in the preview.
     *
     * @param file The file to add.
     * @return The button created for the given file.
     */
    private ToggleButton addFile(File file) {

        Node buttonGraphic = null;

        try (final FileInputStream stream = new FileInputStream(file)) {
            final Image image = new Image(stream, 80, 80, true, true);
            buttonGraphic = new ImageView(image);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can not load the image preview", e);
        }

        final ToggleButton buttonFile = new ToggleButton();

        // If the image hasn't been load, set the text for this button.
        if (buttonGraphic == null) {
            buttonFile.setText(file.getName());
        } else {
            buttonFile.setGraphic(buttonGraphic);
        }

        buttonFile.setTooltip(new Tooltip(file.getName()));
        buttonFile.setPrefSize(100, 100);
        buttonFile.setMinSize(100, 100);
        buttonFile.setMaxSize(100, 100);
        buttonFile.setUserData(file);
        buttonFile.setToggleGroup(this.imagesGroup);
        this.defineContextMenuForImageButton(buttonFile);

        /**
         * Defines the listener for the #selectedProperty.
         */
        buttonFile.selectedProperty().addListener((value, oldValue, newValue) -> {

            if (newValue != null && newValue) {
                try {
                    final Image image = new Image(new FileInputStream((File) buttonFile.getUserData()));

                    ImageContentExtensionController.this.preview.setImage(image);
                } catch (FileNotFoundException e) {
                    LOGGER.log(Level.WARNING, "Can not preview the image", e);
                }
            }
        });


        this.imagesPane.getChildren().add(buttonFile);

        return buttonFile;
    }

    /**
     * Define a {@link ContextMenu} for the given {@link ToggleButton button} in order to interact with images in the
     * resources.
     *
     * @param button The button to set the context menu on.
     */
    private void defineContextMenuForImageButton(final ToggleButton button) {
        final MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(event -> {
            final File image = (File) button.getUserData();

            if (image != null && image.exists()) {
                final Alert confirmation = getAlert(Alert.AlertType.CONFIRMATION, "Delete image",
                        String.format("Are you sure you want to delete the image %1$s?", image.getName()), ButtonType.NO, ButtonType.YES);
                final ButtonType answer = confirmation.showAndWait().orElse(ButtonType.NO);

                if (answer == ButtonType.YES) {

                    if (image != null && image.exists()) {
                        if (image.delete()) {
                            this.imagesPane.getChildren().remove(button);
                        } else {
                            final Alert error = getAlert(Alert.AlertType.ERROR, "Delete image",
                                    String.format("The image %1$s can not be deleted", image.getName()), ButtonType.OK);
                            error.showAndWait();
                        }
                    }
                }
            } else {
                this.imagesPane.getChildren().remove(button);
            }
        });

        final ContextMenu menu = new ContextMenu(delete);
        button.setContextMenu(menu);
    }

    /**
     * Builds an {@link Alert} to be used in order to display information.
     *
     * @param type    The type of the alert to build.
     * @param title   The title of the alert.
     * @param text    The text of the alert.
     * @param buttons The buttons to include in the alert.
     * @return An alert that can be shown in the UI.
     */
    private Alert getAlert(final Alert.AlertType type, final String title, final String text, final ButtonType... buttons) {
        final Alert alert = new Alert(type, text, buttons);
        alert.setGraphic(null);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.getDialogPane().getStylesheets().add("/com/twasyl/slideshowfx/css/Default.css");
        return alert;
    }

    /**
     * Return the file that is selected in this panel or <code>null</code> if none.
     *
     * @return The file that is selected or <code>null</code> if none.
     */
    public File getSelectedFile() {
        File selection = null;

        if (this.imagesGroup.getSelectedToggle() != null) {
            selection = (File) this.imagesGroup.getSelectedToggle().getUserData();
        }

        return selection;
    }

    /**
     * This methods returns the relative URL from the working directory of the presentation for the selected file.
     *
     * @return The relative URL of the selected file or {@code null} if no file is selected.
     */
    public String getSelectedFileUrl() {
        String url = null;

        final File selection = this.getSelectedFile();
        if (selection != null) {
            final File workingDir = (File) OSGiManager.getInstance().getPresentationProperty(OSGiManager.PRESENTATION_FOLDER);

            url = workingDir.toPath().relativize(selection.toPath()).toString().replace(File.separator, "/");
        }

        return url;
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        final ReadOnlyBooleanWrapper property = new ReadOnlyBooleanWrapper();
        property.bind(this.imagesGroup.selectedToggleProperty().isNotNull());

        return property;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<File> images = this.lookupResources();
        images.forEach(image -> this.addFile(image));
    }
}

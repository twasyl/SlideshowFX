package com.twasyl.slideshowfx.content.extension.image.controllers;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtensionController;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.utils.DialogHelper;
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
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.osgi.OSGiManager.PRESENTATION_RESOURCES_FOLDER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.toList;
import static javafx.scene.control.ButtonType.CANCEL;

/**
 * This class is the controller for the {@code com.twasyl.slideshowfx.content.extension.images.fxmlImageContentExtension.fxml}.
 *
 * @author Thierry Wasylczenko
 * @version 1.4
 * @since SlideshowFX 1.0
 */
public class ImageContentExtensionController extends AbstractContentExtensionController {
    private static final Logger LOGGER = Logger.getLogger(ImageContentExtensionController.class.getName());
    private static final FileChooser.ExtensionFilter IMAGES_FILES = new FileChooser.ExtensionFilter("Image files", "*.png", "*.bmp", "*.jpg", "*.jpeg", "*.gif", "*.svg");
    private static final FileFilter IMAGE_FILTER = new FileFilter() {
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
    private static File previouslyChosenImageDir;

    @FXML
    private FlowPane imagesPane;
    @FXML
    private ImageView preview;
    @FXML
    private TextField imageWidth;
    @FXML
    private TextField imageHeight;

    private final ToggleGroup imagesGroup = new ToggleGroup();

    @FXML
    private void chooseNewFile(ActionEvent event) {

        final FileChooser chooser = new FileChooser();

        if (previouslyChosenImageDir != null) {
            chooser.setInitialDirectory(previouslyChosenImageDir);
        }

        chooser.getExtensionFilters().add(IMAGES_FILES);

        File imageFile = chooser.showOpenDialog(null);

        if (imageFile != null) {
            final OSGiManager manager = OSGiManager.getInstance();
            File targetFile = new File((File) manager.getPresentationProperty(PRESENTATION_RESOURCES_FOLDER), imageFile.getName());
            final CopyOption[] copyOptions;

            if (targetFile.exists()) {
                final ButtonType replace = new ButtonType("Replace");
                final ButtonType keepBoth = new ButtonType("Keep both");

                final ButtonType answer = DialogHelper.showDialog("Image already exists",
                        new Label("The image already exists. What would you like to do?"),
                        CANCEL, replace, keepBoth);
                if (CANCEL == answer) {
                    return;
                } else if (replace == answer) {
                    copyOptions = new CopyOption[]{REPLACE_EXISTING};
                } else {
                    copyOptions = new CopyOption[0];
                    // If the file exists, add a timestamp to the source
                    targetFile = new File((File) manager.getPresentationProperty(PRESENTATION_RESOURCES_FOLDER), System.currentTimeMillis() + imageFile.getName());
                }
            } else {
                copyOptions = new CopyOption[0];
            }

            try {
                Files.copy(imageFile.toPath(), targetFile.toPath(), copyOptions);
                previouslyChosenImageDir = imageFile.getParentFile();

                if (Arrays.binarySearch(copyOptions, REPLACE_EXISTING) != -1) {
                    this.cleanButtonsWithFile(targetFile);
                }

                final ToggleButton newFileButton = this.addButtonWithFile(targetFile);
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

        final File resourcesFolder = (File) OSGiManager.getInstance().getPresentationProperty(PRESENTATION_RESOURCES_FOLDER);
        final File[] files = resourcesFolder.listFiles(IMAGE_FILTER);

        if (files != null) {
            images.addAll(Arrays.asList(files));
        }

        return images;
    }

    /**
     * Clean all {@link ToggleButton} having the same file as the given one stored in their
     * {@link ToggleButton#getUserData() user data}. If the given file is {@code null}, nothing is performed.
     *
     * @param file The file to remove the duplicates.
     */
    private void cleanButtonsWithFile(final File file) {
        this.imagesPane.getChildren().removeAll(
                this.imagesPane.getChildren().stream()
                        .filter(node -> file.equals(node.getUserData()))
                        .collect(toList()));
    }

    /**
     * This method adds a file to the list of resources. The file is added as user data to the node displaying the file.
     * The created button also defines a listener on the {@link ToggleButton#selectedProperty()} so
     * that when the button is selected, the image associated to it is displayed in the preview.
     *
     * @param file The file to add.
     * @return The button created for the given file.
     */
    private ToggleButton addButtonWithFile(File file) {

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
                final ButtonType answer = DialogHelper.showConfirmationAlert("Delete image",
                        String.format("Are you sure you want to delete the image %1$s?", image.getName()));

                if (answer == ButtonType.YES) {
                    if (image.delete()) {
                        this.imagesPane.getChildren().remove(button);
                    } else {
                        DialogHelper.showError("Delete image",
                                String.format("The image %1$s can not be deleted", image.getName()));
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

    /**
     * Get the desired image width the user has put in the text field. The returned value may be {@code null}.
     *
     * @return The filled image width.
     */
    public String getImageWidth() {
        return this.imageWidth.getText();
    }

    /**
     * Checks if a value has been entered in the image width field text field.
     *
     * @return {@code true} if a value has been entered and is not empty, {@code false} otherwise.
     */
    public boolean hasImageWidth() {
        return this.getImageWidth() != null && !this.getImageWidth().trim().isEmpty();
    }

    /**
     * Get the desired image height the user has put in the text field. The returned value may be {@code null}.
     *
     * @return The filled image height.
     */
    public String getImageHeight() {
        return this.imageHeight.getText();
    }

    /**
     * Checks if a value has been entered in the image height field text field.
     *
     * @return {@code true} if a value has been entered and is not empty, {@code false} otherwise.
     */
    public boolean hasImageHeight() {
        return this.getImageHeight() != null && !this.getImageHeight().trim().isEmpty();
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
        images.forEach(this::addButtonWithFile);
    }
}

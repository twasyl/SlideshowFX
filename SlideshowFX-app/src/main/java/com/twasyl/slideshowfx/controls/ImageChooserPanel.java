/*
 * Copyright 2014 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.io.SlideshowFXExtensionFilter;
import com.twasyl.slideshowfx.io.SlideshowFXFileFilter;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a panel listing all images already present in the resources of the current presentation in order
 * to insert them again. But it also provides the feature to choose an image on the disk and copy it to the resources
 * of the current presentation.
 * If the user selects an image, a preview is also displayed.
 * If no images are already present in the resources, a dialog asking the user to choose an image is directly displayed.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class ImageChooserPanel extends HBox {
    private static Logger LOGGER = Logger.getLogger(ImageChooserPanel.class.getName());

    private final VBox resourcesPane = new VBox(5);
    private final ToggleGroup resourcesGroup = new ToggleGroup();
    private final ImageView preview = new ImageView();

    public ImageChooserPanel(final PresentationEngine engine) {
        super(5);
        this.setPadding(new Insets(5, 5, 5, 5));

        final Button browseButton = new Button("Add a new image to the resources");
        browseButton.setPrefWidth(300);
        browseButton.setOnAction(event ->  ImageChooserPanel.this.chooseNewFile(engine) );

        final Text currentFiles = new Text("Already existing images:");
        currentFiles.getStyleClass().add("text");

        this.resourcesPane.getChildren().addAll(currentFiles, new Separator(Orientation.HORIZONTAL), browseButton);

        final List<File> existingImages = this.lookupResources(engine);

        /**
         * If no resources are already present, a dialog for choosing one is opened directly when the panel is visible.
         */
        if(existingImages.isEmpty()) {
            this.parentProperty().addListener((value, oldParent, newParent) -> {
                if(newParent != null) {
                    ImageChooserPanel.this.chooseNewFile(engine);
                }
            });
        }

        for(File image : existingImages) {
            this.addFile(image);
        }

        final ScrollPane resourcesScrollPane = new ScrollPane(this.resourcesPane);
        resourcesScrollPane.setPrefWidth(320);
        resourcesScrollPane.setPrefViewportWidth(320);

        final ScrollPane previewScrollPane = new ScrollPane(this.preview);
        previewScrollPane.setPrefSize(500, 500);

        this.getChildren().addAll(resourcesScrollPane, previewScrollPane);
    }

    /**
     * This method adds a file to the list of resources. The file is added as user data to the node displaying the file.
     * The created button also defines a listener on the {@link javafx.scene.control.ToggleButton#selectedProperty()} so
     * that when the button is selected, the image associated to it is displayed in the preview.
     * @param file The file to add.
     * @return The button created for the given file.
     */
    private ToggleButton addFile(File file) {

        final ToggleButton buttonFile = new ToggleButton(file.getName());
        buttonFile.setTooltip(new Tooltip(file.getName()));
        buttonFile.setPrefWidth(300);
        buttonFile.setMinWidth(300);
        buttonFile.setMaxWidth(300);
        buttonFile.setUserData(file);
        buttonFile.setToggleGroup(this.resourcesGroup);

        /**
         * Defines the listener for the #selectedProperty.
         */
        buttonFile.selectedProperty().addListener((value, oldValue, newValue) -> {

            if(newValue != null && newValue) {
                try {
                    final Image image = new Image(new FileInputStream((File) buttonFile.getUserData()));

                    ImageChooserPanel.this.preview.setImage(image);
                } catch (FileNotFoundException e) {
                    LOGGER.log(Level.WARNING, "Can not preview the image", e);
                }
            }
        });


        this.resourcesPane.getChildren().add(this.resourcesPane.getChildren().size() - 2, buttonFile);

        return buttonFile;
    }

    /**
     * Opens a file chooser for chossing a new image that will be added to the list of resources and copied into the
     * directory of resources.
     * @param engine The presentation engine that will be used to determine the directory for the resources.
     */
    private void chooseNewFile(PresentationEngine engine) {
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.IMAGES_FILES);

        File imageFile = chooser.showOpenDialog(null);

        if (imageFile != null) {
            File targetFile = new File(engine.getTemplateConfiguration().getResourcesDirectory(), imageFile.getName());

            if (targetFile.exists()) {
                // If the file exists, add a timestamp to the source
                targetFile = new File(engine.getTemplateConfiguration().getResourcesDirectory(), System.currentTimeMillis() + imageFile.getName());
            }

            try {
                Files.copy(imageFile.toPath(), targetFile.toPath());

                final ToggleButton newFileButton = ImageChooserPanel.this.addFile(targetFile);
                newFileButton.setSelected(true);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not copy the image file", e);
            }
        }
    }

    /**
     * This methods looks for all resources that are images in the resources directory and return the list of files.
     * @param engine The engine used to determine the resources directory.
     * @return The list of files that are images or an empty list if there is none.
     */
    private List<File> lookupResources(PresentationEngine engine) {
        final List<File> images = new ArrayList<>();

        for(File img : engine.getTemplateConfiguration().getResourcesDirectory().listFiles(SlideshowFXFileFilter.IMAGE_FILTER)) {
            images.add(img);

        }

        return images;
    }

    /**
     * Return the file that is selected in this panel or <code>null</code> if none.
     * @return The file that is selected or <code>null</code> if none.
     */
    public File getSelectedFile() {
        File selection = null;

        if(this.resourcesGroup.getSelectedToggle() != null) {
            selection = (File) this.resourcesGroup.getSelectedToggle().getUserData();
        }

        return selection;
    }
}

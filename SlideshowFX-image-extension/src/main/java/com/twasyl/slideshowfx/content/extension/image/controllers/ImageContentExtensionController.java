/*
 * Copyright 2015 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.content.extension.image.controllers;

import com.twasyl.slideshowfx.osgi.OSGiManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the controller for the {@code com.twasyl.slideshowfx.content.extension.images.fxmlImageContentExtension.fxml}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class ImageContentExtensionController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ImageContentExtensionController.class.getName());
    public static FileChooser.ExtensionFilter IMAGES_FILES = new FileChooser.ExtensionFilter("Image files", "*.png", "*.bmp", "*.jpg", "*.jpeg", "*.gif");
    public static final FileFilter IMAGE_FILTER = new FileFilter() {
        private final String[] extensions = new String[] { ".png", ".bmp", ".gif", ".jpg", ".jpeg" };

        @Override
        public boolean accept(File pathname) {
            boolean accept = false;

            int index = 0;
            while(!accept && index < extensions.length) {
                accept = pathname.getName().endsWith(extensions[index++]);
            }

            return accept;
        }
    };

    @FXML private FlowPane imagesPane;
    @FXML private ImageView preview;

    private final ToggleGroup imagesGroup = new ToggleGroup();

    @FXML
    private void chooseNewFile(ActionEvent event) {

        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(IMAGES_FILES);

        File imageFile = chooser.showOpenDialog(null);

        if (imageFile != null) {

            File targetFile = new File((File) OSGiManager.getPresentationProperty(OSGiManager.PRESENTATION_RESOURCES_FOLDER), imageFile.getName());

            if (targetFile.exists()) {
                // If the file exists, add a timestamp to the source
                targetFile = new File((File) OSGiManager.getPresentationProperty(OSGiManager.PRESENTATION_RESOURCES_FOLDER), System.currentTimeMillis() + imageFile.getName());
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
     * @return The list of files that are images or an empty list if there is none.
     */
    private List<File> lookupResources() {
        final List<File> images = new ArrayList<>();

        for(File img : ((File) OSGiManager.getPresentationProperty(OSGiManager.PRESENTATION_RESOURCES_FOLDER)).listFiles(IMAGE_FILTER)) {
            images.add(img);

        }

        return images;
    }

    /**
     * This method adds a file to the list of resources. The file is added as user data to the node displaying the file.
     * The created button also defines a listener on the {@link javafx.scene.control.ToggleButton#selectedProperty()} so
     * that when the button is selected, the image associated to it is displayed in the preview.
     * @param file The file to add.
     * @return The button created for the given file.
     */
    private ToggleButton addFile(File file) {

        Node buttonGraphic = null;

        try(final FileInputStream stream = new FileInputStream(file)) {
            final Image image = new Image(stream, 80, 80, true, true);
            buttonGraphic = new ImageView(image);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can not load the image preview", e);
        }

        final ToggleButton buttonFile = new ToggleButton();

        // If the image hasn't been load, set the text for this button.
        if(buttonGraphic == null) {
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

        /**
         * Defines the listener for the #selectedProperty.
         */
        buttonFile.selectedProperty().addListener((value, oldValue, newValue) -> {

            if(newValue != null && newValue) {
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
     * Return the file that is selected in this panel or <code>null</code> if none.
     * @return The file that is selected or <code>null</code> if none.
     */
    public File getSelectedFile() {
        File selection = null;

        if(this.imagesGroup.getSelectedToggle() != null) {
            selection = (File) this.imagesGroup.getSelectedToggle().getUserData();
        }

        return selection;
    }

    /**
     * This methods returns the relative URL from the working directory of the presentation for the selected file.
     * @return The relative URL of the selected file or {@code null} if no file is selected.
     */
    public String getSelectedFileUrl() {
        String url = null;

        final File selection = this.getSelectedFile();
        if(selection != null) {
            final File workingDir = (File) OSGiManager.getPresentationProperty(OSGiManager.PRESENTATION_FOLDER);

            url = workingDir.toPath().relativize(selection.toPath()).toString().replace(File.separator, "/");
        }

        return url;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<File> images = this.lookupResources();
        images.forEach(image -> this.addFile(image));
    }
}

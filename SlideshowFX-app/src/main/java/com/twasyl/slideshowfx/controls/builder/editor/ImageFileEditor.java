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

package com.twasyl.slideshowfx.controls.builder.editor;

import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.MalformedURLException;

/**
 * This class is an editor that is used to display images.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class ImageFileEditor extends AbstractFileEditor<ImageView> {

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
        if(getFile() == null) throw new NullPointerException("The fileProperty is null");

        final Image image;
        try {
            image = new Image(this.getFile().toURI().toURL().toExternalForm());
            this.getFileContent().setImage(image);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method does nothing at the moment because editing images isn't possible in SlideshowFX.
     */
    @Override
    public void saveContent() {
    }
}

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

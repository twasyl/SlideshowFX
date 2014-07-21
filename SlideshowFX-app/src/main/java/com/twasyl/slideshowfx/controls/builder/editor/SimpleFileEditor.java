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

import javafx.scene.control.TextArea;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thierry Wasylczenko
 */
public class SimpleFileEditor extends AbstractFileEditor<TextArea> {

    private static final Logger LOGGER = Logger.getLogger(SimpleFileEditor.class.getName());

    public SimpleFileEditor() {
        super();

        final TextArea area = new TextArea();
        area.setWrapText(true);

        this.setFileContent(area);
    }

    public SimpleFileEditor(File file) {
        this();
        this.setFile(file);
    }

    @Override
    public void updateFileContent() {
        if(getFile() == null) throw new NullPointerException("The fileProperty is null");

        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(getFile())))) {
            final StringBuilder builder = new StringBuilder();

            reader.lines().forEach(line -> builder.append(line));

            getFileContent().setText(builder.toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not save the content", e);
        }
    }

    @Override
    public void saveContent() {
        if(getFile() == null) throw new NullPointerException("The fileProperty is null");

        try(final FileWriter writer = new FileWriter(getFile())) {
            writer.write(this.getFileContent().getText());
            writer.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not save the content", e);
        }
    }
}

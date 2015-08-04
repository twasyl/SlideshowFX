/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.utils.beans.binding;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;

import java.io.File;

/**
 * Creates a {@link javafx.beans.binding.StringBinding} that always reflects the filename of an {@link javafx.beans.property.ObjectProperty}
 * containing a file.
 * If the file is {@code null} the String "Untitled" is returned by this binding.
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class FilenameBinding extends StringBinding {

    private ObjectProperty<File> file;

    /**
     * Construct a filename binding.
     * @param file The property to bind to and create the binding for.
     * @throws java.lang.NullPointerException If the given {@code file} is null.
     */
    public FilenameBinding(ObjectProperty<File> file) {
        if(file == null) throw new NullPointerException("The property can not be null");

        this.file = file;
        super.bind(this.file);
    }

    @Override
    protected String computeValue() {
        return this.file.get() == null ? "Untitled" : this.file.get().getName();
    }

    @Override
    public void dispose() {
        super.unbind(file);
    }
}

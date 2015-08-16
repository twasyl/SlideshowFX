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

package com.twasyl.slideshowfx.utils.beans.converter;

import javafx.util.StringConverter;

import java.io.File;

/**
 * Creates a {@link StringConverter} that always reflects the path of a {@link File}.
 * If the file is {@code null} an empty String is returned by this binding.
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class FileStringConverter extends StringConverter<File> {


    @Override
    public String toString(File object) {
        String result = "";

        if(object != null) result = object.getAbsolutePath().replaceAll("\\\\", "/");

        return result;
    }

    @Override
    public File fromString(String string) {
        File result = null;

        if(string != null && !string.trim().isEmpty()) {
            result = new File(string.trim().replaceAll("\\\\", "/"));
        }

        return result;
    }
}

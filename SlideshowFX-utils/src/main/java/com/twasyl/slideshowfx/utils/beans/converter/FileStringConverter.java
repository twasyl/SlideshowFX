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

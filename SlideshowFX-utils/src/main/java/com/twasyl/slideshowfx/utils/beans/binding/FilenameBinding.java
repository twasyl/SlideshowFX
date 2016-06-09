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
 * @since SlideshowFX 1.0
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

package com.twasyl.slideshowfx.snippet.executor.golo;

import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutorOptions;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Options that are necessary for the Java snippet executor.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since 1.0.0 SlideshowFX 1.0.0
 */
public class GoloSnippetExecutorOptions implements ISnippetExecutorOptions {
    private final ObjectProperty<File> goloHome = new SimpleObjectProperty<>();

    public ObjectProperty<File> goloHomeProperty() { return this.goloHome; }

    public File getGoloHome() { return this.goloHome.get(); }

    public void setGoloHome(File goloHome) throws FileNotFoundException {
        if(goloHome == null) throw new NullPointerException("The goloHome can not be null");
        if(!goloHome.exists()) throw new FileNotFoundException("The goloHome doesn't exist");
        if(!goloHome.isDirectory()) throw new IllegalArgumentException("The goloHome is not a directory");

        this.goloHome.setValue(goloHome);
    }
}

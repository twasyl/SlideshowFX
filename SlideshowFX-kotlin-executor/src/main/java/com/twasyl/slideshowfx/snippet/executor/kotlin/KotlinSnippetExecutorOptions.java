package com.twasyl.slideshowfx.snippet.executor.kotlin;

import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutorOptions;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Options that are necessary for the Kotlin snippet executor.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since 1.0.0
 */
public class KotlinSnippetExecutorOptions implements ISnippetExecutorOptions {
    private final ObjectProperty<File> kotlinHome = new SimpleObjectProperty<>();

    public ObjectProperty<File> kotlinHomeProperty() { return this.kotlinHome; }

    public File getKotlinHome() { return this.kotlinHome.get(); }

    public void setKotlinHome(File kotlinHome) throws FileNotFoundException {
        if(kotlinHome == null) throw new NullPointerException("The kotlinHome can not be null");
        if(!kotlinHome.exists()) throw new FileNotFoundException("The kotlinHome doesn't exist");
        if(!kotlinHome.isDirectory()) throw new IllegalArgumentException("The kotlinHome is not a directory");

        this.kotlinHome.setValue(kotlinHome);
    }
}

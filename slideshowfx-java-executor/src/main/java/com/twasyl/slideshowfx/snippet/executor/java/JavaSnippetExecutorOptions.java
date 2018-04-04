package com.twasyl.slideshowfx.snippet.executor.java;

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
 * @since 1.0.0
 */
public class JavaSnippetExecutorOptions implements ISnippetExecutorOptions {
    private final ObjectProperty<File> javaHome = new SimpleObjectProperty<>();

    public ObjectProperty<File> javaHomeProperty() { return this.javaHome; }

    public File getJavaHome() { return this.javaHome.get(); }

    public void setJavaHome(File javaHome) throws FileNotFoundException {
        if(javaHome == null) throw new NullPointerException("The javaHome can not be null");
        if(!javaHome.exists()) throw new FileNotFoundException("The javaHome doesn't exist");
        if(!javaHome.isDirectory()) throw new IllegalArgumentException("The javaHome is not a directory");

        this.javaHome.setValue(javaHome);
    }
}

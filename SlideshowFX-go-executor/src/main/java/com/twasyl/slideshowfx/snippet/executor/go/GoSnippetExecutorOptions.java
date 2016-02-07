package com.twasyl.slideshowfx.snippet.executor.go;

import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutorOptions;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Options that are necessary for the Go snippet executor.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since 1.0.0
 */
public class GoSnippetExecutorOptions implements ISnippetExecutorOptions {
    private final ObjectProperty<File> goHome = new SimpleObjectProperty<>();

    public ObjectProperty<File> goHomeProperty() { return this.goHome; }

    public File getGoHome() { return this.goHome.get(); }

    public void setGoHome(File goHome) throws FileNotFoundException {
        if(goHome == null) throw new NullPointerException("The goHome can not be null");
        if(!goHome.exists()) throw new FileNotFoundException("The goHome doesn't exist");
        if(!goHome.isDirectory()) throw new IllegalArgumentException("The goHome is not a directory");

        this.goHome.setValue(goHome);
    }
}

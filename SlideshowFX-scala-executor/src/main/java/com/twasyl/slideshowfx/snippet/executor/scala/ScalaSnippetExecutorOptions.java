package com.twasyl.slideshowfx.snippet.executor.scala;

import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutorOptions;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Options that are necessary for the Scala snippet executor.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since 1.0.0
 */
public class ScalaSnippetExecutorOptions implements ISnippetExecutorOptions {
    private final ObjectProperty<File> scalaHome = new SimpleObjectProperty<>();

    public ObjectProperty<File> scalaHomeProperty() { return this.scalaHome; }

    public File getScalaHome() { return this.scalaHome.get(); }

    public void setScalaHome(File scalaHome) throws FileNotFoundException {
        if(scalaHome == null) throw new NullPointerException("The scalaHome can not be null");
        if(!scalaHome.exists()) throw new FileNotFoundException("The scalaHome doesn't exist");
        if(!scalaHome.isDirectory()) throw new IllegalArgumentException("The scalaHome is not a directory");

        this.scalaHome.setValue(scalaHome);
    }
}

package com.twasyl.slideshowfx.snippet.executor.rust;

import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutorOptions;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Options that are necessary for the Rust snippet executor.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since @@NEXT-VERSION@@
 */
public class RustSnippetExecutorOptions implements ISnippetExecutorOptions {
    private final ObjectProperty<File> cargoHome = new SimpleObjectProperty<>();

    public ObjectProperty<File> cargoHomeProperty() { return this.cargoHome; }

    public File getCargoHome() { return this.cargoHome.get(); }

    public void setCargoHome(File cargoHome) throws FileNotFoundException {
        if(cargoHome == null) throw new NullPointerException("The cargoHome can not be null");
        if(!cargoHome.exists()) throw new FileNotFoundException("The cargoHome doesn't exist");
        if(!cargoHome.isDirectory()) throw new IllegalArgumentException("The cargoHome is not a directory");

        this.cargoHome.setValue(cargoHome);
    }
}

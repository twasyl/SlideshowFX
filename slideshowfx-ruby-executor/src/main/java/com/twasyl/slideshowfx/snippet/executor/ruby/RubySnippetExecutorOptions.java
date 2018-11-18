package com.twasyl.slideshowfx.snippet.executor.ruby;

import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutorOptions;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Options that are necessary for the Ruby snippet executor.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since @@NEXT-VERSION@@
 */
public class RubySnippetExecutorOptions implements ISnippetExecutorOptions {
    private final ObjectProperty<File> rubyHome = new SimpleObjectProperty<>();
    private final BooleanProperty useRubyInPath = new SimpleBooleanProperty(false);

    public ObjectProperty<File> rubyHomeProperty() { return this.rubyHome; }

    public File getRubyHome() { return this.rubyHome.get(); }

    public void setRubyHome(File rubyHome) throws FileNotFoundException {
        if(rubyHome == null) throw new NullPointerException("The rubyHome can not be null");
        if(!rubyHome.exists()) throw new FileNotFoundException("The rubyHome doesn't exist");
        if(!rubyHome.isDirectory()) throw new IllegalArgumentException("The rubyHome is not a directory");

        this.rubyHome.setValue(rubyHome);
    }

    public BooleanProperty useRubyInPathProperty() {
        return useRubyInPath;
    }

    public boolean getUseRubyInPath() {
        return useRubyInPath.get();
    }

    public void setUseRubyInPath(boolean useRubyInPath) {
        this.useRubyInPath.set(useRubyInPath);
    }
}

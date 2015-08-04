/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

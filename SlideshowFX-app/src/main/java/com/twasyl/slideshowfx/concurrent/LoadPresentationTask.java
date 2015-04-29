/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.concurrent;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This tasks loads a SlideshowFX presentation. It takes a {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine}
 * that will host the loaded template.
 * In order to load a presentation, the {@link java.io.File} corresponding to the presentation to load must be passed
 * to each instance of this task.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class LoadPresentationTask extends Task<PresentationEngine> {
    private static final Logger LOGGER = Logger.getLogger(LoadPresentationTask.class.getName());
    private File dataFile;

    public LoadPresentationTask(File dataFile) {
        ((SimpleStringProperty) this.titleProperty()).set(String.format("Loading presentation: %1$s", dataFile.getName()));
        this.dataFile = dataFile;
    }

    @Override
    protected PresentationEngine call() throws Exception {
        PresentationEngine engine = null;

        // Ensure the presentation has already been saved
        if(this.dataFile != null && this.dataFile.exists()) {
            engine = new PresentationEngine();
            try {
                engine.loadArchive(this.dataFile);
            } catch(Exception e) {
                engine = null;
                this.setException(e);
                this.failed();
            }
        } else this.failed();

        return engine;
    }

    @Override
    protected void scheduled() {
        super.scheduled();
        this.updateMessage("Opening presentation");
        this.updateProgress(-1, 0);
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        this.updateMessage("Presentation loaded");
        this.updateProgress(0, 0);
    }

    @Override
    protected void running() {
        super.running();
        this.updateMessage("Loading presentation");
        this.updateProgress(-1, 0);
    }

    @Override
    protected void failed() {
        super.failed();
        this.updateMessage("Error while loading the presentation");
        this.updateProgress(0, 0);
        LOGGER.log(Level.SEVERE, "Can not load the presentation", this.getException());
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        this.updateMessage("Cancelled presentation loading");
        this.updateProgress(0, 0);
    }
}

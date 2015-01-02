/*
 * Copyright 2014 Thierry Wasylczenko
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
import javafx.concurrent.Task;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This tasks loads a SlideshowFX template. It takes a {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine}
 * that will host the loaded template.
 * In order to load a template, the {@link java.io.File} corresponding to the template to load must be passed
 * to each instance of this task.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class LoadTemplateTask extends Task<Void> {

    private static final Logger LOGGER = Logger.getLogger(LoadTemplateTask.class.getName());

    private PresentationEngine engine;
    private File dataFile;

    public LoadTemplateTask(PresentationEngine engine, File dataFile) {
        this.engine = engine;
        this.dataFile = dataFile;
    }

    @Override
    protected Void call() throws Exception {

        if(this.engine != null && this.dataFile != null) {
            if(this.dataFile.exists()) this.engine.createFromTemplate(this.dataFile);
            else this.failed();
        }

        return null;
    }

    @Override
    protected void scheduled() {
        super.scheduled();
        this.updateMessage("Opening template");
        this.updateProgress(-1, 0);
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        this.updateMessage("Template loaded");
        this.updateProgress(0, 0);
    }

    @Override
    protected void running() {
        super.running();
        this.updateMessage("Loading template");
        this.updateProgress(-1, 0);
    }

    @Override
    protected void failed() {
        super.failed();
        this.updateMessage("Error while loading the template");
        this.updateProgress(0, 0);
        LOGGER.log(Level.SEVERE, "Can not load the template", this.getException());
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        this.updateMessage("Cancelled template loading");
        this.updateProgress(0, 0);
    }
}

/*
 * Copyright 2016 Thierry Wasylczenko
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
import com.twasyl.slideshowfx.utils.concurrent.SlideshowFXTask;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This tasks saves a SlideshowFX presentation. It takes a {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine}
 * that hosts the presentation to save. {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#saveArchive()}
 * is called in order to save the presentation. If {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#getArchive()}
 * returns {@code null} or if {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine} is {@code null}, the
 * task is considered as failed and {@link Task#failed} is called.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class SavePresentationTask extends SlideshowFXTask<Void> {
    private static final Logger LOGGER = Logger.getLogger(SavePresentationTask.class.getName());

    private final PresentationEngine presentation;

    public SavePresentationTask(final PresentationEngine presentation) {
        this.presentation = presentation;

        if(this.presentation.getArchive() != null) {
            ((SimpleStringProperty) this.titleProperty()).set(String.format("Saving presentation: %1$s", this.presentation.getArchive().getName()));
        }
    }

    @Override
    protected Void call() throws Exception {
        // Ensure the presentation has already been saved
        if(this.presentation == null) throw new NullPointerException("The presentation is null");
        if(this.presentation.getArchive() == null) throw new NullPointerException("The presentation archive is null");

        this.presentation.saveArchive();
        this.succeeded();

        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        this.updateMessage("Presentation saved");
        this.updateProgress(0, 0);
    }

    @Override
    protected void running() {
        super.running();
        this.updateMessage("Saving presentation");
        this.updateProgress(-1, 0);
    }

    @Override
    protected void failed() {
        super.failed();
        this.updateMessage("Error while saving the presentation");
        this.updateProgress(0, 0);
        LOGGER.log(Level.SEVERE, "Can not save the presentation", this.getException());
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        this.updateMessage("Cancelled presentation saving");
        this.updateProgress(0, 0);
    }
}

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

import com.twasyl.slideshowfx.controllers.PresentationViewController;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This tasks reloads the presentation view. If the {@link #presentationView} is null, the task is considered as failed
 * and {@link javafx.concurrent.Task#failed()} is called.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class ReloadPresentationViewTask extends Task<Void> {
    private static final Logger LOGGER = Logger.getLogger(ReloadPresentationViewTask.class.getName());

    private final PresentationViewController presentationView;

    public ReloadPresentationViewTask(final PresentationViewController presentationView) {
        ((SimpleStringProperty) this.titleProperty()).set("Reloading the presentation");
        this.presentationView = presentationView;
    }

    @Override
    protected Void call() throws Exception {
        if(this.presentationView == null) throw new NullPointerException("The presentation view is null");

        PlatformHelper.run(() -> this.presentationView.reloadPresentationBrowser());

        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        this.updateProgress(0, 0);
        this.updateMessage("Presentation view reloaded");
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        this.updateProgress(0, 0);
        this.updateMessage("Presentation view reload cancelled");
    }

    @Override
    protected void failed() {
        super.failed();
        this.updateProgress(0, 0);
        this.updateMessage("Presentation view reload failed");
        LOGGER.log(Level.SEVERE, "Can not reload presentation view", this.getException());
    }

    @Override
    protected void running() {
        super.running();
        this.updateProgress(-1, 0);
        this.updateMessage("Presentation view reloading");
    }
}
package com.twasyl.slideshowfx.concurrent;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
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
 * @since 1.0
 */
public class LoadPresentationTask extends Task<Void> {
    private static final Logger LOGGER = Logger.getLogger(LoadPresentationTask.class.getName());

    private PresentationEngine engine;
    private File dataFile;

    public LoadPresentationTask(PresentationEngine engine, File dataFile) {
        this.engine = engine;
        this.dataFile = dataFile;
    }

    @Override
    protected Void call() throws Exception {

        // Ensure the presentation has already been saved
        if(this.engine != null && this.dataFile != null) {
            if(this.dataFile.exists()) this.engine.loadArchive(this.dataFile);
            else this.failed();
        }

        return null;
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

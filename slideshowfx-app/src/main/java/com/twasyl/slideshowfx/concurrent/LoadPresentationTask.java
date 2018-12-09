package com.twasyl.slideshowfx.concurrent;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.global.configuration.RecentPresentation;
import com.twasyl.slideshowfx.utils.concurrent.SlideshowFXTask;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This tasks loads a SlideshowFX presentation. It takes a {@link PresentationEngine}
 * that will host the loaded template.
 * In order to load a presentation, the {@link File} corresponding to the presentation to load must be passed
 * to each instance of this task.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class LoadPresentationTask extends SlideshowFXTask<PresentationEngine> {
    private static final Logger LOGGER = Logger.getLogger(LoadPresentationTask.class.getName());
    private File dataFile;

    public LoadPresentationTask(File dataFile) {
        ((SimpleStringProperty) this.titleProperty()).set(String.format("Loading presentation: %1$s", dataFile.getName()));
        this.dataFile = dataFile;
    }

    @Override
    protected PresentationEngine call() throws Exception {
        if (this.dataFile == null) throw new NullPointerException("The data file is null");
        if (!this.dataFile.exists()) throw new FileNotFoundException("The data file doesn't exist");

        final PresentationEngine engine = new PresentationEngine();
        engine.loadArchive(this.dataFile);
        engine.setModifiedSinceLatestSave(false);

        final RecentPresentation presentation = new RecentPresentation(this.dataFile.getAbsolutePath(), LocalDateTime.now());
        GlobalConfiguration.saveRecentPresentation(presentation);

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
        LOGGER.info("Presentation loaded: " + this.dataFile.getAbsolutePath());
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
        LOGGER.info("Presentation loading cancelled: " + this.dataFile.getAbsolutePath());
    }
}

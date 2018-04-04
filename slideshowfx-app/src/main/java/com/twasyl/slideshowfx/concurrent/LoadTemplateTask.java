package com.twasyl.slideshowfx.concurrent;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.utils.concurrent.SlideshowFXTask;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.io.FileNotFoundException;
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
 * @since SlideshowFX 1.0
 */
public class LoadTemplateTask extends SlideshowFXTask<PresentationEngine> {

    private static final Logger LOGGER = Logger.getLogger(LoadTemplateTask.class.getName());
    private File dataFile;

    public LoadTemplateTask(File dataFile) {
        ((SimpleStringProperty) this.titleProperty()).set(String.format("Loading template: %1$s", dataFile.getName()));
        this.dataFile = dataFile;
    }

    @Override
    protected PresentationEngine call() throws Exception {
        if(this.dataFile == null) throw new NullPointerException("The data file is null");
        if(!this.dataFile.exists()) throw new FileNotFoundException("The data file doesn't exist");

        final PresentationEngine engine = new PresentationEngine();
        engine.createFromTemplate(this.dataFile);

        return engine;
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

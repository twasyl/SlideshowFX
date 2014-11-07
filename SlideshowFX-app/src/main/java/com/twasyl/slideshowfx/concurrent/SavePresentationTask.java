package com.twasyl.slideshowfx.concurrent;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import javafx.concurrent.Task;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This tasks saves a SlideshowFX presentation. It takes a {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine}
 * that hosts the presentation to save. {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#saveArchive()}
 * is called in order to save the presentation. If {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#getArchive()}
 * returns {@code null} or if {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine} is {@code null}, the
 * task is considered as failed and {@linf Task#failed} is called.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class SavePresentationTask extends Task<Void> {
    private static final Logger LOGGER = Logger.getLogger(SavePresentationTask.class.getName());

    private PresentationEngine engine;

    public SavePresentationTask(PresentationEngine engine) {
        this.engine = engine;
    }

    @Override
    protected Void call() throws Exception {

        // Ensure the presentation has already been saved
        if(this.engine != null && this.engine.getArchive() != null) {
            this.engine.saveArchive();
            this.succeeded();
        } else this.failed();

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

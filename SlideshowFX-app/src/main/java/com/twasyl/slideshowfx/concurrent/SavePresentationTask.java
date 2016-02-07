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

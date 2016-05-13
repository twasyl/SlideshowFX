package com.twasyl.slideshowfx.concurrent;

import com.twasyl.slideshowfx.controllers.PresentationViewController;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import com.twasyl.slideshowfx.utils.concurrent.SlideshowFXTask;
import javafx.beans.property.SimpleStringProperty;

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
public class ReloadPresentationViewTask extends SlideshowFXTask<Void> {
    private static final Logger LOGGER = Logger.getLogger(ReloadPresentationViewTask.class.getName());

    protected Runnable action;
    private final PresentationViewController presentationView;

    public ReloadPresentationViewTask(final PresentationViewController presentationView) {
        this(presentationView, () -> presentationView.reloadPresentationBrowser());
    }

    protected ReloadPresentationViewTask(final PresentationViewController presentationView, final Runnable action) {
        ((SimpleStringProperty) this.titleProperty()).set("Reloading the presentation");
        this.presentationView = presentationView;
        this.action = action;
    }

    @Override
    protected Void call() throws Exception {
        if(this.presentationView == null) throw new NullPointerException("The presentation view is null");

        PlatformHelper.run(action);

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
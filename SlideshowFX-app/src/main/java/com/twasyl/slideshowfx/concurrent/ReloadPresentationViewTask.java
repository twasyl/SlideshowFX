package com.twasyl.slideshowfx.concurrent;

import com.twasyl.slideshowfx.utils.PlatformHelper;
import javafx.concurrent.Task;
import javafx.scene.web.WebView;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This tasks reloads the presentation view. If the {@link #presentationView} is null, the task is considered as failed
 * and {@link javafx.concurrent.Task#failed()} is called.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class ReloadPresentationViewTask extends Task<Void> {
    private static final Logger LOGGER = Logger.getLogger(ReloadPresentationViewTask.class.getName());

    private final WebView presentationView;

    public ReloadPresentationViewTask(WebView presentationView) {
        this.presentationView = presentationView;
    }

    @Override
    protected Void call() throws Exception {
        if(this.presentationView != null) PlatformHelper.run(() -> this.presentationView.getEngine().reload());
        else this.failed();

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
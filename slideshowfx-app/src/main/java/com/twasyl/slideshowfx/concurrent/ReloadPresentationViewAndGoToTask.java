package com.twasyl.slideshowfx.concurrent;

import com.twasyl.slideshowfx.controllers.PresentationViewController;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This tasks reloads the presentation view and then go to a given slide. If the {@link #presentationView} is null
 * , the task is considered as failed.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class ReloadPresentationViewAndGoToTask extends ReloadPresentationViewTask {
    private static final Logger LOGGER = Logger.getLogger(ReloadPresentationViewAndGoToTask.class.getName());

    public ReloadPresentationViewAndGoToTask(final PresentationViewController presentationView, final String slideId) {
        super(presentationView, () -> {
            final CompletableFuture<Boolean> reloadDone = presentationView.reloadPresentationBrowser();
            reloadDone.thenRun(() -> {
                LOGGER.log(Level.FINE, "Going to slide " + slideId);
                presentationView.goToSlide(slideId);
            });
        });
    }
}
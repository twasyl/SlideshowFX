package com.twasyl.slideshowfx.concurrent;

import com.twasyl.slideshowfx.controllers.PresentationViewController;

/**
 * This tasks reloads the presentation view and then go to a given slide. If the {@link #presentationView} is null
 * , the task is considered as failed.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class ReloadPresentationViewAndGoToTask extends ReloadPresentationViewTask {

    public ReloadPresentationViewAndGoToTask(final PresentationViewController presentationView, final String slideId) {
        super(presentationView, () -> {
            presentationView.goToSlide(slideId);
            presentationView.reloadPresentationBrowser();
        });
    }
}
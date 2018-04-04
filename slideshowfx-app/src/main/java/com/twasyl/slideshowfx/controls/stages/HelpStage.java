package com.twasyl.slideshowfx.controls.stages;

import com.twasyl.slideshowfx.controllers.HelpViewController;

/**
 * The stage allowing to display the help of the application.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class HelpStage extends CustomSlideshowFXStage<HelpViewController> {

    public HelpStage() {
        super("Help", HelpStage.class.getResource("/com/twasyl/slideshowfx/fxml/HelpView.fxml"));
    }
}

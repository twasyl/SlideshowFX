package com.twasyl.slideshowfx.controls.stages;

import com.twasyl.slideshowfx.controllers.HelpViewController;
import com.twasyl.slideshowfx.utils.ResourceHelper;

/**
 * The stage allowing to display the help of the application.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class HelpStage extends CustomSlideshowFXStage<HelpViewController> {

    public HelpStage() {
        super("Help", ResourceHelper.getURL("/com/twasyl/slideshowfx/fxml/HelpView.fxml"));
    }
}

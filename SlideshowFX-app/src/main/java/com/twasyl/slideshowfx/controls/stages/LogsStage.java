package com.twasyl.slideshowfx.controls.stages;

import com.twasyl.slideshowfx.controllers.LogsController;
import com.twasyl.slideshowfx.utils.ResourceHelper;

/**
 * This class extends the {@link javafx.stage.Stage} class in order to display the logs' content of the application.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class LogsStage extends CustomSlideshowFXStage<LogsController> {

    public LogsStage() {
        super("Logs", ResourceHelper.getURL("/com/twasyl/slideshowfx/fxml/Logs.fxml"));
    }
}

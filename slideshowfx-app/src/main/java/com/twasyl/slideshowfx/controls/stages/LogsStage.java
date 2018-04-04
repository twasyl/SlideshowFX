package com.twasyl.slideshowfx.controls.stages;

import com.twasyl.slideshowfx.controllers.LogsController;

/**
 * This class extends the {@link javafx.stage.Stage} class in order to display the logs' content of the application.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class LogsStage extends CustomSlideshowFXStage<LogsController> {

    public LogsStage() {
        super("Logs", LogsStage.class.getResource("/com/twasyl/slideshowfx/fxml/Logs.fxml"));
    }
}

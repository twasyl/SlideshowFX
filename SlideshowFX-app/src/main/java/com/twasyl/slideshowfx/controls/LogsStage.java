package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This class extends the {@link javafx.stage.Stage} class in order to display the logs' content of the application.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class LogsStage extends Stage {

    public LogsStage() {
        super(StageStyle.DECORATED);
        this.initOwner(SlideshowFX.getStage());
        this.setTitle("Logs");

        this.getIcons().addAll(
                new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/appicons/16.png")),
                new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/appicons/32.png")),
                new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/appicons/64.png")),
                new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/appicons/128.png")),
                new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/appicons/256.png")),
                new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/appicons/512.png")));
    }
}

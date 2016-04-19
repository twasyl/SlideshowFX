package com.twasyl.slideshowfx.controls.stages;

import com.twasyl.slideshowfx.controllers.AboutViewController;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * An implementation of {@link Stage} that displays information about the application.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0.0
 */
public class AboutStage extends CustomSlideshowFXStage<AboutViewController> {

    public AboutStage() {
        super("About", ResourceHelper.getURL("/com/twasyl/slideshowfx/fxml/AboutView.fxml"));
        this.initStyle(StageStyle.TRANSPARENT);
        this.initModality(Modality.APPLICATION_MODAL);
        this.setAlwaysOnTop(true);
        this.getScene().setFill(Color.TRANSPARENT);
    }
}

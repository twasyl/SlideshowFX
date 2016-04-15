package com.twasyl.slideshowfx.controls.stages;

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A custom implementation of the {@link Stage} class. It predefines the icons of the stage as well as the owner of it.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class CustomSlideshowFXStage extends Stage {
    private static Logger LOGGER = Logger.getLogger(CustomSlideshowFXStage.class.getName());

    /**
     * Creates a default customized stage with a provided title.
     * @param title The title of the stage.
     */
    public CustomSlideshowFXStage(final String title) {
        super(StageStyle.DECORATED);
        this.initOwner(SlideshowFX.getStage());
        this.setTitle(title);

        this.getIcons().addAll(
                new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/appicons/16.png")),
                new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/appicons/32.png")),
                new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/appicons/64.png")),
                new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/appicons/128.png")),
                new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/appicons/256.png")),
                new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/appicons/512.png")));
    }

    /**
     * Creates a default customized stage with the given title as well as the FXML identified by it's URL.
     * @param title The title of the stage.
     * @param fxml The FXML to load.
     */
    public CustomSlideshowFXStage(final String title, final URL fxml) {
        this(title);

        try {
            final Parent root = FXMLLoader.load(fxml);
            final Scene scene = new Scene(root);
            this.setScene(scene);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not load the desired FXML", e);
        }
    }
}

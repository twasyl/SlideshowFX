package com.twasyl.slideshowfx.controls.stages;

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
public class CustomSlideshowFXStage<T extends Initializable> extends Stage {
    private static Logger LOGGER = Logger.getLogger(CustomSlideshowFXStage.class.getName());
    private T controller;

    /**
     * Creates a default customized stage with a provided title.
     * @param title The title of the stage.
     */
    public CustomSlideshowFXStage(final String title) {
        this(title, null);
    }

    /**
     * Creates a default customized stage with the given title as well as the FXML identified by it's URL.
     * @param title The title of the stage.
     * @param fxml The FXML to load.
     */
    public CustomSlideshowFXStage(final String title, final URL fxml) {
        super(StageStyle.DECORATED);

        this.setDefaultProperties(title);
        this.loadAndSetScene(fxml);
    }

    private void setDefaultProperties(final String title) {
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
     * Loads the given FXML, create a {@link Scene} and set it to this stage.
     * @param fxml The FXML to load.
     */
    public void loadAndSetScene(final URL fxml) {
        if(fxml != null) {
            final FXMLLoader loader = new FXMLLoader(fxml);
            try {
                final Parent root = loader.load();
                this.controller = loader.getController();

                final Scene scene = new Scene(root);
                this.setScene(scene);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not load the desired FXML", e);
            }
        }
    }
    /**
     * Get the controller that has been loaded with the FXML.
     * @return The controller of the specified FXML or {@code null} if no FXML has been loaded yet.
     */
    public T getController() {
        return controller;
    }
}

package com.twasyl.slideshowfx.controls.about;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * An implementation of {@link Stage} that displays information about the application.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0.0
 */
public class AboutStage extends Stage {

    /**
     * Constructs a new {@link AboutStage}.
     * @param owner The owner of the stage.
     */
    public AboutStage(final Window owner) {
        super(StageStyle.TRANSPARENT);
        this.initModality(Modality.APPLICATION_MODAL);
        this.initOwner(owner);
        this.setAlwaysOnTop(true);
        this.setScene(this.buildScene());
    }

    /**
     * Builds the {@link Scene} that will be displayed in this stage.
     * @return The created scene.
     */
    private Scene buildScene() {
        final Scene scene = new Scene(new AboutPanel(), null);

        scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                AboutStage.this.close();
            }
        });

        scene.setOnMouseClicked(mouseEvent -> AboutStage.this.close());

        return scene;
    }
}

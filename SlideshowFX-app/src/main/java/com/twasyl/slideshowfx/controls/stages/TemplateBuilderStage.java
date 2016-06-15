package com.twasyl.slideshowfx.controls.stages;

import com.twasyl.slideshowfx.controllers.TemplateBuilderController;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.utils.ResourceHelper;

import static javafx.stage.Modality.APPLICATION_MODAL;

/**
 * An implementation of the {@link CustomSlideshowFXStage} displaying the template builder.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class TemplateBuilderStage extends CustomSlideshowFXStage<TemplateBuilderController> {

    public TemplateBuilderStage(final TemplateEngine engine) {
        super("Template builder", ResourceHelper.getURL("/com/twasyl/slideshowfx/fxml/TemplateBuilder.fxml"));

        this.initModality(APPLICATION_MODAL);
        this.getController().setTemplateEngine(engine);
        this.getController().setStage(this);
    }
}

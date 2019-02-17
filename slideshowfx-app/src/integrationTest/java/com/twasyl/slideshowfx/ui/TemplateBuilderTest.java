package com.twasyl.slideshowfx.ui;

import com.twasyl.slideshowfx.controls.stages.TemplateBuilderStage;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.theme.Themes;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;

/**
 * @author Thierry Wasylczenko
 */
public class TemplateBuilderTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final TemplateEngine template = new TemplateEngine();
        template.loadArchive(new File("examples/templates/dark-template.sfxt"));

        primaryStage = new TemplateBuilderStage(template);
        Themes.applyTheme(primaryStage.getScene().getRoot(), "Light");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

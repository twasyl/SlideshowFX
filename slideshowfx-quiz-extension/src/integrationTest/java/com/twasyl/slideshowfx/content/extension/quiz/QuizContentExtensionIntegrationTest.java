package com.twasyl.slideshowfx.content.extension.quiz;

import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.plugin.manager.BasePluginIntegrationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@DisplayName("The 'Quiz content extension' plugin")
@ExtendWith(ApplicationExtension.class)
public class QuizContentExtensionIntegrationTest extends BasePluginIntegrationTest {

    @Start
    void onStart(final Stage stage) {
        stage.show();
    }

    @Test
    @DisplayName("can be installed in the plugin manager")
    void pluginIsInstalled() {
        assertPluginIsInstalled(IContentExtension.class, "QUIZ");
    }
}

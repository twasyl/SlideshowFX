package com.twasyl.slideshowfx.content.extension.code;

import com.twasyl.slideshowfx.content.extension.code.enums.SupportedLanguage;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCode.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class CodeContentExtensionUIIntegrationTest {

    private static final String LANGUAGE_LIST_VIEW_QUERY = "#language";
    private static final String LANGUAGE_FILTER_TEXT_FIELD_QUERY = "#languageFilter";

    @Start
    public void onStart(final Stage stage) throws IOException {
        final Parent root = FXMLLoader.load(CodeContentExtensionUIIntegrationTest.class.getResource("/com/twasyl/slideshowfx/content/extension/code/fxml/CodeContentExtension.fxml"));

        final Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void filteringLanguages() {
        final List<SupportedLanguage> expectedLanguages = Arrays.stream(SupportedLanguage.values())
                .filter(supportedLanguage -> supportedLanguage.getName().toLowerCase().contains("java"))
                .collect(Collectors.toList());

        final FxRobot robot = new FxRobot();
        final ListView<SupportedLanguage> language = robot.clickOn(LANGUAGE_LIST_VIEW_QUERY)
                .type(J, A, V, A)
                .lookup(LANGUAGE_LIST_VIEW_QUERY)
                .queryListView();

        assertEquals(expectedLanguages.size(), language.getItems().size());

        final List<Executable> hasAllLanguages = new ArrayList<>();
        expectedLanguages.forEach(supportedLanguage -> hasAllLanguages.add(() -> language.getItems().contains(supportedLanguage)));
        assertAll(hasAllLanguages);

        assertTrue(robot.lookup(LANGUAGE_FILTER_TEXT_FIELD_QUERY).queryTextInputControl().isVisible());
    }

    @Test
    void restoringAllLanguagesByEscaping() {
        final FxRobot robot = new FxRobot();
        ListView<SupportedLanguage> language = robot.clickOn(LANGUAGE_LIST_VIEW_QUERY)
                .type(J, A, V, A)
                .press(ESCAPE)
                .lookup(LANGUAGE_LIST_VIEW_QUERY)
                .queryListView();

        assertFalse(robot.lookup(LANGUAGE_FILTER_TEXT_FIELD_QUERY).queryTextInputControl().isVisible());
        assertEquals(SupportedLanguage.values().length, language.getItems().size());
    }

    @Test
    void restoringAllLanguagesByClickingOnList() {
        final FxRobot robot = new FxRobot();

        final Node cell = robot.lookup(LANGUAGE_LIST_VIEW_QUERY + " .supported-language-cell").nth(2).query();
        final Bounds bounds = cell.localToScreen(cell.getBoundsInLocal());

        ListView<SupportedLanguage> language = robot.clickOn(LANGUAGE_LIST_VIEW_QUERY)
                .type(J, A, V, A)
                .clickOn(bounds)
                .lookup(LANGUAGE_LIST_VIEW_QUERY)
                .queryListView();

        assertFalse(robot.lookup(LANGUAGE_FILTER_TEXT_FIELD_QUERY).queryTextInputControl().isVisible());
        assertEquals(SupportedLanguage.values().length, language.getItems().size());
        assertNotNull(language.getSelectionModel().getSelectedItem());
    }
}

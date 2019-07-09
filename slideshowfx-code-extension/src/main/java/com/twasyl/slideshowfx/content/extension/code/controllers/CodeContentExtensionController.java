package com.twasyl.slideshowfx.content.extension.code.controllers;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtensionController;
import com.twasyl.slideshowfx.content.extension.code.controls.SupportedLanguageCell;
import com.twasyl.slideshowfx.content.extension.code.enums.SupportedLanguage;
import com.twasyl.slideshowfx.ui.controls.ZoomTextArea;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isNotEmpty;
import static java.util.stream.Collectors.toList;
import static javafx.scene.input.KeyCode.ESCAPE;

/**
 * This class is the controller used by the {@code CodeContentExtension.fxml} file.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class CodeContentExtensionController extends AbstractContentExtensionController {

    @FXML
    private ListView<SupportedLanguage> language;
    @FXML
    private TextField languageFilter;
    @FXML
    private ZoomTextArea code;
    @FXML
    private CheckBox showingLineNumbers;
    @FXML
    public TextField highlightedLines;

    /**
     * Get the selected programming language in the UI.
     *
     * @return The selected programming language in the UI.
     */
    public SupportedLanguage getLanguage() {
        return this.language.getSelectionModel().getSelectedItem();
    }

    /**
     * Get the code that has been entered in the UI.
     *
     * @return The code entered in the UI.
     */
    public String getCode() {
        return this.code.getText();
    }

    /**
     * Indicates if the line numbers should be showed.
     *
     * @return {@code true} if the line numbers showed, {@code false} otherwise.
     */
    public Boolean isShowingLineNumbers() {
        return this.showingLineNumbers.isSelected();
    }

    /**
     * Indicates if some lines are specified to be highlighted.
     *
     * @return {@code true} if some lines must be highlighted, {@code false} otherwise.
     */
    public boolean shouldHighlightLines() {
        return this.highlightedLines.getText() != null && !this.highlightedLines.getText().trim().isEmpty();
    }

    public String getHightlightedLines() {
        return this.highlightedLines.getText();
    }

    private void filterLanguagesList() {
        final SupportedLanguage selectedLanguage = this.language.getSelectionModel().getSelectedItem();
        this.language.getItems().clear();

        if (this.languageFilter.getText() == null) {
            this.language.getItems().addAll(SupportedLanguage.values());
        } else {
            final Predicate<SupportedLanguage> containsIgnoreCase = supportedLanguage -> supportedLanguage.getName().toLowerCase().contains(this.languageFilter.getText().toLowerCase());
            this.language.getItems().addAll(Arrays.stream(SupportedLanguage.values())
                    .filter(containsIgnoreCase)
                    .collect(toList()));
        }

        if (selectedLanguage != null) {
            this.language.getSelectionModel().select(selectedLanguage);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.code.setValidator(isNotEmpty());

        this.language.setCellFactory(listview -> new SupportedLanguageCell());
        this.language.getItems().addAll(SupportedLanguage.values());

        this.language.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                this.languageFilter.setVisible(false);
                this.languageFilter.setText(null);
            }
        });

        this.language.setOnKeyTyped(event -> {
            if (!KeyEvent.CHAR_UNDEFINED.equals(event.getCharacter())) {
                this.languageFilter.setVisible(true);
                this.languageFilter.requestFocus();
                this.languageFilter.appendText(event.getCharacter());
            }
        });

        this.languageFilter.textProperty().addListener((property, oldText, newText) -> this.filterLanguagesList());

        this.languageFilter.setOnKeyPressed(event -> {
            if (event.getCode() == ESCAPE) {
                this.languageFilter.setVisible(false);
                this.languageFilter.setText(null);
                event.consume();
            }
        });
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        final ReadOnlyBooleanWrapper property = new ReadOnlyBooleanWrapper();
        property.bind(this.code.validProperty());

        return property;
    }
}

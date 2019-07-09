package com.twasyl.slideshowfx.content.extension.snippet.controllers;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtensionController;
import com.twasyl.slideshowfx.content.extension.snippet.controls.SnippetExecutorListCell;
import com.twasyl.slideshowfx.plugin.manager.PluginManager;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import com.twasyl.slideshowfx.ui.controls.ZoomTextArea;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isNotEmpty;

/**
 * This class is the controller used by the {@code SnippetContentExtension.fxml} file.
 *
 * @author Thierry Wasylczenko
 * @version 1.3-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class SnippetContentExtensionController extends AbstractContentExtensionController {

    @FXML
    private ScrollPane advancedOptions;
    @FXML
    private ComboBox<ISnippetExecutor> language;
    @FXML
    private ZoomTextArea code;

    private final CodeSnippet codeSnippet = new CodeSnippet();

    /**
     * Get the code snippet that has been entered by the user in the UI.
     *
     * @return The code snippet.
     */
    public CodeSnippet getCodeSnippet() {
        return this.codeSnippet;
    }

    /**
     * Get the {@link ISnippetExecutor} associated to the language selected in
     * the UI.
     *
     * @return The snippet executor corresponding to the selection.
     */
    public ISnippetExecutor getSnippetExecutor() {
        return this.language.getValue();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PluginManager.getInstance().getServices(ISnippetExecutor.class)
                .stream()
                .sorted(Comparator.comparing(ISnippetExecutor::getCode))
                .forEach(ref -> this.language.getItems().add(ref));

        this.language.setCellFactory((ListView<ISnippetExecutor> param) -> (ListCell<ISnippetExecutor>) new SnippetExecutorListCell());

        this.language.setButtonCell(new SnippetExecutorListCell());

        this.language.valueProperty().addListener((languageValue, oldLanguage, newLanguage) -> {
            // Clear the code snippet for the new language
            this.codeSnippet.getProperties().clear();
            this.codeSnippet.setCode(null);

            if (newLanguage == null) this.advancedOptions.setContent(null);
            else {
                final Node codeSnippetUI = newLanguage.getUI(codeSnippet);
                if (codeSnippetUI == null) this.advancedOptions.setContent(null);
                else {
                    final TitledPane titledPane = new TitledPane("Advanced options", codeSnippetUI);
                    titledPane.setPrefWidth(250);
                    titledPane.setPadding(new Insets(0, 5, 0, 0));
                    titledPane.setExpanded(true);
                    titledPane.setCollapsible(false);

                    this.advancedOptions.setContent(titledPane);
                }
            }
        });

        this.code.setValidator(isNotEmpty());

        // Each time the code in the ZoomTextArea changes, reflect it to the CodeSnippet
        this.code.textProperty().addListener((textValue, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) this.codeSnippet.setCode(null);
            else this.codeSnippet.setCode(newText);
        });
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        final ReadOnlyBooleanWrapper property = new ReadOnlyBooleanWrapper(true);
        property.bind(this.code.validProperty().and(this.language.getSelectionModel().selectedIndexProperty().isNotEqualTo(-1)));

        return property;
    }
}

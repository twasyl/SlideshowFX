package com.twasyl.slideshowfx.content.extension.snippet.controllers;

import com.twasyl.slideshowfx.content.extension.snippet.controls.SnippetExecutorListCell;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import com.twasyl.slideshowfx.ui.controls.ZoomTextArea;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This class is the controller used by the {@code SnippetContentExtension.fxml} file.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class SnippetContentExtensionController implements Initializable {

    @FXML private ScrollPane advancedOptions;
    @FXML private ComboBox<ISnippetExecutor> language;
    @FXML private ZoomTextArea code;

    private final CodeSnippet codeSnippet = new CodeSnippet();

    /**
     * Get the code snippet that has been entered by the user in the UI.
     * @return The code snippet.
     */
    public CodeSnippet getCodeSnippet() { return this.codeSnippet; }

    /**
     * Get the {@link com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor} associated to the language selected in
     * the UI.
     * @return The snippet executor corresponding to the selection.
     */
    public ISnippetExecutor getSnippetExecutor() { return this.language.getValue(); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        OSGiManager.getInstance().getInstalledServices(ISnippetExecutor.class)
                        .stream()
                        .sorted((snippet1, snippet2) -> snippet1.getCode().compareTo(snippet2.getCode()))
                        .forEach(ref -> this.language.getItems().add(ref));

        this.language.setCellFactory((ListView<ISnippetExecutor> param) -> {
                final ListCell<ISnippetExecutor> cell = new SnippetExecutorListCell();
                return cell;
        });

        this.language.setButtonCell(new SnippetExecutorListCell());

        this.language.valueProperty().addListener((languageValue, oldLanguage, newLanguage) -> {
            // Clear the code snippet for the new language
            this.codeSnippet.getProperties().clear();
            this.codeSnippet.setCode(null);

            if(newLanguage == null) this.advancedOptions.setContent(null);
            else {
                final Node codeSnippetUI = newLanguage.getUI(codeSnippet);
                if(codeSnippetUI == null) this.advancedOptions.setContent(null);
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

        // Each time the code in the ZoomTextArea changes, reflect it to the CodeSnippet
        this.code.textProperty().addListener((textValue, oldText, newText) -> {
            if(newText == null || newText.isEmpty()) this.codeSnippet.setCode(null);
            else this.codeSnippet.setCode(newText);
        });
    }
}

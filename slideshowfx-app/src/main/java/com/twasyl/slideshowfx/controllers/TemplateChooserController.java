package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.controls.TemplateFileButton;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.io.SlideshowFXExtensionFilter;
import com.twasyl.slideshowfx.utils.DialogHelper;
import com.twasyl.slideshowfx.utils.io.DefaultCharsetReader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.TilePane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.io.SlideshowFXFileFilters.TEMPLATE_FILE_FILTER;

/**
 * Controller of the {@code TemplateChooser.fxml} view.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class TemplateChooserController implements Initializable {

    private static Logger LOGGER = Logger.getLogger(TemplateChooserController.class.getName());
    protected static final String DEFAULT_PREVIEW_URL = "/com/twasyl/slideshowfx/html/empty-webview.html";

    private ToggleGroup templatesGroup = new ToggleGroup();
    @FXML
    private TilePane templates;
    @FXML
    private Button importNewTemplate;
    @FXML
    private WebView preview;

    /**
     * Get the template that has been chosen by the user.
     *
     * @return The file chosen by the user or {@code null} if no selection.
     */
    public File getChosenTemplate() {
        final TemplateFileButton button = (TemplateFileButton) this.templatesGroup.getSelectedToggle();

        if (button != null) {
            return button.getTemplateFile();
        } else {
            return null;
        }
    }

    @FXML
    private void dragFilesOverImportButton(final DragEvent event) {
        final Dragboard dragboard = event.getDragboard();

        if (event.getGestureSource() != this.importNewTemplate && dragboard.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }

        event.consume();
    }

    @FXML
    private void dropFileOverImportButton(final DragEvent event) {
        final Dragboard dragboard = event.getDragboard();
        boolean allFilesAreValid;

        if (event.getGestureSource() != this.importNewTemplate && dragboard.hasFiles()) {
            allFilesAreValid = true;
            File templateFile;
            int index = 0;

            while (allFilesAreValid && index < dragboard.getFiles().size()) {
                templateFile = dragboard.getFiles().get(index++);

                allFilesAreValid = this.addTemplateToLibrary(templateFile);
            }
        } else {
            allFilesAreValid = false;
        }

        event.setDropCompleted(allFilesAreValid);
        event.consume();
    }

    @FXML
    private void importNewTemplate(final ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.TEMPLATE_FILTER);
        File templateFile = chooser.showOpenDialog(null);

        if (templateFile != null) {
            this.addTemplateToLibrary(templateFile);
        }
    }

    /**
     * Checks if a file chosen by the user is valid or not. In case the file is not a valid template, then an error
     * message is displayed. If it is valid, the template file is added to the list of templates to install and displayed
     * in the templates table.
     *
     * @param templateFile The template file to check.
     * @return {@code true} if the file is a valid template, {@code false} otherwise.
     */
    protected boolean addTemplateToLibrary(final File templateFile) {
        boolean valid = false;

        try {
            if (fileSeemsValid(templateFile)) {
                final TemplateFileButton templateFileButton = createTemplateFileButton(templateFile);
                templateFileButton.setSelected(true);

                this.templates.getChildren().add(templateFileButton);

                valid = true;
            } else {
                DialogHelper.showError("Invalid template", "The chosen template seems invalid");
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Can not determine if the template file seems valid", e);
        }

        return valid;
    }

    /**
     * Creates an instance of {@link TemplateFileButton} for the provided template file.
     *
     * @param templateFile The template file to associated to the create button.
     * @return The created button.
     */
    protected TemplateFileButton createTemplateFileButton(File templateFile) {
        final TemplateFileButton templateFileButton = new TemplateFileButton(templateFile);
        this.templatesGroup.getToggles().add(templateFileButton);

        templateFileButton.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
            try {
                if (newSelected) {
                    final File extraction = new File(System.getProperty("java.io.tmpdir"), "sfx-new-presentation-" + templateFile.getName());
                    templateFileButton.extractTemplate(extraction);

                    final File sample = new File(extraction, "sample.html");

                    if (sample.exists()) {
                        this.preview.getEngine().load(sample.toURI().toURL().toExternalForm());
                    } else {
                        this.loadDefaultPreview();
                    }
                } else {
                    templateFileButton.removeExtractedContent();
                }
            } catch (IOException ex) {

            }
        });

        return templateFileButton;
    }

    /**
     * Checks if the given {@link File file} is seems to be a valid template file.
     *
     * @param file The file to check.
     * @return {@code true} if the file seems to be a template, {@code false} otherwise.
     * @throws NullPointerException  If the file is {@code null}.
     * @throws FileNotFoundException If the file doesn't exist.
     */
    protected boolean fileSeemsValid(final File file) throws FileNotFoundException {
        if (file == null) throw new NullPointerException("The file to check can not be null");
        if (!file.exists()) throw new FileNotFoundException("The file to check must exist");

        boolean isValid = file.getName().endsWith(TemplateEngine.DEFAULT_DOTTED_ARCHIVE_EXTENSION);

        return isValid;
    }

    protected void populateTemplatesView() {
        final File templateLibraryDirectory = GlobalConfiguration.getTemplateLibraryDirectory();

        if (templateLibraryDirectory.exists() && templateLibraryDirectory.canRead()) {
            final File[] templates = templateLibraryDirectory.listFiles(TEMPLATE_FILE_FILTER);

            Arrays.stream(templates).forEach(file -> {
                final TemplateFileButton button = createTemplateFileButton(file);

                if (button != null) {
                    this.templates.getChildren().add(button);
                }
            });
        }
    }

    public void dispose() {
        this.templates.getChildren().stream()
                .filter(node -> node instanceof TemplateFileButton)
                .map(node -> (TemplateFileButton) node)
                .forEach(button -> {
                    try {
                        button.removeExtractedContent();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Error while trying to clean resources", e);
                    }
                });
    }

    protected void initializeTemplatesToggleGroup() {
        this.templatesGroup.selectedToggleProperty().addListener((toggle, oldToggle, newToggle) -> {
            if (newToggle == null) {
                this.loadDefaultPreview();
            }
        });
    }

    protected void loadDefaultPreview() {
        final StringJoiner builder = new StringJoiner("\n");

        try (final BufferedReader reader = new DefaultCharsetReader(TemplateChooserController.class.getResourceAsStream(DEFAULT_PREVIEW_URL))) {
            reader.lines().forEach(builder::add);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not read the default preview page", e);
        }

        this.preview.getEngine().loadContent(builder.toString());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.loadDefaultPreview();
        this.initializeTemplatesToggleGroup();
        this.populateTemplatesView();
    }
}

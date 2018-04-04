package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.utils.DialogHelper;
import com.twasyl.slideshowfx.utils.JSONHelper;
import com.twasyl.slideshowfx.utils.ZipUtils;
import com.twasyl.slideshowfx.utils.io.IOUtils;
import io.vertx.core.json.JsonObject;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.twasyl.slideshowfx.engine.template.TemplateEngine.DEFAULT_CONFIGURATION_FILE_NAME;
import static com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration.*;
import static java.util.zip.ZipFile.OPEN_READ;

/**
 * Implementation of a {@link ToggleButton} representing a file of a template. The button has a CSS class named
 * {@code template-file-button}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class TemplateFileButton extends ToggleButton {
    private static Logger LOGGER = Logger.getLogger(TemplateFileButton.class.getName());

    private static final double BUTTON_SIZE = 80;

    private String templateName;
    private String templateVersion;
    private File extractionLocation = null;
    private boolean presentInLibrary;

    public TemplateFileButton(final File templateFile) {
        this.setUserData(templateFile);

        this.defineIfPresentInLibrary();
        this.setButtonSize();
        this.defineContextMenu();
        this.setWrapText(true);
        this.getStyleClass().add("template-file-button");

        this.templateName = determineTemplateName();
        this.templateVersion = determineTemplateVersion();

        final StringBuilder templateIdentification = new StringBuilder(this.templateName);
        if (!this.templateVersion.isEmpty()) {
            templateIdentification.append(" (v").append(this.templateVersion).append(")");
        }

        this.setText(templateIdentification.toString());
        this.setTooltip(new Tooltip(this.templateName));
    }

    /**
     * Define if the template file returned by {@link #getTemplateFile()} is already present in the template library
     * directory returned by {@link GlobalConfiguration#getTemplateLibraryDirectory()}.
     */
    private final void defineIfPresentInLibrary() {
        this.presentInLibrary = false;

        if (this.getTemplateFile() != null) {
            final File parent = this.getTemplateFile().getParentFile();

            this.presentInLibrary = GlobalConfiguration.getTemplateLibraryDirectory().equals(parent);
        }
    }

    /**
     * Call the {@link #setPrefSize(double, double)}, {@link #setMinSize(double, double)} and
     * {@link #setMaxSize(double, double)} methods for this button.
     */
    protected void setButtonSize() {
        this.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        this.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        this.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
    }

    /**
     * Define the {@link ContextMenu} that will be use for this button.
     */
    protected void defineContextMenu() {
        final ContextMenu menu = new ContextMenu();

        if (this.presentInLibrary) {
            final MenuItem deleteTemplate = new MenuItem("Delete this template");
            deleteTemplate.setOnAction(event -> {
                final ButtonType answer = DialogHelper.showConfirmationAlert("Delete this template", "Are you sure you want to delete the template from the library?");

                if (answer == ButtonType.YES) {
                    try {
                        this.removeExtractedContent();
                        if (this.getTemplateFile().exists()) {
                            this.getTemplateFile().delete();
                        }

                        ((Pane) this.getParent()).getChildren().remove(this);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Can not remove the template", e);
                        DialogHelper.showError("Error", "Can not remove the template");
                    }
                }
            });

            menu.getItems().add(deleteTemplate);
        }

        this.setContextMenu(menu);
    }

    /**
     * Get the template file associated to this button.
     *
     * @return The template file associated to this button.
     */
    public File getTemplateFile() {
        return (File) this.getUserData();
    }

    /**
     * Extracts the template to a desired location.
     *
     * @param destination The folder where extract the template.
     * @throws IOException If an error occurs during the extraction.
     */
    public void extractTemplate(final File destination) throws IOException {
        this.extractionLocation = destination;
        ZipUtils.unzip(getTemplateFile(), this.extractionLocation);
    }

    /**
     * Removes the content that has been extracted by the {@link #extractTemplate(File)} method.
     *
     * @throws IOException If an error occurs when trying to remove the extracted content.
     */
    public void removeExtractedContent() throws IOException {
        if (this.extractionLocation != null && this.extractionLocation.exists()) {
            IOUtils.deleteDirectory(this.extractionLocation);
        }
    }

    /**
     * Determine the name of this template. This method will look for a file named
     * {@linkplain TemplateEngine#DEFAULT_CONFIGURATION_FILE_NAME}. If it is found, then the name of the template is
     * taken from this configuration.
     * If the name can not be determined by the configuration, then the name of the file returned by {@link #getTemplateFile()}
     * will be returned.
     *
     * @return The name of the template.
     */
    protected final String determineTemplateName() {
        String name = getTemplateFile().getName();
        final JsonObject jsonConfig = getJsonConfig();

        if (jsonConfig != null) {
            name = jsonConfig.getJsonObject(TEMPLATE).getString(TEMPLATE_NAME);

            if (name.trim().isEmpty()) {
                name = getTemplateFile().getName();
            }
        }

        return name;
    }

    /**
     * Determine the version of this template. This method will look for a file named
     * {@linkplain TemplateEngine#DEFAULT_CONFIGURATION_FILE_NAME}. If it is found, then the version of the template is
     * taken from this configuration.
     * If the name can not be determined by the configuration, then an empty string will be returned.
     *
     * @return The version of the template.
     */
    protected final String determineTemplateVersion() {
        String version = "";
        final JsonObject jsonConfig = getJsonConfig();

        if (jsonConfig != null) {
            version = jsonConfig.getJsonObject(TEMPLATE).getString(TEMPLATE_VERSION, null);

            if (version == null || version.trim().isEmpty()) {
                version = "";
            } else {
                version = version.trim();
            }
        }

        return version;
    }

    /**
     * Get the JSON configuration object stored within the template. This method looks for a file named
     * {@linkplain TemplateEngine#DEFAULT_CONFIGURATION_FILE_NAME}. If it is found, then the configuration is
     * retrieved from this file.
     *
     * @return The JSON configuration stored in the template, {@code null} if it is not found.
     */
    protected JsonObject getJsonConfig() {
        JsonObject jsonConfig = null;

        try (final ZipFile zip = new ZipFile(getTemplateFile(), OPEN_READ)) {

            final ZipEntry templateConfigEntry = zip.stream()
                    .filter(entry -> DEFAULT_CONFIGURATION_FILE_NAME.equals(entry.getName()))
                    .findAny()
                    .orElseGet(null);

            if (templateConfigEntry != null) {
                try (final InputStreamReader input = new InputStreamReader(zip.getInputStream(templateConfigEntry))) {
                    jsonConfig = JSONHelper.readFromReader(input);
                }
            } else {
                LOGGER.log(Level.INFO, "No template-config.json file in the template");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can not determine template's name", e);
        }
        return jsonConfig;
    }
}

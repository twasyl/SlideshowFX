package com.twasyl.slideshowfx.controls.builder.nodes;

import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.engine.template.configuration.SlideElementTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration;
import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import com.twasyl.slideshowfx.utils.DialogHelper;
import com.twasyl.slideshowfx.utils.beans.Pair;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isNotEmpty;

/**
 * This class provides a control allowing to open a template configuration file. It defines the complete UI used
 * to add slides, define the template configuration.
 * In order to fill the control with a configuration file, the method {@link #fillWithFile(File)}} must be used.
 * In order to get the configuration as a string, the method {@link #getAsString()} must be used.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.3
 */
public class TemplateConfigurationFilePane extends VBox {
    private static Logger LOGGER = Logger.getLogger(TemplateConfigurationFilePane.class.getName());

    private Path workingPath;

    // General template configuration
    private ExtendedTextField templateName = new ExtendedTextField("Name", true);
    private ExtendedTextField templateFile = new ExtendedTextField("File", true);
    private ExtendedTextField jsObject = new ExtendedTextField("JS Object", true);
    private ExtendedTextField templateResourcesDirectory = new ExtendedTextField("Resources' directory", true);
    private List<TemplateVariable> defaultVariables = new ArrayList<>();

    // Template default variables
    final VBox defaultVariablesPane = new VBox(5);

    // General slides configuration
    private ExtendedTextField slidesContainer = new ExtendedTextField("Slides' container", true);
    private ExtendedTextField slideIdPrefix = new ExtendedTextField("Slide ID prefix", true);
    private ExtendedTextField slidesTemplateDirectory = new ExtendedTextField("Presentation directory", true);
    private ExtendedTextField slidesPresentationDirectory = new ExtendedTextField("Presentation directory", true);
    private ExtendedTextField slidesThumbnailDirectory = new ExtendedTextField("Thumbnails directory", true);

    // Slides definitions
    private List<SlideDefinition> slideDefinitions = new ArrayList<>();
    private VBox slideDefinitionsPane = new VBox(5);

    public TemplateConfigurationFilePane() {
        this.setSpacing(10);

        this.initializeMandatoryFields();

        this.getChildren().addAll(this.getTemplateGlobalConfigurationPane(),
                this.getTemplateDefaultVariablePane(),
                this.getSlidesGlobalConfigurationPane(),
                getSlidesPane());

        this.parentProperty().addListener((parentValue, oldParent, newParent) -> {
            if (this.prefWidthProperty().isBound()) {
                this.prefWidthProperty().unbind();
            }

            if (newParent != null && newParent instanceof Region) {
                this.prefWidthProperty().bind(((Region) newParent).widthProperty());
            }
        });
    }

    /**
     * Initialize the {@link ExtendedTextField fields} that are mandatory by applying validators to them.
     */
    private void initializeMandatoryFields() {
        this.templateName.setValidator(isNotEmpty());
        this.templateFile.setValidator(isNotEmpty());
        this.templateResourcesDirectory.setValidator(isNotEmpty());
        this.jsObject.setValidator(isNotEmpty());
        this.slidesContainer.setValidator(isNotEmpty());
        this.slideIdPrefix.setValidator(isNotEmpty());
        this.slidesPresentationDirectory.setValidator(isNotEmpty());
        this.slidesTemplateDirectory.setValidator(isNotEmpty());
        this.slidesThumbnailDirectory.setValidator(isNotEmpty());
    }

    /**
     * Build the {@link TitledPane} that contains the UI elements for specifying the global configuration of the
     * template, as the name, file, resources' directory and JS object.
     *
     * @return The properly initialized {@link TitledPane} containing the global configuration elements.
     */
    private TitledPane getTemplateGlobalConfigurationPane() {
        final FlowPane internalContainer = new FlowPane(5, 5, templateName, templateFile, templateResourcesDirectory, jsObject);
        final TitledPane templateGlobalConfigurationPane = new TitledPane("Template global configuration", internalContainer);
        templateGlobalConfigurationPane.setCollapsible(false);

        return templateGlobalConfigurationPane;
    }

    /**
     * Build the {@link TitledPane} that contains the UI elements for specifying the default template variables.
     *
     * @return The properly initialized {@link TitledPane} containing the default template variables elements.
     */
    private TitledPane getTemplateDefaultVariablePane() {
        final Button addButton = new Button("Add");
        addButton.setTooltip(new Tooltip("Add a default template variable"));
        addButton.setOnAction(event -> this.addDefaultTemplateVariable());

        final VBox internalContainer = new VBox(5, defaultVariablesPane, addButton);

        final TitledPane variablePane = new TitledPane("Template default variables", internalContainer);
        variablePane.setCollapsible(false);

        return variablePane;
    }

    /**
     * Build the {@link TitledPane} that contains the UI elements for specifying the global slides' configuration as
     * the slides' container, slide's ID prefix, presentation directory, template directory and thumbnails directory.
     *
     * @return The properly initialized {@link TitledPane} containing the slides' global configuration UI elements.
     */
    private TitledPane getSlidesGlobalConfigurationPane() {
        final FlowPane internalContainer = new FlowPane(5, 5,
                slidesContainer, slideIdPrefix, slidesPresentationDirectory, slidesTemplateDirectory, slidesThumbnailDirectory);

        final TitledPane slidesGlobalConfigurationPane = new TitledPane("Slides global configuration", internalContainer);
        slidesGlobalConfigurationPane.setCollapsible(false);

        return slidesGlobalConfigurationPane;
    }

    /**
     * Build the {@link TitledPane} that contains the UI elements for specifying the slides.
     *
     * @return The properly initialized {@link TitledPane} containing the slides UI elements.
     */
    private TitledPane getSlidesPane() {
        final Button addButton = new Button("Add slide");
        addButton.setTooltip(new Tooltip("Add a slide"));
        addButton.setOnAction(event -> this.addSlide());

        final VBox internalContainer = new VBox(5, slideDefinitionsPane, addButton);

        final TitledPane slidesPane = new TitledPane("Slides", internalContainer);
        slidesPane.setCollapsible(false);

        return slidesPane;
    }

    /**
     * Get the working path of this control. The working path correspond to the folder on the disk where the template
     * is built.
     *
     * @return The working path of this template.
     */
    public Path getWorkingPath() {
        return workingPath;
    }

    /**
     * Set the directory in which the template is built.
     *
     * @param workingPath The working path of this template.
     */
    public void setWorkingPath(Path workingPath) {
        this.workingPath = workingPath;
    }

    /**
     * Adds a template variable to the UI.
     *
     * @return The UI element added to the editor corresponding to the new template variable.
     */
    private TemplateVariable addDefaultTemplateVariable() {
        final TemplateVariable variable = new TemplateVariable();
        variable.setOnDelete(event -> {
            final ButtonType answer = DialogHelper.showConfirmationAlert("Delete variable", "Are you sure you want to delete this variable?");

            if (answer == ButtonType.YES) {
                defaultVariables.remove(variable);
                defaultVariablesPane.getChildren().remove(variable);
            }
        });

        defaultVariables.add(variable);
        this.defaultVariablesPane.getChildren().add(variable);

        return variable;
    }

    /**
     * Add the UI element corresponding to a slide to the current UI.
     *
     * @return The element used to define a new slide in the template.
     */
    private SlideDefinition addSlide() {
        final SlideDefinition slideDefinition = new SlideDefinition();
        this.slideDefinitions.add(slideDefinition);
        this.slideDefinitionsPane.getChildren().add(slideDefinition);

        slideDefinition.setOnDelete(event -> {
            final ButtonType answer = DialogHelper.showConfirmationAlert("Delete slide", "Are you sure you want to delete this slide?");

            if (answer == ButtonType.YES) {
                this.slideDefinitions.remove(slideDefinition);
                this.slideDefinitionsPane.getChildren().remove(slideDefinition);
            }
        });

        return slideDefinition;
    }

    /**
     * Return the configuration given inside the UI as a JSON structure. This structure is the one defined for the
     * {@code template-config.json} file and can be put inside this file.
     *
     * @return The JSON structure representing this template configuration.
     */
    public String getAsString() {
        final TemplateConfiguration configuration = new TemplateConfiguration();
        configuration.setName(this.templateName.getText());
        configuration.setFile(new File(this.workingPath.toFile(), this.templateFile.getText()));
        configuration.setJsObject(this.jsObject.getText());
        configuration.setResourcesDirectory(new File(this.workingPath.toFile(), this.templateResourcesDirectory.getText()));
        configuration.setDefaultVariables(
                this.defaultVariables.stream()
                        .filter(TemplateVariable::isValid)
                        .map(variable -> new Pair<>(variable.getName(), variable.getValue()))
                        .collect(Collectors.toSet())
        );
        configuration.setSlidesContainer(this.slidesContainer.getText());
        configuration.setSlideIdPrefix(this.slideIdPrefix.getText());
        configuration.setSlidesTemplateDirectory(new File(this.workingPath.toFile(), this.slidesTemplateDirectory.getText()));
        configuration.setSlidesPresentationDirectory(new File(this.workingPath.toFile(), this.slidesPresentationDirectory.getText()));
        configuration.setSlidesThumbnailDirectory(new File(this.workingPath.toFile(), this.slidesThumbnailDirectory.getText()));

        configuration.setSlideTemplates(new ArrayList<>());

        configuration.setSlideTemplates(
                this.slideDefinitions.stream()
                        .filter(SlideDefinition::isValid)
                        .map(definition -> {
                            final SlideTemplate template = new SlideTemplate(
                                    definition.getSlideId(),
                                    definition.getName(),
                                    new File(definition.getFile()));

                            if (!definition.getSlideElements().isEmpty()) {
                                template.setElements(
                                        definition.getSlideElements().stream()
                                                .filter(SlideElementDefinition::isValid)
                                                .map(elementDefinition -> {
                                                    final SlideElementTemplate elementTemplate = new SlideElementTemplate();
                                                    elementTemplate.setId(elementDefinition.getElementId());
                                                    elementTemplate.setHtmlId(elementDefinition.getHtmlId());
                                                    elementTemplate.setDefaultContent(elementDefinition.getDefaultContent());
                                                    return elementTemplate;
                                                })
                                                .toArray(SlideElementTemplate[]::new));
                            }

                            return template;
                        })
                        .collect(Collectors.toList()));

        final TemplateEngine engine = new TemplateEngine();
        engine.setConfiguration(configuration);
        engine.setWorkingDirectory(this.workingPath.toFile());
        final StringWriter writer = new StringWriter();

        try {
            engine.writeConfiguration(writer);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not save the template configuration file", e);
        }

        return writer.toString();
    }

    /**
     * Populate the UI according the given configuration file.
     *
     * @param file The configuration file to open.
     */
    public void fillWithFile(final File file) {
        final TemplateEngine engine = new TemplateEngine();
        engine.setWorkingDirectory(this.workingPath.toFile());

        try {
            final TemplateConfiguration configuration = engine.readConfiguration(file);

            this.templateName.setText(configuration.getName());
            this.templateFile.setText(this.getPathRelativeToWorkingPath(configuration.getFile()));
            this.jsObject.setText(configuration.getJsObject());
            this.templateResourcesDirectory.setText(this.getPathRelativeToWorkingPath(configuration.getResourcesDirectory()));

            configuration.getDefaultVariables()
                    .forEach(variable -> {
                        final TemplateVariable templateVariable = this.addDefaultTemplateVariable();
                        templateVariable.setName(variable.getKey());
                        templateVariable.setValue(variable.getValue());
                    });

            this.slidesContainer.setText(configuration.getSlidesContainer());
            this.slideIdPrefix.setText(configuration.getSlideIdPrefix());
            this.slidesTemplateDirectory.setText(this.getPathRelativeToWorkingPath(configuration.getSlidesTemplateDirectory()));
            this.slidesPresentationDirectory.setText(this.getPathRelativeToWorkingPath(configuration.getSlidesPresentationDirectory()));
            this.slidesThumbnailDirectory.setText(this.getPathRelativeToWorkingPath(configuration.getSlidesThumbnailDirectory()));

            configuration.getSlideTemplates()
                    .forEach(template -> {
                        final SlideDefinition definition = this.addSlide();
                        definition.setSlideId(template.getId());
                        definition.setName(template.getName());
                        definition.setFile(template.getFile().getName());

                        for (SlideElementTemplate elementTemplate : template.getElements()) {
                            final SlideElementDefinition slideElementDefinition = definition.addSlideElement();
                            slideElementDefinition.setElementId(elementTemplate.getId());
                            slideElementDefinition.setHtmlId(elementTemplate.getHtmlId());
                            slideElementDefinition.setDefaultContent(elementTemplate.getDefaultContent());
                        }
                    });
        } catch (IOException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Can not read the template configuration file", e);
        }
    }

    /**
     * Indicates if the configuration defined within the UI is considered valid.
     *
     * @return {@code true} if the configuration is valid, {@code false} otherwise.
     */
    public boolean isContentValid() {
        boolean globallyValid = true;

        if (!this.defaultVariables.isEmpty()) {
            boolean isValid;
            for (TemplateVariable variable : this.defaultVariables) {
                isValid = variable.isValid();

                if (globallyValid && !isValid) {
                    globallyValid = false;
                }
            }
        }

        if (!this.slideDefinitions.isEmpty()) {
            boolean isValid;
            for (SlideDefinition slide : this.slideDefinitions) {
                isValid = slide.isValid();

                if (globallyValid && !isValid) {
                    globallyValid = false;
                }
            }
        }

        boolean isValid = this.templateName.isValid();
        if (globallyValid && !isValid) {
            globallyValid = false;
        }

        isValid = this.templateFile.isValid();
        if (globallyValid && !isValid) {
            globallyValid = false;
        }

        isValid = this.templateResourcesDirectory.isValid();
        if (globallyValid && !isValid) {
            globallyValid = false;
        }

        isValid = this.jsObject.isValid();
        if (globallyValid && !isValid) {
            globallyValid = false;
        }

        isValid = this.slidesContainer.isValid();
        if (globallyValid && !isValid) {
            globallyValid = false;
        }

        isValid = this.slideIdPrefix.isValid();
        if (globallyValid && !isValid) {
            globallyValid = false;
        }

        isValid = this.slidesPresentationDirectory.isValid();
        if (globallyValid && !isValid) {
            globallyValid = false;
        }

        isValid = this.slidesTemplateDirectory.isValid();
        if (globallyValid && !isValid) {
            globallyValid = false;
        }

        isValid = this.slidesThumbnailDirectory.isValid();
        if (globallyValid && !isValid) {
            globallyValid = false;
        }

        return globallyValid;
    }

    /**
     * Get the relative path of the given file from the current working path of the editor.
     *
     * @param file The file to determine the relative path.
     * @return The relative path of the given file from the current working path.
     */
    private String getPathRelativeToWorkingPath(final File file) {
        return this.workingPath.relativize(file.toPath()).toString();
    }
}

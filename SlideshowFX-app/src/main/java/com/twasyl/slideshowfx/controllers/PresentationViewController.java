/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.controls.PresentationBrowser;
import com.twasyl.slideshowfx.controls.PresentationVariablesPanel;
import com.twasyl.slideshowfx.controls.QuizzCreatorPanel;
import com.twasyl.slideshowfx.controls.SlideContentEditor;
import com.twasyl.slideshowfx.controls.slideshow.Context;
import com.twasyl.slideshowfx.controls.slideshow.SlideshowStage;
import com.twasyl.slideshowfx.dao.PresentationDAO;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import com.twasyl.slideshowfx.engine.presentation.configuration.SlideElement;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.MarkupManager;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import com.twasyl.slideshowfx.utils.DialogHelper;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import com.twasyl.slideshowfx.utils.beans.Pair;
import com.twasyl.slideshowfx.utils.beans.binding.FilenameBinding;
import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Base64;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  This class is the controller of the <code>PresentationView.fxml</code> file. It defines all actions possible inside the view
 *  represented by the FXML.
 *  
 *  @author Thierry Wasyczenko
 *  @version 1.0
 *  @since SlideshowFX 1.0.0
 */
public class PresentationViewController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(PresentationViewController.class.getName());

    private SlideshowFXController parent;
    private PresentationEngine presentationEngine;
    private final ReadOnlyStringProperty presentationName = new SimpleStringProperty();
    private final ReadOnlyBooleanProperty presentationModified = new SimpleBooleanProperty(false);

    @FXML private PresentationBrowser browser;
    @FXML private TextField slideNumber;
    @FXML private TextField fieldName;
    @FXML private HBox markupContentTypeBox;
    @FXML private ToolBar contentExtensionToolBar;
    @FXML private ToggleGroup markupContentType = new ToggleGroup();
    @FXML private SlideContentEditor contentEditor;
    @FXML private Button defineContent;

    /* All methods called by the FXML */

    /**
     * This method is called by the <code>Define</code> button of the FXML. The selected syntax is retrieved as well as the content.
     * The treatment is then delegated to the {@link #updateSlide(com.twasyl.slideshowfx.markup.IMarkup, String)} method.
     *
     * @param event
     * @throws javax.xml.transform.TransformerException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     */
    @FXML private void updateSlideWithText(ActionEvent event) throws TransformerException, IOException, ParserConfigurationException, SAXException {
        RadioButton selectedMarkup = (RadioButton) this.markupContentType.getSelectedToggle();
        this.updateSlide((IMarkup) selectedMarkup.getUserData(), this.contentEditor.getContentEditorValue());
    }

    /**
     * Displays the wizard allowing to insert a quiz in the presentation.
     * @param event
     */
    @FXML private void insertQuizz(ActionEvent event) {
        final QuizzCreatorPanel quizzCreatorPanel = new QuizzCreatorPanel();

        final ButtonType response = DialogHelper.showCancellableDialog("Insert a quizz", quizzCreatorPanel);

        if(response != null && response == ButtonType.OK) {
            this.contentEditor.appendContentEditorValue(quizzCreatorPanel.convertToHtml());
        }
    }

    /**
     * Define and manages variables that are available for the presentation. Variable allow to insert elements which
     * values will be replaced inside the presentation.
     * @param event The source event calling this method.
     */
    @FXML private void definePresentationVariables(ActionEvent event) {
        final PresentationVariablesPanel variablesPanel = new PresentationVariablesPanel(this.presentationEngine.getConfiguration());

        final ButtonType insert = new ButtonType("Insert", ButtonBar.ButtonData.OTHER);

        final ButtonType answer = DialogHelper.showDialog("Insert a variable", variablesPanel, ButtonType.CANCEL, insert, ButtonType.OK);

        // Insert the token inside the editor
        if(answer != null && answer == insert) {
            final Pair<String, String> variable = variablesPanel.getSelectedVariable();

            if(variable != null) this.contentEditor.appendContentEditorValue(String.format("${%1$s}", variable.getKey()));
        }

        // If cancel wasn't clicked, updates all variables in the presentation and updates it the presentation file
        if(answer != ButtonType.CANCEL) {
            this.presentationEngine.getConfiguration().setVariables(variablesPanel.getVariables());

            this.presentationEngine.getConfiguration()
                    .getSlides()
                    .forEach(slide -> this.presentationEngine.getConfiguration().updateSlideInDocument(slide));

            this.presentationEngine.savePresentationFile();
            this.reloadPresentationBrowser();
        }
    }

    /**
     * This method updates a slide of the presentation. It takes the <code>markup</code> to converte the <code>originalContent</code>
     * in HTML and then the slide element is updated. The presentation is then saved temporary.
     * The content is send to the page by calling the {@link com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration#getContentDefinerMethod()}
     * with the HTML content converted in Base64.
     * A screenshot of the slide is taken to update the menu of available slides.
     *
     * @param markup The markup with which the new content was generated.
     * @param originalContent The original content, in Base64, with which the slide will be updated.
     * @throws javax.xml.transform.TransformerException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     */
    private void updateSlide(final IMarkup markup, final String originalContent) throws TransformerException, IOException, ParserConfigurationException, SAXException {
        final String elementId = String.format("%1$s-%2$s", this.slideNumber.getText(), this.fieldName.getText());
        String htmlContent = markup.convertAsHtml(originalContent);

        // Update the SlideElement
        final Slide slideToUpdate = this.presentationEngine.getConfiguration().getSlideByNumber(this.slideNumber.getText());
        slideToUpdate.updateElement(elementId, markup.getCode(), originalContent, htmlContent);

        this.presentationEngine.getConfiguration().updateSlideInDocument(slideToUpdate);

        this.presentationEngine.savePresentationFile();

        // Clear the HTML of any variables
        htmlContent = slideToUpdate.getElement(elementId).getClearedHtmlContent(this.presentationEngine.getConfiguration().getVariables());

        this.browser.defineContent(this.slideNumber.getText(), this.fieldName.getText(), htmlContent);

        // Take a thumbnail of the slide
        WritableImage thumbnail = this.browser.snapshot(null, null);
        this.presentationEngine.getConfiguration().updateSlideThumbnail(this.slideNumber.getText(), thumbnail);

        if(this.parent != null) this.parent.updateSlideSplitMenu();

        ((SimpleBooleanProperty) this.presentationModified).set(true);
    }

    /**
     * Update the JavaFX UI with the data from the element that has been clicked in the HTML page.
     *
     * @param slideNumber The slide number of the slide that has been clicked in the HTML page
     * @param field The field of the slide that has been clicked in the HTML page.
     * @param currentElementContent The current content of the element clicked in the HTML page.
     */
    public void prefillContentDefinition(String slideNumber, String field, String currentElementContent) {
        this.slideNumber.setText(slideNumber);
        this.fieldName.setText(field);

        final Slide slide = this.presentationEngine.getConfiguration().getSlideByNumber(slideNumber);

        if (slide != null) {
            final SlideElement element = slide.getElement(slideNumber + "-" + field);

            /**
             * Prefill the content either with the element's content if it is not null, either with the given
             * <code>currentElementContent</code>.
             */
            if (element != null) {
                /**
                 * Prefill the content with either the original content is it is still supported either
                 * the HTML content.
                 */
                if (MarkupManager.isContentSupported(element.getOriginalContentCode())) {
                    this.contentEditor.setContentEditorValue(element.getOriginalContent());
                } else {
                    this.contentEditor.setContentEditorValue(element.getHtmlContent());
                }

                this.selectMarkupRadioButton(element.getOriginalContentCode());
            } else {
                try {
                    this.contentEditor.setContentEditorValue(new String(Base64.getDecoder().decode(currentElementContent), "UTF8"));
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.WARNING, "Can not decode String in UTF8", e);
                } finally {
                    this.selectMarkupRadioButton(null);
                    this.contentEditor.selectAll();
                }
            }

            this.contentEditor.requestFocus();
        } else {
            LOGGER.info(String.format("Prefill information for the field %1$s of slide #%2$s is impossible: the slide is not found", field, slideNumber));
        }
    }

    /**
     * This method is called by the presentation in order to execute a code snippet. The executor is identified by the
     * {@code snippetExecutorCode} and retrieved in the OSGi context to get the {@link com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor}
     * instance that will execute the code.
     * The code to execute is passed to this method in Base64 using the {@code base64CodeSnippet} parameter. The execution
     * result will be pushed back to the presentation in the HTML element {@code consoleOutputId}.
     *
     * @param snippetExecutorCode The unique identifier of the executor that will execute the code.
     * @param base64CodeSnippet The code snippet to execute, given in Base64.
     * @param consoleOutputId The HTML element that will be updated with the execution result.
     */
    public void executeCodeSnippet(final String snippetExecutorCode, final String base64CodeSnippet, final String consoleOutputId) {

        if(snippetExecutorCode != null) {
            final Optional<ISnippetExecutor> snippetExecutor = OSGiManager.getInstalledServices(ISnippetExecutor.class)
                    .stream()
                    .filter(executor -> snippetExecutorCode.equals(executor.getCode()))
                    .findFirst();

            if(snippetExecutor.isPresent()) {
                try {
                    final CodeSnippet codeSnippetDecoded = CodeSnippet.toObject(new String(Base64.getDecoder().decode(base64CodeSnippet), "UTF8"));
                    final ObservableList<String> consoleOutput = snippetExecutor.get().execute(codeSnippetDecoded);

                    consoleOutput.addListener((ListChangeListener<String>) change -> {
                        // Push the execution result to the presentation.
                        PlatformHelper.run(() -> {
                            while (change.next()) {
                                if (change.wasAdded()) {
                                    change.getAddedSubList()
                                            .stream()
                                            .forEach(line -> this.browser.updateCodeSnippetConsole(consoleOutputId, line));
                                }
                            }
                            change.reset();
                        });
                    });
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.SEVERE, "Can not decode code snippet", e);
                }
            }
        }
    }

    /**
     * Defines the parent controller of this one. This is useful if this controller is created by another one.
     * @param controller The parent controller of this one.
     */
    public void setParent(final Initializable controller) {
        if(controller != null && controller instanceof SlideshowFXController) {
            this.parent = (SlideshowFXController) controller;
        }
    }

    /**
     * Creates a RadioButton for the given markup so the user will be able to select the new syntax. The RadioButton is
     * added to the panel of markups as well as in the ToggleGroup for all markups.
     * Note that the RadioButton will not request focus when it is clicked. This avoid the cursor to leave an eventual
     * text edition area.
     * @param markup The markup to create the RadioButton for
     * @return The created RadioButton.
     */
    private RadioButton createRadioButtonForMakup(IMarkup markup) {
        final RadioButton button = new RadioButton(markup.getName()) {
            @Override
            public void requestFocus() {
                // Avoid the button to get the focus. So if the cursor is in the editor it won't loose the focus
            }
        };
        button.setUserData(markup);

        markupContentType.getToggles().add(button);
        markupContentTypeBox.getChildren().add(button);

        return button;
    }

    /**
     * Creates a Button for the given content extension so the user will be able to insert new type of content in a slide.
     * The Button is added to the ToolBar of content extensions.
     * @param contentExtension The content extension to create the Button for.
     * @return The created Button.
     */
    private Button createButtonForContentExtension(final IContentExtension contentExtension) {
        final Button button = new Button();
        button.setUserData(contentExtension);
        button.setTooltip(new Tooltip(contentExtension.getToolTip()));
        button.getStyleClass().add("image");

        final GlyphIcon icon = new FontAwesomeIconView();
        icon.setGlyphName(contentExtension.getIcon().name());
        icon.setGlyphSize(20);
        icon.setGlyphStyle("-fx-fill: app-color-orange;");

        button.setGraphic(icon);

        button.setOnAction(event -> {

            final ButtonType response = DialogHelper.showCancellableDialog(contentExtension.getTitle(), contentExtension.getUI());

            if (response != null && response == ButtonType.OK) {
                final String content = contentExtension.buildContentString(this.markupContentType.getSelectedToggle() != null ?
                        (IMarkup) this.markupContentType.getSelectedToggle().getUserData() :
                        null);

                if (content != null) {
                    this.contentEditor.appendContentEditorValue(content);
                    contentExtension.extractResources(this.presentationEngine.getTemplateConfiguration().getResourcesDirectory());

                    contentExtension.getResources()
                            .stream()
                            .forEach(resource -> this.presentationEngine.addCustomResource(resource));
                }
            }
        });

        this.contentExtensionToolBar.getItems().add(button);

        return button;
    }

    /**
     * Select the RadioButton corresponding to the given <code>contentCode</code>. If the <code>contentCode</code> is null,
     * every RadioButton is unselected.
     *
     * @param contentCode
     */
    private void selectMarkupRadioButton(final String contentCode) {
        // Clear the current selection
        this.markupContentTypeBox.getChildren()
                .stream()
                .filter(child -> child instanceof RadioButton)
                .map(child -> (RadioButton) child)
                .forEach(button -> button.setSelected(false));

        Optional<RadioButton> radioButton = this.markupContentTypeBox.getChildren()
                .stream()
                .filter(child -> child instanceof RadioButton)
                .map(child -> (RadioButton) child)
                .filter(button -> ((IMarkup) button.getUserData()).getCode().equals(contentCode))
                .findFirst();

        if (radioButton.isPresent()) radioButton.get().setSelected(true);
    }

    /**
     * Refresh the part of the view that allows to choose a markup syntax to reflect the currently installed plugin.
     */
    public void refreshMarkupSyntax() {
        // Clear already present markups
        final Iterator<Node> it = this.markupContentTypeBox.getChildren().iterator();
        Node child;

        while(it.hasNext()) {
            child = it.next();

            if(child instanceof RadioButton) it.remove();
        }

        // Creating RadioButtons for each markup bundle installed
        MarkupManager.getInstalledMarkupSyntax().stream()
                .sorted((markup1, markup2) -> markup1.getName().compareToIgnoreCase(markup2.getName()))
                .forEach(markup -> createRadioButtonForMakup(markup));
    }

    /**
     * Refresh the UI in order to display all content extensions that are installed on the system.
     */
    public void refreshContentExtensions() {
        final Iterator<Node> iterator = this.contentExtensionToolBar.getItems().iterator();
        Node child;

        while(iterator.hasNext()) {
            child = iterator.next();

            if(child instanceof Button && child.getUserData() instanceof IContentExtension) iterator.remove();
        }

        // Creating Buttons for each extension bundle installed
        OSGiManager.getInstalledServices(IContentExtension.class)
                .stream()
                .sorted((extension1, extension2) -> extension1.getCode().compareTo(extension2.getCode()))
                .forEach(extension -> createButtonForContentExtension(extension));
    }

    /**
     * Indicates if the presentation has already been saved by testing if the {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#getArchive()}
     * method returns {@code null} or not.
     * @return {@code true} if {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#getArchive()} is not
     * {@code null}, {@code false} otherwise.
     */
    public boolean isPresentationAlreadySaved() {
        return this.presentationEngine.getArchive() != null;
    }

    /**
     * Get the archive file of this presentation if it has been defined.
     * @return The value returned by {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#getArchive()}
     */
    public File getArchiveFile() {
        return this.presentationEngine.getArchive();
    }

    /**
     * Save the presentation hosted in this view to the given {@code archiveFile}. The process for
     * saving the presentation is only started if the given {@code archiveFile} is not {@code null}. If the process is
     * started, the given {@code archiveFile} is set as archive to this {@link #presentationEngine} using
     * {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#setArchive(java.io.File)}. Then a
     * {@link com.twasyl.slideshowfx.concurrent.SavePresentationTask} is instantiated and started.
     * @param archiveFile The file to save the presentation in.
     * @throws java.io.IOException If an error occurs when save the file.
     */
    public void savePresentation(File archiveFile) throws IOException {
        if(archiveFile != null) {
            this.presentationEngine.setArchive(archiveFile);
            this.presentationEngine.saveArchive();
            PlatformHelper.run(() -> ((SimpleBooleanProperty) this.presentationModified).set(false));
        }
    }

    /**
     * This method refreshed the browser displaying the presentation.
     */
    public void reloadPresentationBrowser() {
        this.browser.reload();
    }

    /**
     * Loads the presentation file in the browser displaying it. If the presentation fil is {@code null} or does not
     * exists, nothing if done.
     */
    public void loadPresentationInBrowser() {
        if(this.presentationEngine.getConfiguration().getPresentationFile() != null
                && this.presentationEngine.getConfiguration().getPresentationFile().exists()) {
            this.browser.loadPresentation(this.presentationEngine);
        }
    }

    /**
     * Defines the presentation for the given view and load it in the browser.
     * @param presentation The presentation associated to the view.
     * @throws java.lang.NullPointerException If {@code presentation} is {@code null}.
     */
    public void  definePresentation(final PresentationEngine presentation) {
        if(presentation == null) throw new NullPointerException("The presentation can not be null");

        this.presentationEngine = presentation;
        this.loadPresentationInBrowser();

        try {
            final JavaBeanObjectProperty<File> archiveFile = new JavaBeanObjectPropertyBuilder<>()
                    .bean(this.presentationEngine)
                    .getter("getArchive")
                    .setter("setArchive")
                    .name("archiveFile")
                    .build();

            ((SimpleStringProperty) this.presentationName).bind(new FilenameBinding(archiveFile));

            /*
             * Determine if the defined presentation is opened from a template or an existing presentation in order to
             * indicate if it is considered as modified.
             */
            ((SimpleBooleanProperty) this.presentationModified).set(this.presentationEngine.getArchive() == null ? true : false);
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE, "Can not create the property for the name of the presentation");
        }
    }

    /**
     * Get the presentation name. This will typically be the name of the {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#getArchive()}
     * object, or "Untitled" if it doesn't exist.
     * @return The name of this presentation.
     */
    public ReadOnlyStringProperty getPresentationName() { return this.presentationName; }

    /**
     * Indicates if the presentation has been modified since the latest time it has been saved.
     *
     * @return The property indicating if the presentation has been modified since the latest save.
     */

    public ReadOnlyBooleanProperty presentationModifiedProperty() { return presentationModified; }

    /**
     * Indicates if the presentation has been modified since the latest time it has been saved.
     *
     * @return {@code true} If the presentation has been modified but not saved, {@code false} is no modifications have
     * been made on the presentation since the latest save.
     */
    public boolean isPresentationModified() { return presentationModified.get(); }

    /**
     * Get the slide number of the slide currently displayed.
     * @return The slide number of the current displayed slide or {@code null} if no slide is displayed.
     */
    public String getCurrentSlideNumber() {
        String slideNumber = null;
        final String slideId = this.getCurrentSlideId();

        if(slideId != null && !slideId.isEmpty()) {
            slideNumber = slideId.substring(this.presentationEngine.getTemplateConfiguration().getSlideIdPrefix().length());
        }

        return slideNumber;
    }

    /**
     * Get the ID of the slide currently displayed.
     * @return The ID of the slide currently displayed or {@code null} if no slide is displayed.
     */
    public String getCurrentSlideId() {
        return this.browser.getCurrentSlideId();
    }

    /**
     * Get the {@link Slide}
     * of the current displayed slide.
     * @return The {@link Slide} of the
     * current displayed slide or {@code null} if no slide is displayed.
     */
    public Slide getCurrentSlidePresentationConfiguration() {
        Slide configuration = null;
        final String slideId = this.getCurrentSlideId();

        if(slideId != null && !slideId.isEmpty()) {
            configuration = this.presentationEngine.getConfiguration().getSlideById(slideId);
        }

        return configuration;
    }

    /**
     * Add a slide after the current slide displayed. The created slide will be defined by the given {@code template}.
     * @param template The template used to create the slide.
     * @throws java.lang.NullPointerException If the {@code template} is null.
     * @throws java.io.IOException If an error occured when adding a slide.
     */
    public void addSlide(SlideTemplate template) throws IOException {
        if(template == null) throw new NullPointerException("The template can not be null");

        this.presentationEngine.addSlide(template, this.getCurrentSlideNumber());
    }

    /**
     * Moves a slide before another.
     * @param slideToMove The slide to move.
     * @param beforeSlide The slide where {@code slideToMove} will be placed before.
     * @throws java.lang.NullPointerException If {@code slideToMove} is {@code null}
     */
    public void moveSlide(Slide slideToMove, Slide beforeSlide) {
        if(slideToMove == null) throw new NullPointerException("The slide to move can not be null");

        this.presentationEngine.moveSlide(slideToMove, beforeSlide);
    }

    /**
     * Copy the current displayed slide. If no slide is present, nothing is performed.
     */
    public void copyCurrentSlide() {
        final String slideId = this.getCurrentSlideId();

        if(slideId != null && !slideId.isEmpty()) {
            final Slide slideToCopy = this.presentationEngine.getConfiguration().getSlideById(slideId);

            if(slideToCopy != null) {
                try {
                    this.presentationEngine.duplicateSlide(slideToCopy);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Can not duplicate the slide", e);
                }
            }
        }
    }

    /**
     * Delete the current displayed slide. If no slide is present, nothing is performed.
     */
    public void deleteCurrentSlide() {
        final ButtonType response = DialogHelper.showConfirmationAlert("Delete slide", "Are you sure you want to delete the current slide?");

        if(response != null && response == ButtonType.YES) {
            final String slideNumber = this.getCurrentSlideNumber();

            if(slideNumber != null && !slideNumber.isEmpty()) {
                this.presentationEngine.deleteSlide(slideNumber);
            }
        }
    }

    /**
     * Get the working directory of this presentation.
     * @return The file representing the working directory of this presentation.
     */
    public File getWorkingDirectory() {
        return this.presentationEngine.getWorkingDirectory();
    }

    /**
     * Get the HTML presentation file of the current presentation.
     * @return The file pointing to the HTML presentation file.
     */
    public File getPresentationFile() {
        return this.presentationEngine.getConfiguration().getPresentationFile();
    }

    /**
     * Get the slide for this presentation.
     * @return An array containing all slides, {@code null} if no slides are found.
     */
    public Slide[] getSlides() {
        Slide[] slides = null;

        if(this.presentationEngine != null
            && this.presentationEngine.getConfiguration() != null
            && this.presentationEngine.getConfiguration().getSlides() != null
            && !this.presentationEngine.getConfiguration().getSlides().isEmpty()) {
            slides = this.presentationEngine.getConfiguration().getSlides().toArray(new Slide[0]);
        }

        return slides;
    }

    /**
     * Get the slide templates for this presentation.
     * @return An array containing all templates, {@code null} if no templates are found.
     */
    public SlideTemplate[] getSlideTemplates() {
        SlideTemplate[] templates = null;

        if(this.presentationEngine != null
                && this.presentationEngine.getTemplateConfiguration() != null
                && this.presentationEngine.getTemplateConfiguration().getSlideTemplates() != null
                && !this.presentationEngine.getTemplateConfiguration().getSlideTemplates().isEmpty()) {
            templates = this.presentationEngine.getTemplateConfiguration().getSlideTemplates().toArray(new SlideTemplate[0]);
        }

        return templates;
    }

    /**
     * Print the current presentation.
     */
    public void printPresentation() {
        this.browser.print();
    }

    /**
     * Start the slideshow for the current presentation. The slideshow is only started if:
     * <ul>
     *     <li>{@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#getConfiguration()} returns a non
     *     null value</li>
     *     <li>{@link com.twasyl.slideshowfx.engine.presentation.configuration.PresentationConfiguration#getPresentationFile()}
     *     returns a non null value and a file that exists</li>
     * </ul>
     * @param leapMotionEnabled Indicates if the LeapMotion controller should be enabled during the slideshow.
     */
    public void startSlideshow(final boolean leapMotionEnabled, final String fromSlideId) {
        if (this.presentationEngine.getConfiguration() != null
                && this.presentationEngine.getConfiguration().getPresentationFile() != null
                && this.presentationEngine.getConfiguration().getPresentationFile().exists()) {

            final Context context = new Context();
            context.setLeapMotionEnabled(leapMotionEnabled);
            context.setStartAtSlideId(fromSlideId);
            context.setPresentation(this.presentationEngine);

            final SlideshowStage stage = new SlideshowStage(context);

            stage.show();
        }
    }

    /**
     * Set the presentation contained in this controller as default presentation by calling
     * {@link com.twasyl.slideshowfx.dao.PresentationDAO#setCurrentPresentation(com.twasyl.slideshowfx.engine.presentation.PresentationEngine)}.
     * If the current presentation is {@code null}, nothing is performed.
     */
    public void setAsDefault() {
        if(this.presentationEngine != null) {
            PresentationDAO.getInstance().setCurrentPresentation(this.presentationEngine);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Make this controller available to JavaScript
        this.browser.setPresentation(this.presentationEngine);
        this.browser.setBackend(this);

        this.refreshMarkupSyntax();

        // Creating buttons for each content extension bundle installed
        OSGiManager.getInstalledServices(IContentExtension.class)
                .stream()
                .sorted((contentExtension1, contentExtension2) -> contentExtension1.getCode().compareTo(contentExtension2.getCode()))
                .forEach(contentExtension -> createButtonForContentExtension(contentExtension));

        // Change the mode for the content editor as the selection for markup language changes
        this.markupContentType.selectedToggleProperty().addListener((value, oldToggle, newToggle) -> {
            if (newToggle == null) {
                this.contentEditor.setMode(null);
            } else {
                this.contentEditor.setMode(((IMarkup) newToggle.getUserData()).getAceMode());
            }
        });

        this.defineContent.disableProperty().bind(this.slideNumber.textProperty().isEmpty()
                .or(this.fieldName.textProperty().isEmpty())
                .or(this.markupContentType.selectedToggleProperty().isNull()));
    }
}
package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.controls.PresentationBrowser;
import com.twasyl.slideshowfx.controls.PresentationVariablesPanel;
import com.twasyl.slideshowfx.controls.SlideContentEditor;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.engine.presentation.Presentations;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import com.twasyl.slideshowfx.engine.presentation.configuration.SlideElement;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
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
        this.updateSlide();
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
     * This method updates a slide of the presentation. The <code>markup</code> and the <code>originalContent</code> are
     * deduced from the user interface. If all parameters can be deduced, then {@link #updateSlide(IMarkup, String)} is
     * called, otherwise nothing is performed.
     * @throws TransformerException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private void updateSlide() throws TransformerException, IOException, ParserConfigurationException, SAXException {
        RadioButton selectedMarkup = (RadioButton) this.markupContentType.getSelectedToggle();

        if(selectedMarkup != null) {
            this.updateSlide((IMarkup) selectedMarkup.getUserData(), this.contentEditor.getContentEditorValue());
        }
    }

    /**
     * This method updates a slide of the presentation. It takes the <code>markup</code> to convert the <code>originalContent</code>
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

        this.presentationEngine.setModifiedSinceLatestSave(true);
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
                final String decodedContent = new String(Base64.getDecoder().decode(currentElementContent), GlobalConfiguration.getDefaultCharset());
                this.contentEditor.setContentEditorValue(decodedContent);
                this.selectMarkupRadioButton(null);
                this.contentEditor.selectAll();

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
                final String decodedString = new String(Base64.getDecoder().decode(base64CodeSnippet), GlobalConfiguration.getDefaultCharset());
                final CodeSnippet codeSnippetDecoded = CodeSnippet.toObject(decodedString);
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
            }
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

            final JavaBeanObjectProperty<Boolean> presentationModifiedSinceLatestSave = new JavaBeanObjectPropertyBuilder<>()
                    .bean(this.presentationEngine)
                    .getter("isModifiedSinceLatestSave")
                    .setter("setModifiedSinceLatestSave")
                    .name("modifiedSinceLatestSave")
                    .build();

            ((SimpleBooleanProperty) this.presentationModified).bind(presentationModifiedSinceLatestSave);
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE, "Can not create the property for the name of the presentation", e);
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
     * Go to a specific slide ID. If the given ID is {@code null} or empty, nothing will be performed.
     * @param slideId The ID of the slide to go to.
     */
    public void goToSlide(final String slideId) {
        if(slideId != null && !slideId.isEmpty()) {
            this.browser.slide(slideId);
        }
    }

    /**
     * Print the current presentation.
     */
    public void printPresentation() {
        this.browser.print();
    }

    /**
     * Set the presentation displayed in this view as the one currently displayed.
     */
    public void setAsCurrentPresentation() {
        Presentations.setCurrentDisplayedPresentation(this.presentationEngine);
    }

    /**
     * Get the presentation associated to this view.
     * @return The presentation associated to this view.
     */
    public PresentationEngine getPresentation() {
        return this.presentationEngine;
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

        // Add a shortcut to the content editor for defining the content using META + Enter
        this.contentEditor.registerEvent(KeyEvent.KEY_PRESSED, event -> {
            if (event.isShortcutDown() && KeyCode.ENTER.equals(event.getCode())) {
                event.consume();
                try {
                    this.updateSlide();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Can not define content", e);
                }
            }
        });
    }
}
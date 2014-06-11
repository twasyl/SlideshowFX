/*
 * Copyright 2014 Thierry Wasylczenko
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

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.builder.PresentationBuilder;
import com.twasyl.slideshowfx.builder.Slide;
import com.twasyl.slideshowfx.builder.SlideElement;
import com.twasyl.slideshowfx.builder.template.SlideTemplate;
import com.twasyl.slideshowfx.chat.Chat;
import com.twasyl.slideshowfx.controls.SlideMenuItem;
import com.twasyl.slideshowfx.controls.SlideShowScene;
import com.twasyl.slideshowfx.exceptions.InvalidPresentationConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateException;
import com.twasyl.slideshowfx.exceptions.PresentationException;
import com.twasyl.slideshowfx.io.SlideshowFXExtensionFilter;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.MarkupManager;
import com.twasyl.slideshowfx.utils.NetworkUtils;
import com.twasyl.slideshowfx.utils.OSGiManager;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import netscape.javascript.JSObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  This class is the controller of the <code>Slideshow.fxml</code> file. It defines all actions possible inside the view
 *  represented by the FXML.
 *  
 *  @author Thierry Wasyczenko
 *  @version 1.0
 *  @since 1.0
 */
public class SlideshowFXController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(SlideshowFXController.class.getName());

    private final PresentationBuilder builder = new PresentationBuilder();

    private final EventHandler<ActionEvent> addSlideActionEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            try {

                Object userData = ((MenuItem) actionEvent.getSource()).getUserData();
                if (userData instanceof SlideTemplate) {
                    final String slideId = (String) SlideshowFXController.this.browser.getEngine().executeScript(SlideshowFXController.this.builder.getTemplate().getGetCurrentSlideMethod() + "();");
                    String slideNumber = null;

                    if (slideId != null && !slideId.isEmpty()) {
                        slideNumber = slideId.substring(SlideshowFXController.this.builder.getTemplate().getSlideIdPrefix().length());
                    }

                    SlideshowFXController.this.builder.addSlide((SlideTemplate) userData, slideNumber);
                    SlideshowFXController.this.browser.getEngine().reload();

                    SlideshowFXController.this.updateSlideSplitMenu();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private final EventHandler<ActionEvent> moveSlideActionEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            final SlideMenuItem menunItem = (SlideMenuItem) actionEvent.getSource();

            final String slideId = (String) SlideshowFXController.this.browser.getEngine()
                    .executeScript(SlideshowFXController.this.builder.getTemplate().getGetCurrentSlideMethod() + "();");

            Slide slideToMove = SlideshowFXController.this.builder.getPresentation().getSlideById(slideId);
            Slide beforeSlide = menunItem.getSlide();

            SlideshowFXController.this.builder.moveSlide(slideToMove, beforeSlide);

            SlideshowFXController.this.browser.getEngine().reload();
            SlideshowFXController.this.updateSlideSplitMenu();
        }
    };

    @FXML
    private WebView browser;
    @FXML
    private SplitMenuButton saveButton;
    @FXML
    private SplitMenuButton addSlideButton;
    @FXML
    private SplitMenuButton moveSlideButton;
    @FXML
    private TextField slideNumber;
    @FXML
    private TextField fieldName;
    @FXML
    private HBox markupContentTypeBox;
    @FXML
    private ToggleGroup markupContentType;
    @FXML
    private TextArea fieldValueText;
    @FXML
    private TextField chatIpAddress;
    @FXML
    private TextField chatPort;
    @FXML
    private TextField twitterHashtag;
    @FXML private Button startChatButton;
    @FXML
    private CheckBox leapMotionEnabled;
    @FXML
    private Button defineContent;

    /**
     * Loads a SlideshowFX template. This method displays an open dialog which only allows to open template files (with
     * .sfxt extension) and then call the {@link #openTemplateOrPresentation(java.io.File)} method.
     *
     * @param event the event that triggered the call.
     */
    @FXML
    private void loadTemplate(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.TEMPLATE_FILTER);
        File templateFile = chooser.showOpenDialog(null);

        if (templateFile != null) {
            try {
                this.openTemplateOrPresentation(templateFile);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Open a SlideshowFX presentation. This method displays an open dialog which only allows to open presentation files
     * (with the .sfx extension) and then call {@link #openTemplateOrPresentation(java.io.File)} method.
     *
     * @param event the event that triggered the call.
     */
    @FXML
    private void openPresentation(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PRESENTATION_FILES);
        File file = chooser.showOpenDialog(null);

        if (file != null) {
            try {
                this.openTemplateOrPresentation(file);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Open the dataFile. If the name ends with <code>.sfx</code> the file is considered as a presentation,
     * if it ends with <code>.sfxt</code> it is considered as a template.
     *
     * @param dataFile the file corresponding to either a template or a presentation.
     * @throws java.lang.IllegalArgumentException If the file is null.
     * @throws java.io.FileNotFoundException      If dataFile does not exist.
     * @throws java.lang.IllegalAccessException   If the file can not be accessed.
     */
    private void openTemplateOrPresentation(final File dataFile) throws IllegalArgumentException, IllegalAccessException, FileNotFoundException {
        if (dataFile == null) throw new IllegalArgumentException("The dataFile can not be null");
        if (!dataFile.exists()) throw new FileNotFoundException("The dataFile does not exist");
        if (!dataFile.canRead()) throw new IllegalAccessException("The dataFile can not be accessed");

        if (dataFile.getName().endsWith(".sfx")) {
            try {
                this.builder.openPresentation(dataFile);
                this.browser.getEngine().load(this.builder.getPresentation().getPresentationFile().toURI().toASCIIString());

                this.updateSlideTemplatesSplitMenu();
                this.updateSlideSplitMenu();
            } catch (InvalidTemplateConfigurationException e) {
                e.printStackTrace();
            } catch (InvalidTemplateException e) {
                e.printStackTrace();
            } catch (PresentationException e) {
                e.printStackTrace();
            } catch (InvalidPresentationConfigurationException e) {
                e.printStackTrace();
            }
        } else if (dataFile.getName().endsWith(".sfxt")) {
            try {
                this.builder.loadTemplate(dataFile);
                this.browser.getEngine().load(this.builder.getPresentation().getPresentationFile().toURI().toASCIIString());

                this.updateSlideTemplatesSplitMenu();
            } catch (InvalidTemplateException e) {
                e.printStackTrace();
            } catch (InvalidTemplateConfigurationException e) {
                e.printStackTrace();
            } catch (PresentationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is called by the <code>Define</code> button of the FXML. The selected syntax is retrieved as well as the content.
     * The treatment is then delegated to the {@link #updateSlide(com.twasyl.slideshowfx.markup.IMarkup, String)} method.
     *
     * @param event
     * @throws TransformerException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @FXML
    private void updateSlideWithText(ActionEvent event) throws TransformerException, IOException, ParserConfigurationException, SAXException {
        RadioButton selectedMarkup = (RadioButton) this.markupContentType.getSelectedToggle();
        this.updateSlide((IMarkup) selectedMarkup.getUserData(), this.fieldValueText.getText());
    }

    /**
     * This method updates a slide of the presentation. It takes the <code>markup</code> to converte the <code>originalContent</code>
     * in HTML and then the slide element is updated. The presentation is then saved temporary.
     * The content is send to the page by calling the {@link com.twasyl.slideshowfx.builder.template.Template#getContentDefinerMethod()}
     * with the HTLM content converted in Base64.
     * A screenshot of the slide is taken to update the menu of available slides.
     *
     * @param markup
     * @param originalContent
     * @throws TransformerException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private void updateSlide(final IMarkup markup, final String originalContent) throws TransformerException, IOException, ParserConfigurationException, SAXException {
        final String htmlContent = markup.convertAsHtml(originalContent);

        // Update the SlideElement
        final Slide slideToUpdate = this.builder.getPresentation().getSlideByNumber(this.slideNumber.getText());
        slideToUpdate.updateElement(
                this.slideNumber.getText() + "-" + this.fieldName.getText(), markup.getCode(), originalContent, htmlContent
        );

        this.builder.getPresentation().updateSlideInDocument(slideToUpdate);

        this.builder.saveTemporaryPresentation();

        String clearedContent = Base64.getEncoder().encodeToString(htmlContent.getBytes("UTF8"));
        String jsCommand = String.format("%1$s(%2$s, \"%3$s\", '%4$s');",
                this.builder.getTemplate().getContentDefinerMethod(),
                this.slideNumber.getText(),
                this.fieldName.getText(),
                clearedContent);

        this.browser.getEngine().executeScript(jsCommand);

        // Take a thumbnail of the slide
        WritableImage thumbnail = this.browser.snapshot(null, null);
        this.builder.getPresentation().updateSlideThumbnail(this.slideNumber.getText(), thumbnail);

        updateSlideSplitMenu();
    }

    /**
     * Copy the slide, update the menu of available slides and reload the presentation.
     * The copy is delegated to {@link com.twasyl.slideshowfx.builder.PresentationBuilder#duplicateSlide(com.twasyl.slideshowfx.builder.Slide)}.
     *
     * @param event
     */
    @FXML
    private void copySlide(ActionEvent event) {
        final String slideId = (String) this.browser.getEngine().executeScript(this.builder.getTemplate().getGetCurrentSlideMethod() + "();");

        Slide slideToCopy = this.builder.getPresentation().getSlideById(slideId);
        this.builder.duplicateSlide(slideToCopy);

        this.updateSlideSplitMenu();

        this.browser.getEngine().reload();
    }

    /**
     * Delete a slide from the presentation. The deletion is delegated to {@link com.twasyl.slideshowfx.builder.PresentationBuilder#deleteSlide(String)}.
     *
     * @param event
     */
    @FXML
    private void deleteSlide(ActionEvent event) {
        String slideId = this.browser.getEngine().executeScript(this.builder.getTemplate().getGetCurrentSlideMethod() + "();").toString();

        if (slideId != null && !slideId.isEmpty()) {
            String slideNumber = slideId.substring(this.builder.getTemplate().getSlideIdPrefix().length());

            try {
                this.builder.deleteSlide(slideNumber);
                SlideshowFXController.this.browser.getEngine().reload();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Simply releod the presentation by calling {@link javafx.scene.web.WebEngine#reload()}.
     *
     * @param event
     */
    @FXML
    private void reload(ActionEvent event) {
        this.browser.getEngine().reload();
    }

    /**
     * Save the current presentation. If the presentation has never been saved a save dialog is displayed.
     * Then the presentation is saved where the user has chosen or opened the presentation.
     * The saving is delegated to {@link com.twasyl.slideshowfx.builder.PresentationBuilder#savePresentation(java.io.File)}
     *
     * @param event
     */
    @FXML
    private void save(ActionEvent event) {
        File presentationArchive = null;
        if (this.builder.getPresentationArchiveFile() == null) {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PRESENTATION_FILES);
            presentationArchive = chooser.showSaveDialog(null);
        } else {
            presentationArchive = this.builder.getPresentationArchiveFile();
        }

        try {
            this.builder.savePresentation(presentationArchive);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a copy of the existing presentation. A save dialog is displayed to the user.
     * The saving is delegated to {@link com.twasyl.slideshowfx.builder.PresentationBuilder#savePresentation(java.io.File)}.
     *
     * @param event
     */
    @FXML
    private void saveAs(ActionEvent event) {
        File presentationArchive = null;
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PRESENTATION_FILES);
        presentationArchive = chooser.showSaveDialog(null);

        if (presentationArchive != null) {
            try {
                this.builder.setPresentationArchiveFile(presentationArchive);
                this.builder.savePresentation(presentationArchive);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Print the presentation displayed.
     *
     * @param event
     */
    @FXML
    private void print(ActionEvent event) {
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null) {
            if (job.showPrintDialog(null)) {
                this.browser.getEngine().print(job);
                job.endJob();
            } else {
                job.cancelJob();
            }
        }
    }

    @FXML
    private void slideShow(ActionEvent event) {
        if (this.builder.getPresentation() != null
                && this.builder.getPresentation().getPresentationFile() != null
                && this.builder.getPresentation().getPresentationFile().exists()) {
            final WebView slideShowBrowser = new WebView();
            slideShowBrowser.getEngine().load(this.builder.getPresentation().getPresentationFile().toURI().toASCIIString());

            final SlideShowScene subScene = new SlideShowScene(slideShowBrowser);

            SlideshowFX.setSlideShowScene(subScene);
        }
    }

    /**
     * This methods starts the chat when the ENTER key is pressed in the IP address, port number or Twitter's hashtag textfields.
     *
     * @param event
     */
    @FXML private void startChatByKeyPressed(KeyEvent event) {
        if(event.getCode().equals(KeyCode.ENTER)) this.startChat();
    }

    /**
     * This methods starts the chat when the button for starting the chat is clicked.
     *
     * @param event
     */
    @FXML private void startChatByButton(ActionEvent event) {
        this.startChat();
    }

    @FXML
    private void insertImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File imageFile = chooser.showOpenDialog(null);

        if (imageFile != null) {
            File targetFile;
            if (imageFile.exists()) {
                // If the file exists, add a timestamp to the source
                targetFile = new File(this.builder.getTemplate().getResourcesDirectory(), System.currentTimeMillis() + imageFile.getName());
            } else {
                targetFile = new File(this.builder.getTemplate().getResourcesDirectory(), imageFile.getName());
            }

            try {
                Files.copy(imageFile.toPath(), targetFile.toPath());

                // Get the relative path of the resources folder from the template directory
                final Path relativePath = this.builder.getTemplate().getFolder().toPath().relativize(this.builder.getTemplate().getResourcesDirectory().toPath());

                final String imgMarkup = String.format("<img src=\"%1$s/%2$s\" />", relativePath.toString(), targetFile.getName());

                this.insertText(this.fieldValueText, imgMarkup, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void insertQuote(ActionEvent event) {
        String quoteMarkup = "<blockquote></blockquote>";

        this.insertText(this.fieldValueText, quoteMarkup, 12);

        this.fieldValueText.requestFocus();
    }

    @FXML
    private void insertChatQRCode(ActionEvent event) {
        final String qrCode = String.format("<img src=\"http://%1$s:%2$s/images/chatQRCode.png\" />",
                Chat.getIp(), Chat.getPort());


        this.insertText(this.fieldValueText, qrCode, null);

        this.fieldValueText.requestFocus();
    }

    private void insertText(TextInputControl input, String textToInsert, Integer moveCaretPosition) {
        if (!input.getText().isEmpty()) {
            final int currentCarret = input.getCaretPosition();
            final String firstPart = input.getText().substring(0, currentCarret);
            final String secondPart = input.getText().substring(currentCarret);

            input.setText(String.format("%1$s%2$s%3$s", firstPart, textToInsert, secondPart));

            if (moveCaretPosition != null) {
                input.positionCaret(currentCarret + moveCaretPosition);
            } else {
                input.positionCaret(currentCarret + textToInsert.length());
            }
        } else {
            input.setText(textToInsert);

            if (moveCaretPosition != null) {
                input.positionCaret(input.getCaretPosition() + moveCaretPosition);
            } else {
                input.positionCaret(input.getCaretPosition() + textToInsert.length());
            }
        }
    }

    private void updateSlideTemplatesSplitMenu() {
        this.addSlideButton.getItems().clear();
        if (this.builder.getTemplate() != null) {
            MenuItem item;

            for (SlideTemplate slideTemplate : this.builder.getTemplate().getSlideTemplates()) {
                item = new MenuItem();
                item.setText(slideTemplate.getName());
                item.setUserData(slideTemplate);
                item.setOnAction(addSlideActionEvent);
                this.addSlideButton.getItems().add(item);
            }
        }
    }

    private void updateSlideSplitMenu() {
        SlideshowFXController.this.moveSlideButton.getItems().clear();
        SlideMenuItem menuItem;
        for (Slide slide : SlideshowFXController.this.builder.getPresentation().getSlides()) {
            menuItem = new SlideMenuItem(slide);
            menuItem.setOnAction(SlideshowFXController.this.moveSlideActionEvent);
            SlideshowFXController.this.moveSlideButton.getItems().add(menuItem);
        }
    }

    /**
     * Prefill the information coming from the HTML page in the JavaFX UI.
     *
     * @param slideNumber
     * @param field
     * @param currentElementContent
     */
    public void prefillContentDefinition(String slideNumber, String field, String currentElementContent) {
        this.slideNumber.setText(slideNumber);
        this.fieldName.setText(field);

        final Slide slide = this.builder.getPresentation().getSlideByNumber(slideNumber);

        if (slide != null) {
            final SlideElement element = slide.getElements().get(slideNumber + "-" + field);

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
                    this.fieldValueText.setText(element.getOriginalContent());
                } else {
                    this.fieldValueText.setText(element.getHtmlContent());
                }

                this.selectMarkupRadioButton(element.getOriginalContentCode());
            } else {
                try {
                    this.fieldValueText.setText(new String(Base64.getDecoder().decode(currentElementContent), "UTF8"));
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.WARNING, "Can not decode String in UTF8", e);
                } finally {
                    this.selectMarkupRadioButton(null);
                    this.fieldValueText.selectAll();
                }
            }

            this.fieldValueText.requestFocus();
        } else {
            LOGGER.info(String.format("Prefill information for the field %1$s of slide #%2$s is impossible: the slide is not found", field, slideNumber));
        }
    }

    /**
     * Start the chat. This method takes the text entered in the IP, port and Twitter's hashtag fields to start the chat.
     * If no text is entered for the IP address and the port number, the IP address of the computer is used and the port 80 is chosen.
     */
    private void startChat() {
        Image icon;

        if (Chat.isOpened()) {
            Chat.close();

            icon = new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/start.png"));
        } else {
            String ip = this.chatIpAddress.getText();
            if (ip == null || ip.isEmpty()) {
                ip = NetworkUtils.getIP();
            }

            int port = 80;
            if (this.chatPort.getText() != null && !this.chatPort.getText().isEmpty()) {
                try {
                    port = Integer.parseInt(this.chatPort.getText());
                } catch(NumberFormatException ex) {
                    LOGGER.log(Level.WARNING, "Can not parse given chat port, use the default one instead", ex);
                }
            }

            this.chatIpAddress.setText(ip);
            this.chatPort.setText(port + "");

            Chat.create(ip, port, this.twitterHashtag.getText());

            icon = new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/shutdown.png"));
        }

        ((ImageView) this.startChatButton.getGraphic()).setImage(icon);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Make this controller available to JavaScript
        this.browser.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State state, Worker.State state2) {
                if (state2 == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) browser.getEngine().executeScript("window");
                    window.setMember(SlideshowFXController.this.builder.getTemplate().getJsObject(), SlideshowFXController.this);
                }
            }
        });

        this.browser.getEngine().setJavaScriptEnabled(true);

        this.browser.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent dragEvent) {
                Dragboard board = dragEvent.getDragboard();
                boolean dragSuccess = false;

                if (board.hasFiles()) {
                    Optional<File> slideshowFXFile = board.getFiles().stream()
                            .filter(file -> file.getName().endsWith(".sfx") || file.getName().endsWith(".sfxt"))
                            .findFirst();

                    if (slideshowFXFile != null && slideshowFXFile.isPresent()) {
                        try {
                            SlideshowFXController.this.openTemplateOrPresentation(slideshowFXFile.get());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        dragSuccess = true;
                    }
                }

                dragEvent.setDropCompleted(dragSuccess);
                dragEvent.consume();
            }
        });

        this.saveButton.setGraphic(
                new ImageView(
                        new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/save.png"), 20d, 20d, true, true)
                )
        );

        this.addSlideButton.setGraphic(
                new ImageView(
                        new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/add.png"), 20d, 20d, true, true)
                )
        );

        this.moveSlideButton.setGraphic(
                new ImageView(
                        new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/move.png"), 20d, 20d, true, true)
                )
        );

        SlideshowFX.leapMotionAllowedProperty().bind(this.leapMotionEnabled.selectedProperty());
        this.leapMotionEnabled.setSelected(true);

        // Creating RadioButtons for each markup bundle installed
        MarkupManager.getInstalledMarkupSyntax().stream()
                .sorted((markup1, markup2) -> markup1.getName().compareTo(markup2.getName()))
                .forEach(markup -> createRadioButtonForMakup(markup));


        this.defineContent.disableProperty().bind(this.slideNumber.textProperty().isEmpty()
                .or(this.fieldName.textProperty().isEmpty())
                .or(this.markupContentType.selectedToggleProperty().isNull()));
    }

    /**
     * Creates a RadioButton for the given markup so the user will be able to select the new syntax. The RadioButton is
     * added to the panel of markups as well as in the ToggleGroup for all markups.
     * @param markup The markup to create the RadioButton for
     * @return The created RadioButton.
     */
    private RadioButton createRadioButtonForMakup(IMarkup markup) {
        final RadioButton button = new RadioButton(markup.getName());
        button.setUserData(markup);

        markupContentType.getToggles().add(button);
        markupContentTypeBox.getChildren().add(button);

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
     * This method exits the application.
     *
     * @param event
     */
    @FXML private void exitApplication(ActionEvent event) {
        PlatformHelper.run(() -> Platform.exit());
    }

    /**
     * This method shows an open dialog that allows to install plugin.
     *
     * @param event
     */
    @FXML
    private void installPlugin(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PLUGIN_FILES);
        File bundleFile = chooser.showOpenDialog(null);

        if(bundleFile != null) {
            Object service = null;
            try {
                service = OSGiManager.deployBundle(bundleFile);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Can not deploy the plugin", e);
            }

            if(service != null) {
                this.createRadioButtonForMakup((IMarkup) service);
            }
        }
    }
}
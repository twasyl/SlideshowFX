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
import com.twasyl.slideshowfx.concurrent.*;
import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.controls.Dialog;
import com.twasyl.slideshowfx.controls.*;
import com.twasyl.slideshowfx.dao.PresentationDAO;
import com.twasyl.slideshowfx.dao.TaskDAO;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.engine.presentation.configuration.SlideElementConfiguration;
import com.twasyl.slideshowfx.engine.presentation.configuration.SlidePresentationConfiguration;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplateConfiguration;
import com.twasyl.slideshowfx.extension.ContentExtensionManager;
import com.twasyl.slideshowfx.hosting.connector.HostingConnectorManager;
import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
import com.twasyl.slideshowfx.io.SlideshowFXExtensionFilter;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.MarkupManager;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.utils.NetworkUtils;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import com.twasyl.slideshowfx.utils.ZipUtils;
import javafx.application.Platform;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrintQuality;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.*;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  This class is the controller of the <code>Slideshow.fxml</code> file. It defines all actions possible inside the view
 *  represented by the FXML.
 *  
 *  @author Thierry Wasyczenko
 *  @version 1.0
 *  @since SlideshowFX 1.0.0
 */
public class SlideshowFXController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(SlideshowFXController.class.getName());

    private final PresentationEngine presentationEngine = new PresentationEngine();

    private final EventHandler<ActionEvent> addSlideActionEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            try {

                Object userData = ((MenuItem) actionEvent.getSource()).getUserData();
                if (userData instanceof SlideTemplateConfiguration) {
                    final String slideId = (String) SlideshowFXController.this.browser.getEngine().executeScript(SlideshowFXController.this.presentationEngine.getTemplateConfiguration().getGetCurrentSlideMethod() + "();");
                    String slideNumber = null;

                    if (slideId != null && !slideId.isEmpty()) {
                        slideNumber = slideId.substring(SlideshowFXController.this.presentationEngine.getTemplateConfiguration().getSlideIdPrefix().length());
                    }

                    SlideshowFXController.this.presentationEngine.addSlide((SlideTemplateConfiguration) userData, slideNumber);

                    final ReloadPresentationViewTask task = new ReloadPresentationViewTask(SlideshowFXController.this.browser);
                    SlideshowFXController.this.taskInProgress.setCurrentTask(task);
                    TaskDAO.getInstance().startTask(task);

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
                    .executeScript(SlideshowFXController.this.presentationEngine.getTemplateConfiguration().getGetCurrentSlideMethod() + "();");

            SlidePresentationConfiguration slideToMove = SlideshowFXController.this.presentationEngine.getConfiguration().getSlideById(slideId);
            SlidePresentationConfiguration beforeSlide = menunItem.getSlide();

            SlideshowFXController.this.presentationEngine.moveSlide(slideToMove, beforeSlide);

            final ReloadPresentationViewTask task = new ReloadPresentationViewTask(SlideshowFXController.this.browser);
            SlideshowFXController.this.taskInProgress.setCurrentTask(task);
            TaskDAO.getInstance().startTask(task);

            SlideshowFXController.this.updateSlideSplitMenu();
        }
    };

    @FXML private StackPane browserStackPane;
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
    private ToolBar contentExtensionToolBar;
    @FXML
    private ToggleGroup markupContentType = new ToggleGroup();
    @FXML private SlideContentEditor contentEditor;
    @FXML
    private ComboBox<String> chatIpAddress;
    @FXML
    private TextField chatPort;
    @FXML
    private TextField twitterHashtag;
    @FXML private Button startChatButton;
    @FXML
    private CheckBox leapMotionEnabled;
    @FXML
    private Button defineContent;
    @FXML private TaskProgressIndicator taskInProgress;
    @FXML private TextFieldCheckMenuItem autoSaveItem;
    @FXML private Menu uploadersMenu;
    @FXML private Menu downloadersMenu;

    /**
     * Loads a SlideshowFX template. This method displays an open dialog which only allows to open template files (with
     * .sfxt archiveExtension) and then call the {@link #openTemplateOrPresentation(java.io.File)} method.
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
     * (with the .sfx archiveExtension) and then call {@link #openTemplateOrPresentation(java.io.File)} method.
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

        Task loadingTask = null;

        if (dataFile.getName().endsWith(".sfx")) {
            loadingTask = new LoadPresentationTask(this.presentationEngine, dataFile);
        } else if (dataFile.getName().endsWith(".sfxt")) {
            loadingTask = new LoadTemplateTask(this.presentationEngine, dataFile);
        }

        if(loadingTask != null) {
            this.taskInProgress.setCurrentTask(loadingTask);
            loadingTask.stateProperty().addListener((value, oldState, newState) -> {
                if(newState != null && (
                        newState == Worker.State.FAILED ||
                        newState == Worker.State.CANCELLED ||
                        newState == Worker.State.SUCCEEDED)) {
                    this.browser.getEngine().load(this.presentationEngine.getConfiguration().getPresentationFile().toURI().toASCIIString());

                    this.updateSlideTemplatesSplitMenu();
                    this.updateSlideSplitMenu();
                }
            });

            TaskDAO.getInstance().startTask(loadingTask);
        }
    }

    /**
     * Open the current working directory in the file explorer of the system.
     * @param event The event associated to the request
     */
    @FXML
    private void openWorkingDirectory(ActionEvent event) {
        if(this.presentationEngine.getWorkingDirectory() != null && this.presentationEngine.getWorkingDirectory().exists()) {
            if(Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(this.presentationEngine.getWorkingDirectory());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Can not open working directory", e);
                }
            }
        }
    }

    @FXML private void takeTour(ActionEvent event) {
        final Tour tour = new Tour(SlideshowFX.getStage().getScene());
        tour.addStep(new Tour.Step("#menuBar", "The menu bar of SlideshowFX."))
            .addStep(new Tour.Step("#toolBar", "The toolbar gives you access to most commons features of SlideshowFX."))
            .addStep(new Tour.Step("#loadTemplate", "Create a new presentation from a template."))
            .addStep(new Tour.Step("#openPresentation", "Open an already existent presentation."))
            .addStep(new Tour.Step("#saveButton", "Save the current presentation."))
            .addStep(new Tour.Step("#printPresentation", "Print the current presentation."))
            .addStep(new Tour.Step("#addSlideButton", "Add a slide after the current slide in the presentation."))
            .addStep(new Tour.Step("#copySlide", "Copy the current slide after it."))
            .addStep(new Tour.Step("#moveSlideButton", "Move a slide before another."))
            .addStep(new Tour.Step("#deleteSlide", "Delete the current slide."))
            .addStep(new Tour.Step("#reloadPresentation", "Reload the current presentation."))
            .addStep(new Tour.Step("#slideshow", "Enter in the presentation mode."))
            .addStep(new Tour.Step("#leapMotionEnabled", "Enable or disable LeapMotion in the presentation mode. If enabled, you will be able to change slides using your index and middle fingers with a swipe, and show a pointer using your index finder."))
            .addStep(new Tour.Step("#chatIpAddress", "The IP address of the embedded server. If nothing is provided, an automatic IP will be used."))
            .addStep(new Tour.Step("#chatPort", "The port of the embedded server. If nothing is provided, 8080 will be used."))
            .addStep(new Tour.Step("#twitterHashtag", "Look for the given hashtag on Twitter. If left blank, the Twitter service will not be started."))
            .addStep(new Tour.Step("#startChatButton", "Start or stop the embedded server."))
            .addStep(new Tour.Step("#browser", "Your presentation is displayed here."))
            .addStep(new Tour.Step("#contentExtensionToolBar", "Extensions are added here. If you install new ones they will also appear here. An extension provides a feature that adds something to your presentation, like inserting an image."))
            .addStep(new Tour.Step("#markupContentTypeBox", "The syntaxes available to define slides's content are located here. If you install new ones, they will also appear here."))
            .addStep(new Tour.Step("#contentEditor", "It is here that you define the content for a slide."))
            .addStep(new Tour.Step("#defineContent", "Define the content for the given element."))
            .start();
    }

    /**
     * Displays a dialog showing the information about SlideshowFX
     * @param event The source of the event
     */
    @FXML private void displayAbout(ActionEvent event) {

        String appVersion = null;
        try {
            final File file = new File(SlideshowFXController.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            final JarFile jarFile = new JarFile(file);
            final Manifest manifest = jarFile.getManifest();
            final Attributes attrs = manifest.getMainAttributes();

            if(attrs != null) {
                appVersion = attrs.getValue("Implementation-Version");
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Can not get application's version", e);
        }

        final Image logoImage = new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/about.png"));
        final ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/about.png")));
        final Label sfxVersion = new Label(String.format("SlideshowFX version: %1$s", appVersion));
        sfxVersion.setStyle("-fx-text-fill: white;");
        final Label javaVersion = new Label(String.format("Java version: %1$s", System.getProperty("java.version")));
        javaVersion.setStyle("-fx-text-fill: white;");

        final VBox labels = new VBox(10);
        labels.getChildren().setAll(sfxVersion, javaVersion);
        labels.setTranslateX(25);
        labels.setTranslateY(150);

        final StackPane stack = new StackPane(logoView, labels);
        stack.setAlignment(Pos.CENTER);
        stack.setPrefSize(logoImage.getWidth(), logoImage.getHeight());
        stack.setBackground(null);

        final Scene scene = new Scene(stack, null);

        final Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(SlideshowFX.getStage());
        stage.setScene(scene);
        stage.show();

        scene.setOnMouseClicked(mouseEvent -> stage.close());
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
        this.updateSlide((IMarkup) selectedMarkup.getUserData(), this.contentEditor.getContentEditorValue());
    }

    /**
     * This method updates a slide of the presentation. It takes the <code>markup</code> to converte the <code>originalContent</code>
     * in HTML and then the slide element is updated. The presentation is then saved temporary.
     * The content is send to the page by calling the {@link com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration#getContentDefinerMethod()}
     * with the HTML content converted in Base64.
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
        final SlidePresentationConfiguration slideToUpdate = this.presentationEngine.getConfiguration().getSlideByNumber(this.slideNumber.getText());
        slideToUpdate.updateElement(
                this.slideNumber.getText() + "-" + this.fieldName.getText(), markup.getCode(), originalContent, htmlContent
        );

        this.presentationEngine.getConfiguration().updateSlideInDocument(slideToUpdate);

        this.presentationEngine.savePresentationFile();

        String clearedContent = Base64.getEncoder().encodeToString(htmlContent.getBytes("UTF8"));
        String jsCommand = String.format("%1$s(%2$s, \"%3$s\", '%4$s');",
                this.presentationEngine.getTemplateConfiguration().getContentDefinerMethod(),
                this.slideNumber.getText(),
                this.fieldName.getText(),
                clearedContent);

        this.browser.getEngine().executeScript(jsCommand);

        // Take a thumbnail of the slide
        WritableImage thumbnail = this.browser.snapshot(null, null);
        this.presentationEngine.getConfiguration().updateSlideThumbnail(this.slideNumber.getText(), thumbnail);

        updateSlideSplitMenu();
    }

    /**
     * Copy the slide, update the menu of available slides and reload the presentation.
     * The copy is delegated to {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#duplicateSlide(com.twasyl.slideshowfx.engine.presentation.configuration.SlidePresentationConfiguration)}.
     *
     * @param event
     */
    @FXML
    private void copySlide(ActionEvent event) {
        final String slideId = (String) this.browser.getEngine().executeScript(this.presentationEngine.getTemplateConfiguration().getGetCurrentSlideMethod() + "();");

        SlidePresentationConfiguration slideToCopy = this.presentationEngine.getConfiguration().getSlideById(slideId);
        this.presentationEngine.duplicateSlide(slideToCopy);

        this.updateSlideSplitMenu();

        final ReloadPresentationViewTask task = new ReloadPresentationViewTask(this.browser);
        this.taskInProgress.setCurrentTask(task);
        TaskDAO.getInstance().startTask(task);
    }

    /**
     * Delete a slide from the presentation. The deletion is delegated to {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#deleteSlide(String)}.
     *
     * @param event
     */
    @FXML
    private void deleteSlide(ActionEvent event) {
        String slideId = this.browser.getEngine().executeScript(this.presentationEngine.getTemplateConfiguration().getGetCurrentSlideMethod() + "();").toString();

        if (slideId != null && !slideId.isEmpty()) {
            String slideNumber = slideId.substring(this.presentationEngine.getTemplateConfiguration().getSlideIdPrefix().length());

            try {
                this.presentationEngine.deleteSlide(slideNumber);

                final ReloadPresentationViewTask task = new ReloadPresentationViewTask(SlideshowFXController.this.browser);
                SlideshowFXController.this.taskInProgress.setCurrentTask(task);
                TaskDAO.getInstance().startTask(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Simply reload the presentation by calling {@link javafx.scene.web.WebEngine#reload()}.
     *
     * @param event
     */
    @FXML
    private void reload(ActionEvent event) {
        final ReloadPresentationViewTask task = new ReloadPresentationViewTask(SlideshowFXController.this.browser);
        SlideshowFXController.this.taskInProgress.setCurrentTask(task);
        TaskDAO.getInstance().startTask(task);
    }

    /**
     * Save the current presentation. If the presentation has never been saved a save dialog is displayed.
     * Then the presentation is saved where the user has chosen or opened the presentation.
     * The saving is delegated to {@link #savePresentation(java.io.File)}
     *
     * @param event
     */
    @FXML
    private void save(ActionEvent event) {
        File presentationArchive = null;

        if (this.presentationEngine.getArchive() == null) {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PRESENTATION_FILES);
            presentationArchive = chooser.showSaveDialog(null);
        } else presentationArchive = this.presentationEngine.getArchive();

        this.savePresentation(presentationArchive);
    }

    /**
     * Saves a copy of the existing presentation. A save dialog is displayed to the user.
     * The saving is delegated to {@link #savePresentation(java.io.File)}.
     *
     * @param event
     */
    @FXML
    private void saveAs(ActionEvent event) {
        File presentationArchive = null;
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PRESENTATION_FILES);
        presentationArchive = chooser.showSaveDialog(null);

        this.savePresentation(presentationArchive);
    }

    /**
     * Save the presentation hosted in {@link #presentationEngine} to the given {@param archiveFile}. The process for
     * saving the presentation is only started if the given {@param archiveFile} is not {@code null}. If the process is
     * started, the given {@param archiveFile} is set as archive to this {@link #presentationEngine} using
     * {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#setArchive(java.io.File)}. Then a
     * {@link com.twasyl.slideshowfx.concurrent.SavePresentationTask} is instantiated and started.
     * @param archiveFile The file to save the presentation in.
     */
    private void savePresentation(File archiveFile) {
        if(archiveFile != null) {
            this.presentationEngine.setArchive(archiveFile);

            final Task saveTask = new SavePresentationTask(this.presentationEngine);
            this.taskInProgress.setCurrentTask(saveTask);

            TaskDAO.getInstance().startTask(saveTask);
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

                if(this.presentationEngine.getArchive() != null) {
                    final String extension = ".".concat(this.presentationEngine.getArchiveExtension());
                    final int indexOfExtension = this.presentationEngine.getArchive().getName().indexOf(extension);
                    final String jobName = this.presentationEngine.getArchive().getName().substring(0, indexOfExtension);
                    job.getJobSettings().setJobName(jobName);
                }

                job.getJobSettings().setPrintQuality(PrintQuality.HIGH);
                job.getJobSettings().setPageLayout(job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, 0, 0, 0, 0));

                this.browser.getEngine().print(job);
                job.endJob();
            } else {
                job.cancelJob();
            }
        }
    }

    @FXML
    private void slideShow(ActionEvent event) {
        if (this.presentationEngine.getConfiguration() != null
                && this.presentationEngine.getConfiguration().getPresentationFile() != null
                && this.presentationEngine.getConfiguration().getPresentationFile().exists()) {
            final WebView slideShowBrowser = new WebView();

            slideShowBrowser.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                        JSObject window = (JSObject) slideShowBrowser.getEngine().executeScript("window");
                        window.setMember("sfxServer", SlideshowFXServer.getSingleton());
                }
            });

            slideShowBrowser.getEngine().load(this.presentationEngine.getConfiguration().getPresentationFile().toURI().toASCIIString());

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
    private void insertQuizz(ActionEvent event) {
        final QuizzCreatorPanel quizzCreatorPanel = new QuizzCreatorPanel();

        Dialog.Response response = Dialog.showCancellableDialog(true, SlideshowFX.getStage(), "Insert a quizz", quizzCreatorPanel);

        if(response != null && response.equals(Dialog.Response.OK)) {
            this.contentEditor.appendContentEditorValue(quizzCreatorPanel.convertToHtml());
        }
    }

    /**
     * This method is called when a file is dropped on the presentation's browser.
     * It allows to open presentation or template to be opened by drag'n'drop.
     *
     * @param dragEvent The drag event associated to the drag.
     */
    @FXML private void dragDroppedOnBrowser(DragEvent dragEvent) {
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
        PlatformHelper.run(() -> {
            this.browserStackPane.getStyleClass().remove("validDragOver");
            this.browserStackPane.getStyleClass().remove("invalidDragOver");
        });
    }

    /**
     * This method is called when a file is dragged over the presentation's browser.
     * It allows to open presentation or template to be opened by drag'n'drop.
     *
     * @param dragEvent The drag event associated to the drag.
     */
    @FXML private void dragOverBrowser(DragEvent dragEvent) {
        if(dragEvent.getGestureSource() != browser && dragEvent.getDragboard().hasFiles()) {
            /**
             * Check if either a template or a presentation is drag over the browser.
             */
            Optional<File> slideshowFXFile = dragEvent.getDragboard().getFiles().stream()
                    .filter(file -> file.getName().endsWith(".sfx") || file.getName().endsWith(".sfxt"))
                    .findFirst();

            if (slideshowFXFile != null && slideshowFXFile.isPresent()) {
                dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);

                PlatformHelper.run(() ->  {
                    this.browserStackPane.getStyleClass().remove("invalidDragOver");

                    if(!this.browserStackPane.getStyleClass().contains("validDragOver")) {
                        this.browserStackPane.getStyleClass().add("validDragOver");
                    }
                });

            } else {
                PlatformHelper.run(() -> {
                    this.browserStackPane.getStyleClass().remove("validDragOver");

                    if (!this.browserStackPane.getStyleClass().contains("invalidDragOver")) {
                        this.browserStackPane.getStyleClass().add("invalidDragOver");
                    }
                });
            }

            dragEvent.consume();
        }
    }

    /**
     * This method is called the drag exits the presentation's browser
     *
     * @param dragEvent The drag event associated to the drag.
     */
    @FXML private void onDragExitedBrowser(DragEvent dragEvent) {
        PlatformHelper.run(() -> {
            this.browserStackPane.getStyleClass().remove("validDragOver");
            this.browserStackPane.getStyleClass().remove("invalidDragOver");
        });
    }

    /**
     * This method is called in order to create a template from scratch.
     * @param event The event associated to the click that should open the template builder.
     */
    @FXML private void createTemplate(ActionEvent event) {
        final TemplateEngine engine = new TemplateEngine();
        engine.setWorkingDirectory(engine.generateWorkingDirectory());
        engine.getWorkingDirectory().mkdir();

        final File templateConfigurationFile = new File(engine.getWorkingDirectory(), engine.getConfigurationFilename());

        try {
            Files.createFile(templateConfigurationFile.toPath());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can not create template configuration file", e);
        }

        this.showTemplateBuilder(engine);
    }

    /**
     * This method is called in order to open an existing template from scratch.
     * @param event The event associated to the click that should open the template builder.
     */
    @FXML private void openTemplate(ActionEvent event) {
        final FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(SlideshowFXExtensionFilter.TEMPLATE_FILTER);

        final File file = chooser.showOpenDialog(null);
        if(file != null) {
            final TemplateEngine engine = new TemplateEngine();
            engine.setWorkingDirectory(engine.generateWorkingDirectory());
            engine.getWorkingDirectory().mkdir();


            try {
                ZipUtils.unzip(file, engine.getWorkingDirectory());

                this.showTemplateBuilder(engine);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not unzip the template", e);
            }
        }
    }

    /**
     * This method is called in order to edit the current template for the opened presentation.
     * @param event The event associated to the click that should open the template builder.
     */
    @FXML private void editTemplate(ActionEvent event) {
        final TemplateEngine engine = new TemplateEngine();
        engine.setWorkingDirectory(this.presentationEngine.getWorkingDirectory());

        this.showTemplateBuilder(engine);
    }

    /**
     * Show the template builder window.
     * @param engine the engine used for the template builder that will be created.
     */
    private void showTemplateBuilder(final TemplateEngine engine) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/twasyl/slideshowfx/fxml/TemplateBuilder.fxml"));
            final Parent root = loader.load();

            final TemplateBuilderController controller = loader.getController();
            controller.setTemplateEngine(engine);

            final Scene scene = new Scene(root);

            final Stage stage = new Stage();
            stage.initOwner(SlideshowFX.getStage());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setTitle("Template builder");
            stage.getIcons().addAll(
                    new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/16.png")),
                    new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/32.png")),
                    new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/64.png")),
                    new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/128.png")),
                    new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/256.png")),
                    new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/512.png")));

            stage.show();

            controller.setStage(stage);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not load the template builder", e);
        }
    }

    private void updateSlideTemplatesSplitMenu() {
        this.addSlideButton.getItems().clear();
        if (this.presentationEngine.getTemplateConfiguration() != null) {
            MenuItem item;

            for (SlideTemplateConfiguration slideTemplateConfiguration : this.presentationEngine.getTemplateConfiguration().getSlideTemplateConfigurations()) {
                item = new MenuItem();
                item.setText(slideTemplateConfiguration.getName());
                item.setUserData(slideTemplateConfiguration);
                item.setOnAction(addSlideActionEvent);
                this.addSlideButton.getItems().add(item);
            }
        }
    }

    private void updateSlideSplitMenu() {
        SlideshowFXController.this.moveSlideButton.getItems().clear();
        SlideMenuItem menuItem;
        for (SlidePresentationConfiguration slide : SlideshowFXController.this.presentationEngine.getConfiguration().getSlides()) {
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

        final SlidePresentationConfiguration slide = this.presentationEngine.getConfiguration().getSlideByNumber(slideNumber);

        if (slide != null) {
            final SlideElementConfiguration element = slide.getElements().get(slideNumber + "-" + field);

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
     * Start the chat. This method takes the text entered in the IP, port and Twitter's hashtag fields to start the chat.
     * If no text is entered for the IP address and the port number, the IP address of the computer is used and the port 80 is chosen.
     */
    private void startChat() {
        Image icon;

        if (SlideshowFXServer.getSingleton() != null) {
            SlideshowFXServer.getSingleton().stop();

            icon = new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/start.png"));
        } else {
            String ip = this.chatIpAddress.getValue();
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

            this.chatIpAddress.setValue(ip);
            this.chatPort.setText(port + "");

            new SlideshowFXServer(ip, port, this.twitterHashtag.getText());

            icon = new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/shutdown.png"));
        }

        ((ImageView) this.startChatButton.getGraphic()).setImage(icon);
        this.chatIpAddress.setDisable(!this.chatIpAddress.isDisable());
        this.chatPort.setDisable(!this.chatPort.isDisable());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PresentationDAO.getInstance().setCurrentPresentation(this.presentationEngine);

        // Add a listener for auto-saving the presentation
        final ScheduledService<Void> service = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                final SavePresentationTask task = new SavePresentationTask(SlideshowFXController.this.presentationEngine);
                return task;
            }

            @Override
            protected void executeTask(Task<Void> task) {
                super.executeTask(task);
                SlideshowFXController.this.taskInProgress.setCurrentTask(task);
            }
        };
        service.setRestartOnFailure(true);

        this.autoSaveItem.selectedProperty().addListener((value, oldValue, newValue) -> {
            if(!newValue) service.cancel();
            else {
                Integer interval = null;
                try {
                    interval = Integer.parseInt(this.autoSaveItem.getValue());
                } catch(NumberFormatException e) {
                    LOGGER.log(Level.FINE, "Can not parse auto save interval");
                }

                if(interval != null & interval > 0) {
                    service.setDelay(Duration.minutes(interval.doubleValue()));
                    service.setPeriod(Duration.minutes(interval.doubleValue()));
                    service.restart();
                }
            }
        });

        this.autoSaveItem.valueProperty().addListener((value, oldValue, newValue) -> {
            Integer interval = null;
            try {
                interval = Integer.parseInt(this.autoSaveItem.getValue());
            } catch(NumberFormatException e) {
                LOGGER.log(Level.FINE, "Can not parse auto save interval");
            }

            if(interval != null & interval > 0) {
                service.setDelay(Duration.minutes(interval.doubleValue()));
                service.setPeriod(Duration.minutes(interval.doubleValue()));
                service.restart();
            }
        });

        // Ensure the title bar of the application reflects the name of the presentation
        try {
            final JavaBeanObjectProperty<File> archiveFile = new JavaBeanObjectPropertyBuilder<File>()
                    .bean(this.presentationEngine)
                    .getter("getArchive")
                    .setter("setArchive")
                    .name("archiveFile")
                    .build();

            archiveFile.addListener((archiveValue, oldArchive, newArchive) -> {
                PlatformHelper.run(() -> {
                    if(newArchive != null) {
                        SlideshowFX.getStage().setTitle("SlideshowFX - ".concat(newArchive.getName()));
                    } else {
                        SlideshowFX.getStage().setTitle("SlideshowFX - Untitled");
                    }
                });
            });

            SlideshowFX.stageProperty().addListener((stageValue, oldStage, newStage) -> {
                PlatformHelper.run(() -> {
                    if (this.presentationEngine.getArchive() != null) {
                        SlideshowFX.getStage().setTitle("SlideshowFX - ".concat(this.presentationEngine.getArchive().getName()));
                    } else {
                        SlideshowFX.getStage().setTitle("SlideshowFX - Untitled");
                    }
                });
            });
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE, "Can not bind the presentation archive name to the application's title bar", e);
        }


        // Make this controller available to JavaScript
        this.browser.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                if(SlideshowFXController.this.presentationEngine != null
                        && SlideshowFXController.this.presentationEngine.getTemplateConfiguration() != null
                        && SlideshowFXController.this.presentationEngine.getTemplateConfiguration().getJsObject() != null) {
                    JSObject window = (JSObject) browser.getEngine().executeScript("window");
                    window.setMember(SlideshowFXController.this.presentationEngine.getTemplateConfiguration().getJsObject(), SlideshowFXController.this);
                    window.setMember("sfxServer", SlideshowFXServer.getSingleton());
                }
            }
        });

        this.browser.getEngine().setJavaScriptEnabled(true);
        this.browser.getEngine().load(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/html/empty-webview.html"));

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
                .sorted((markup1, markup2) -> markup1.getName().compareToIgnoreCase(markup2.getName()))
                .forEach(markup -> createRadioButtonForMakup(markup));

        // Creating buttons for each content extension bundle installed
        ContentExtensionManager.getInstalledContentExtensions().stream()
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

        // Create the entries in the Upload & Download menus
        HostingConnectorManager.getInstalledHostingConnectors().stream()
                .sorted((hostingConnector1, hostingConnector2) -> hostingConnector1.getName().compareTo(hostingConnector2.getName()))
                .forEach(hostingConnector -> {
                    createUploaderMenuItem(hostingConnector);
                    createDownloaderMenuItem(hostingConnector);
                });
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

        final Image icon = new Image(contentExtension.getIcon());
        final ImageView view = new ImageView(icon);
        view.setFitHeight(20);
        view.setFitWidth(20);

        button.setGraphic(view);

        button.setOnAction(event -> {

            final Dialog.Response response = Dialog.showCancellableDialog(true, SlideshowFX.getStage(), contentExtension.getTitle(), contentExtension.getUI());

            if(response == Dialog.Response.OK) {
                final String content = contentExtension.buildContentString(this.markupContentType.getSelectedToggle() != null ?
                        (IMarkup) this.markupContentType.getSelectedToggle().getUserData() :
                        null);

                if (content != null) {
                    this.contentEditor.appendContentEditorValue(content);
                    contentExtension.extractResources(this.presentationEngine.getTemplateConfiguration().getResourcesDirectory());

                    contentExtension.getResources()
                            .stream()
                            .forEach(resource -> {
                                this.presentationEngine.addCustomResource(resource);
                            });
                }
            }
        });

        this.contentExtensionToolBar.getItems().add(button);

        return button;
    }

    /**
     * Create the MenuItem that will be placed in the menu for uploaders, for the given {@code hostingConnector}. The {@code hostingConnector}
     * is set as user data of the created MenuItem. The created MenuItem is added to the Upload menu.
     * @param hostingConnector The hostingConnector attached to the MenuItem that will be created.
     * @return A MenuItem for the {@code hostingConnector}
     */
    private MenuItem createUploaderMenuItem(final IHostingConnector hostingConnector) {
        final MenuItem uploaderMenuItem = new MenuItem(hostingConnector.getName());
        uploaderMenuItem.setUserData(hostingConnector);

        uploaderMenuItem.setOnAction(event -> {
            PlatformHelper.run(() -> {
                if (!hostingConnector.isAuthenticated()) {
                    hostingConnector.authenticate();
                }

                if (hostingConnector.isAuthenticated()) {
                    // Prompts the user where to upload the presentation
                    final RemoteFile destination = hostingConnector.chooseFile(true, false);

                    if(destination != null) {
                        final UploadPresentationTask task = new UploadPresentationTask(PresentationDAO.getInstance().getCurrentPresentation(),
                                hostingConnector, destination);
                        SlideshowFXController.this.taskInProgress.setCurrentTask(task);
                        TaskDAO.getInstance().startTask(task);
                    }
                }
            });
        });

        this.uploadersMenu.getItems().add(uploaderMenuItem);

        return uploaderMenuItem;
    }

    /**
     * Create the MenuItem that will be placed in the menu for downloaders, for the given {@code hostingConnector}. The {@code hostingConnector}
     * is set as user data of the created MenuItem. The created MenuItem is added to the Download menu.
     * @param hostingConnector The hostingConnector attached to the MenuItem that will be created.
     * @return A MenuItem for the {@code hostingConnector}
     */
    private MenuItem createDownloaderMenuItem(final IHostingConnector hostingConnector) {
        final MenuItem downloaderMenuItem = new MenuItem(hostingConnector.getName());
        downloaderMenuItem.setUserData(hostingConnector);

        downloaderMenuItem.setOnAction(event -> {
            PlatformHelper.run(() -> {
                if (!hostingConnector.isAuthenticated()) {
                    hostingConnector.authenticate();
                }

                if (hostingConnector.isAuthenticated()) {
                    // Prompts the user which file to download
                    final RemoteFile presentationFile = hostingConnector.chooseFile(true, true);

                    if (presentationFile != null) {
                        // Prompts the user where the file should be downloaded
                        final DirectoryChooser chooser = new DirectoryChooser();
                        chooser.setTitle("Choose directory");

                        final File directory = chooser.showDialog(null);

                        if (directory != null) {
                            final DownloadPresentationTask task = new DownloadPresentationTask(
                                    hostingConnector, directory, presentationFile);
                            task.stateProperty().addListener((value, oldState, newState) -> {
                                if(newState == Worker.State.SUCCEEDED && task.getValue() != null) {

                                    final Dialog.Response answer = Dialog.showConfirmDialog(SlideshowFX.getStage(),
                                                    "Open file",
                                                    String.format("Do you want to open '%1$s' ?", task.getValue()));

                                    if(answer == Dialog.Response.YES) {
                                        try {
                                            this.openTemplateOrPresentation(task.getValue());
                                        } catch (IOException | IllegalAccessException e) {
                                            LOGGER.log(Level.SEVERE, "Error when opening file", e);
                                        }
                                    }
                                }
                            });
                            SlideshowFXController.this.taskInProgress.setCurrentTask(task);
                            TaskDAO.getInstance().startTask(task);

                        }
                    }
                }
            });
        });

        this.downloadersMenu.getItems().add(downloaderMenuItem);

        return downloaderMenuItem;
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
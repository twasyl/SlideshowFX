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

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.beans.properties.PresentationModifiedBinding;
import com.twasyl.slideshowfx.concurrent.*;
import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.controls.SlideMenuItem;
import com.twasyl.slideshowfx.controls.TaskProgressIndicator;
import com.twasyl.slideshowfx.controls.TextFieldCheckMenuItem;
import com.twasyl.slideshowfx.controls.Tour;
import com.twasyl.slideshowfx.dao.PresentationDAO;
import com.twasyl.slideshowfx.dao.TaskDAO;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
import com.twasyl.slideshowfx.io.SlideshowFXExtensionFilter;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.utils.*;
import com.twasyl.slideshowfx.utils.concurrent.TaskAction;
import com.twasyl.slideshowfx.utils.concurrent.actions.DisableAction;
import com.twasyl.slideshowfx.utils.concurrent.actions.EnableAction;
import javafx.application.Platform;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

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

    private final EventHandler<ActionEvent> addSlideActionEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            try {

                final PresentationViewController view = SlideshowFXController.this.getCurrentPresentationView();
                final Object userData = ((MenuItem) actionEvent.getSource()).getUserData();

                if (userData instanceof SlideTemplate && view != null) {
                    view.addSlide((SlideTemplate) userData);

                    final ReloadPresentationViewTask task = new ReloadPresentationViewTask(view);
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
            final PresentationViewController view = SlideshowFXController.this.getCurrentPresentationView();

            if(view != null) {
                final SlideMenuItem menunItem = (SlideMenuItem) actionEvent.getSource();
                final Slide slideToMove = view.getCurrentSlidePresentationConfiguration();
                final Slide beforeSlide = menunItem.getSlide();

                view.moveSlide(slideToMove, beforeSlide);

                final ReloadPresentationViewTask task = new ReloadPresentationViewTask(view);
                SlideshowFXController.this.taskInProgress.setCurrentTask(task);
                TaskDAO.getInstance().startTask(task);

                SlideshowFXController.this.updateSlideSplitMenu();
            }

        }
    };

    /* Main ToolBar elements */
    @FXML private SplitMenuButton addSlideButton;
    @FXML private SplitMenuButton moveSlideButton;
    @FXML private ComboBox<String> chatIpAddress;
    @FXML private TextField chatPort;
    @FXML private TextField twitterHashtag;
    @FXML private Button startChatButton;
    @FXML private CheckBox leapMotionEnabled;

    /* Main application UI elements */
    @FXML private TabPane openedPresentationsTabPane;

    /* Main application menu elements */
    @FXML private TextFieldCheckMenuItem autoSaveItem;
    @FXML private Menu uploadersMenu;
    @FXML private Menu downloadersMenu;

    /* Notification center */
    @FXML private TaskProgressIndicator taskInProgress;

    /* List of controls */
    @FXML private ObservableList<Object> saveElementsGroup;
    @FXML private ObservableList<Object> openElementsGroup;
    @FXML private ObservableList<Object> whenNoDocumentOpened;


    /* All methods called by the FXML */

    /**
     * Loads a SlideshowFX template. This method displays an open dialog which only allows to open template files (with
     * .sfxt archiveExtension) and then call the {@link #openTemplateOrPresentation(java.io.File)} method.
     *
     * @param event the event that triggered the call.
     */
    @FXML private void loadTemplate(ActionEvent event) {
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
    @FXML private void openPresentation(ActionEvent event) {
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
     * Close the current displayed presentation.
     * @param event The event that triggered the call.
     */
    @FXML private void closePresentation(ActionEvent event) {
        if(this.openedPresentationsTabPane.getSelectionModel().getSelectedItem() != null) {
            final Tab tab = this.openedPresentationsTabPane.getSelectionModel().getSelectedItem();

            // Fire a close request event on the tab if any EventHandler has been defined
            final EventType<Event> closeRequestEventType = Tab.TAB_CLOSE_REQUEST_EVENT;
            final Event closeRequestEvent = new Event(closeRequestEventType);
            Event.fireEvent(tab, closeRequestEvent);

            // Fire a closed event on the tab if any EventHandler has been defined
            final EventType<Event> closedEventType = Tab.CLOSED_EVENT;
            final Event closedEvent = new Event(closedEventType);
            Event.fireEvent(tab, closedEvent);

            this.openedPresentationsTabPane.getTabs().remove(tab);
        }
    }

    /**
     * This method is called when a file is dropped on the main UI.
     * It allows to open presentation or template to be opened by drag'n'drop.
     *
     * @param dragEvent The drag event associated to the drag.
     */
    @FXML private void dragDroppedOnUI(DragEvent dragEvent) {
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
            this.openedPresentationsTabPane.getStyleClass().remove("validDragOver");
            this.openedPresentationsTabPane.getStyleClass().remove("invalidDragOver");
        });
    }

    /**
     * This method is called when a file is dragged over the main UI.
     * It allows to open presentation or template to be opened by drag'n'drop.
     *
     * @param dragEvent The drag event associated to the drag.
     */
    @FXML private void dragOverUI(DragEvent dragEvent) {
        if(dragEvent.getGestureSource() != this.openedPresentationsTabPane && dragEvent.getDragboard().hasFiles()) {
            /**
             * Check if either a template or a presentation is drag over the browser.
             */
            Optional<File> slideshowFXFile = dragEvent.getDragboard().getFiles().stream()
                    .filter(file -> file.getName().endsWith(".sfx") || file.getName().endsWith(".sfxt"))
                    .findFirst();

            if (slideshowFXFile != null && slideshowFXFile.isPresent()) {
                dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);

                PlatformHelper.run(() ->  {
                    this.openedPresentationsTabPane.getStyleClass().remove("invalidDragOver");

                    if(!this.openedPresentationsTabPane.getStyleClass().contains("validDragOver")) {
                        this.openedPresentationsTabPane.getStyleClass().add("validDragOver");
                    }
                });

            } else {
                PlatformHelper.run(() -> {
                    this.openedPresentationsTabPane.getStyleClass().remove("validDragOver");

                    if (!this.openedPresentationsTabPane.getStyleClass().contains("invalidDragOver")) {
                        this.openedPresentationsTabPane.getStyleClass().add("invalidDragOver");
                    }
                });
            }

            dragEvent.consume();
        }
    }

    /**
     * This method is called the drag exits the main UI.
     *
     * @param dragEvent The drag event associated to the drag.
     */
    @FXML private void dragExitedUI(DragEvent dragEvent) {
        PlatformHelper.run(() -> {
            this.openedPresentationsTabPane.getStyleClass().remove("validDragOver");
            this.openedPresentationsTabPane.getStyleClass().remove("invalidDragOver");
        });
    }

    /**
     * Open the current working directory in the file explorer of the system.
     * @param event The event associated to the request
     */
    @FXML private void openWorkingDirectory(ActionEvent event) {
        final PresentationViewController view = this.getCurrentPresentationView();

        if(view != null) {
            final File workingDir = view.getWorkingDirectory();

            if(workingDir != null && workingDir.exists()) {
                if(Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(workingDir);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Can not open working directory", e);
                    }
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
     * Copy the slide, update the menu of available slides and reload the presentation.
     * The copy is delegated to {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#duplicateSlide(Slide)}.
     *
     * @param event
     */
    @FXML private void copySlide(ActionEvent event) {
        final PresentationViewController view = this.getCurrentPresentationView();

        if(view != null) {
            view.copyCurrentSlide();
            this.updateSlideSplitMenu();

            final ReloadPresentationViewTask task = new ReloadPresentationViewTask(view);
            this.taskInProgress.setCurrentTask(task);
            TaskDAO.getInstance().startTask(task);
        }
    }

    /**
     * Delete a slide from the presentation. The deletion is delegated to {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#deleteSlide(String)}.
     *
     * @param event
     */
    @FXML private void deleteSlide(ActionEvent event) {
        final PresentationViewController view = this.getCurrentPresentationView();

        if(view != null) {
            view.deleteCurrentSlide();

            final ReloadPresentationViewTask task = new ReloadPresentationViewTask(view);
            SlideshowFXController.this.taskInProgress.setCurrentTask(task);
            TaskDAO.getInstance().startTask(task);
        }
    }

    /**
     * Simply reload the presentation by calling {@link javafx.scene.web.WebEngine#reload()}.
     *
     * @param event
     */
    @FXML private void reload(ActionEvent event) {
        final PresentationViewController view = this.getCurrentPresentationView();

        if(view != null) {
            final ReloadPresentationViewTask task = new ReloadPresentationViewTask(view);
            SlideshowFXController.this.taskInProgress.setCurrentTask(task);
            TaskDAO.getInstance().startTask(task);
        }
    }

    /**
     * Save the current presentation. If the presentation has never been saved a save dialog is displayed.
     * Then the presentation is saved where the user has chosen or opened the presentation.
     * The saving is delegated to {@link #savePresentation(java.io.File)}
     *
     * @param event
     */
    @FXML private void save(ActionEvent event) {

        final PresentationViewController view = this.getCurrentPresentationView();

        if(view != null) {
            File presentationArchive = null;

            if (!view.isPresentationAlreadySaved()) {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PRESENTATION_FILES);
                presentationArchive = chooser.showSaveDialog(SlideshowFX.getStage());
            } else presentationArchive = view.getArchiveFile();

            this.savePresentation(presentationArchive);
        }
    }

    /**
     * Saves a copy of the existing presentation. A save dialog is displayed to the user.
     * The saving is delegated to {@link #savePresentation(java.io.File)}.
     *
     * @param event
     */
    @FXML private void saveAs(ActionEvent event) {
        File presentationArchive = null;
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PRESENTATION_FILES);
        presentationArchive = chooser.showSaveDialog(SlideshowFX.getStage());

        this.savePresentation(presentationArchive);
    }

    /**
     * Print the current presentation displayed.
     *
     * @param event
     */
    @FXML private void print(ActionEvent event) {
        final PresentationViewController view = this.getCurrentPresentationView();

        if(view != null) view.printPresentation();
    }

    @FXML private void slideShow(ActionEvent event) {
        final PresentationViewController view = this.getCurrentPresentationView();

        if(view != null) view.startSlideshow(this.leapMotionEnabled.isSelected(), null);
    }

    @FXML private void slideshowFromCurrentSlide(ActionEvent event) {
        final PresentationViewController view = this.getCurrentPresentationView();

        if(view != null) view.startSlideshow(this.leapMotionEnabled.isSelected(), view.getCurrentSlideId());
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
            engine.setArchive(file);

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
        final PresentationViewController view = this.getCurrentPresentationView();

        if(view != null) {
            final TemplateEngine engine = new TemplateEngine();
            engine.setWorkingDirectory(view.getWorkingDirectory());

            this.showTemplateBuilder(engine);
        }
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
                final Stream<Tab> stream = this.openedPresentationsTabPane.getTabs()
                        .stream()
                        .filter(tab -> tab.getUserData() != null && tab.getUserData() instanceof PresentationViewController);
                if(service instanceof IMarkup) {
                    stream.forEach(tab -> ((PresentationViewController) tab.getUserData()).refreshMarkupSyntax());
                } else if(service instanceof IContentExtension) {
                    stream.forEach(tab -> ((PresentationViewController) tab.getUserData()).refreshContentExtensions());
                }
            }
        }
    }

    /**
     * This method shows a dialog for options of SlideshowFX.
     * @param event
     */
    @FXML private void showOptionsDialog(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(ResourceHelper.getURL("/com/twasyl/slideshowfx/fxml/OptionsVIew.fxml"));
        try {
            final Parent root = loader.load();
            final OptionsViewController controller = loader.getController();

            final ButtonType response = DialogHelper.showCancellableDialog("Options", root);

            if(response != null && response == ButtonType.OK) {
                controller.saveOptions();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not open options view", e);
        }
    }

    /* All instance methods */

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

        final Task<PresentationEngine> loadingTask = dataFile.getName().endsWith(".sfx") ? new LoadPresentationTask(dataFile) :
                dataFile.getName().endsWith(".sfxt") ? new LoadTemplateTask(dataFile) : null;


        if(loadingTask != null) {
            TaskAction.forTask(loadingTask)
                    .when().stateIs(Worker.State.RUNNING).perform(DisableAction.forElements(this.whenNoDocumentOpened).and(this.openElementsGroup))
                    .when().stateIs(Worker.State.READY).perform(DisableAction.forElements(this.whenNoDocumentOpened).and(this.openElementsGroup))
                    .when().stateIs(Worker.State.SUCCEEDED).perform(EnableAction.forElements(this.whenNoDocumentOpened).and(this.openElementsGroup))
                    .when().stateIs(Worker.State.FAILED).perform(EnableAction.forElements(this.whenNoDocumentOpened).and(this.openElementsGroup))
                    .when().stateIs(Worker.State.CANCELLED).perform(EnableAction.forElements(this.whenNoDocumentOpened).and(this.openElementsGroup));

            this.taskInProgress.setCurrentTask(loadingTask);
            loadingTask.stateProperty().addListener((value, oldState, newState) -> {
                if(newState != null && (
                        newState == Worker.State.FAILED ||
                                newState == Worker.State.CANCELLED ||
                                newState == Worker.State.SUCCEEDED) &&
                        loadingTask.getValue() != null) {

                    final FXMLLoader loader = new FXMLLoader(ResourceHelper.getURL("/com/twasyl/slideshowfx/fxml/PresentationView.fxml"));
                    try {
                        final Parent parent = loader.load();
                        final PresentationViewController controller = loader.getController();
                        controller.definePresentation(loadingTask.getValue());

                        final Tab tab = new Tab();
                        final PresentationModifiedBinding presentationModifiedBinding = new PresentationModifiedBinding(controller.presentationModifiedProperty());
                        final StringExpression tabTitle = controller.getPresentationName().concat(presentationModifiedBinding);
                        tab.textProperty().bind(tabTitle);
                        tab.setUserData(controller);
                        tab.setContent(parent);

                        this.openedPresentationsTabPane.getTabs().addAll(tab);
                        this.openedPresentationsTabPane.getSelectionModel().select(tab);

                        this.updateSlideTemplatesSplitMenu();
                        this.updateSlideSplitMenu();
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Can not load the view", e);
                    }
                }
            });

            TaskDAO.getInstance().startTask(loadingTask);
        }
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

    /**
     * Update the control hosting the slides' templates. This method get the templates from the current displayed presentation.
     * If no presentation is opened the list of templates is cleared.
     */
    private void updateSlideTemplatesSplitMenu() {
        this.addSlideButton.getItems().clear();

        final PresentationViewController view = this.getCurrentPresentationView();
        if (view != null) {
            SlideTemplate[] templates = view.getSlideTemplates();

            if(templates != null) {
                MenuItem item;

                for (SlideTemplate template : templates) {
                    item = new MenuItem();
                    item.setText(template.getName());
                    item.setUserData(template);
                    item.setOnAction(addSlideActionEvent);
                    this.addSlideButton.getItems().add(item);
                }
            }
        }
    }

    /**
     * Update the list of existing slides for the current presentation. If no presentation is opened or no slides are
     * defined, the control is cleared.
     */
    protected void updateSlideSplitMenu() {
        SlideshowFXController.this.moveSlideButton.getItems().clear();

        final PresentationViewController view = this.getCurrentPresentationView();
        if (view != null) {
            Slide[] slides = view.getSlides();

            if(slides != null) {
                SlideMenuItem menuItem;

                for (Slide slide : slides) {
                    menuItem = new SlideMenuItem(slide);
                    menuItem.setOnAction(SlideshowFXController.this.moveSlideActionEvent);
                    this.moveSlideButton.getItems().add(menuItem);
                }
            }
        }
    }

    /**
     * Save the current opened presentation to the given {@param archiveFile}. The process for
     * saving the presentation is only started if the given {@param archiveFile} is not {@code null}. If the process is
     * started, a {@link com.twasyl.slideshowfx.concurrent.SavePresentationTask} is started with the current presentation
     * and {@code archiveFile}.
     * @param archiveFile The file to save the presentation in.
     */
    private void savePresentation(File archiveFile) {
        if(archiveFile != null) {
            final PresentationViewController view = this.getCurrentPresentationView();
            final Task saveTask = new SavePresentationTask(view, archiveFile);

            TaskAction.forTask(saveTask)
                    .when().stateIs(Worker.State.RUNNING).perform(DisableAction.forElements(this.saveElementsGroup).and(this.openElementsGroup))
                    .when().stateIs(Worker.State.SUCCEEDED).perform(EnableAction.forElements(this.saveElementsGroup).and(this.openElementsGroup))
                    .when().stateIs(Worker.State.FAILED).perform(EnableAction.forElements(this.saveElementsGroup).and(this.openElementsGroup))
                    .when().stateIs(Worker.State.CANCELLED).perform(EnableAction.forElements(this.saveElementsGroup).and(this.openElementsGroup));
            this.taskInProgress.setCurrentTask(saveTask);

            TaskDAO.getInstance().startTask(saveTask);
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
                                if (newState == Worker.State.SUCCEEDED && task.getValue() != null) {
                                    ButtonType response = DialogHelper.showConfirmationAlert("Open file?", String.format("Do you want to open '%1$s' ?", task.getValue()));

                                    if(response != null && response == ButtonType.YES) {
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
     * This method looks for the current focused view containing the presentation.
     * In the current implementation the view is identified by the selected tab in the UI.
     * @return The controller associated to the displayed view or {@code null} if no presentation is opened or focused.
     */
    private PresentationViewController getCurrentPresentationView() {
        PresentationViewController view = null;

        final Tab selectedTab = this.openedPresentationsTabPane.getSelectionModel().getSelectedItem();

        if(selectedTab != null) {
            final Object userData = selectedTab.getUserData();
            if(userData != null && userData instanceof PresentationViewController) {
                view = (PresentationViewController) userData;
            }
        }

        return view;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add a listener for auto-saving the presentation
        final ScheduledService<Void> service = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                final PresentationViewController view = SlideshowFXController.this.getCurrentPresentationView();
                final File archiveFile = view == null ? null : view.getArchiveFile();
                final SavePresentationTask task = new SavePresentationTask(view, archiveFile);

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
            if (!newValue) service.cancel();
            else {
                Integer interval = null;
                try {
                    interval = Integer.parseInt(this.autoSaveItem.getValue());
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.FINE, "Can not parse auto save interval");
                }

                if (interval != null & interval > 0) {
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
            } catch (NumberFormatException e) {
                LOGGER.log(Level.FINE, "Can not parse auto save interval");
            }

            if (interval != null & interval > 0) {
                service.setDelay(Duration.minutes(interval.doubleValue()));
                service.setPeriod(Duration.minutes(interval.doubleValue()));
                service.restart();
            }
        });

        // We use reflection to disable all elements present in the list
        final Consumer<Object> disableElementLambda = element -> {
            try {
                final Method setDisable = element.getClass().getMethod("setDisable", boolean.class);
                setDisable.invoke(element, true);
            } catch (NoSuchMethodException e) {
                LOGGER.log(Level.FINE, "No setDisableMethod found", e);
            } catch (InvocationTargetException e) {
                LOGGER.log(Level.WARNING, "Can not disable element", e);
            } catch (IllegalAccessException e) {
                LOGGER.log(Level.WARNING, "Can not disable element", e);
            }
        };

        this.whenNoDocumentOpened.forEach(disableElementLambda);

        // Create the entries in the Upload & Download menus
        OSGiManager.getInstalledServices(IHostingConnector.class)
                .stream()
                .sorted((hostingConnector1, hostingConnector2) -> hostingConnector1.getName().compareTo(hostingConnector2.getName()))
                .forEach(hostingConnector -> {
                    createUploaderMenuItem(hostingConnector);
                    createDownloaderMenuItem(hostingConnector);
                });

        this.openedPresentationsTabPane.getSelectionModel().selectedItemProperty().addListener((value, oldSelection, newSelection) -> {
            if(newSelection != null) {
                final Object userData = newSelection.getUserData();

                if(userData != null && userData instanceof PresentationViewController) {
                    final PresentationViewController view = (PresentationViewController) userData;
                    view.setAsDefault();

                    this.updateSlideSplitMenu();
                    this.updateSlideTemplatesSplitMenu();

                    // Bind the title of the presentation with the application bar title
                    if(SlideshowFX.getStage().titleProperty().isBound()) SlideshowFX.getStage().titleProperty().unbind();
                    SlideshowFX.getStage().titleProperty().bind(new SimpleStringProperty("SlideshowFX - ").concat(view.getPresentationName()));

                } else {
                    PresentationDAO.getInstance().setCurrentPresentation(null);
                }
            } else {
                this.updateSlideSplitMenu();
                this.updateSlideTemplatesSplitMenu();

                this.whenNoDocumentOpened.forEach(disableElementLambda);

                if(SlideshowFX.getStage().titleProperty().isBound()) SlideshowFX.getStage().titleProperty().unbind();
                SlideshowFX.getStage().setTitle("SlideshowFX");
            }
        });

        this.openedPresentationsTabPane.getTabs().addListener((ListChangeListener) change -> {
            final  StackPane header = (StackPane) this.openedPresentationsTabPane.lookup(".tab-header-area");

            if(header != null) {
                if(this.openedPresentationsTabPane.getTabs().size() == 1) header.setPrefHeight(0);
                else header.setPrefHeight(-1);
            }
        });
    }
}
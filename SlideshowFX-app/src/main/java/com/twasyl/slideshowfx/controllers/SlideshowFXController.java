package com.twasyl.slideshowfx.controllers;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Listener;
import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.concurrent.*;
import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.controls.SlideMenuItem;
import com.twasyl.slideshowfx.controls.Tour;
import com.twasyl.slideshowfx.controls.notification.NotificationCenter;
import com.twasyl.slideshowfx.controls.slideshow.Context;
import com.twasyl.slideshowfx.controls.slideshow.SlideshowStage;
import com.twasyl.slideshowfx.controls.stages.AboutStage;
import com.twasyl.slideshowfx.controls.stages.HelpStage;
import com.twasyl.slideshowfx.controls.stages.LogsStage;
import com.twasyl.slideshowfx.controls.stages.TemplateBuilderStage;
import com.twasyl.slideshowfx.dao.TaskDAO;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.engine.presentation.Presentations;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.exceptions.HostingConnectorException;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
import com.twasyl.slideshowfx.io.SlideshowFXExtensionFilter;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.server.service.AttendeeChatService;
import com.twasyl.slideshowfx.server.service.PresenterChatService;
import com.twasyl.slideshowfx.server.service.QuizService;
import com.twasyl.slideshowfx.server.service.TwitterService;
import com.twasyl.slideshowfx.services.AutoSavingService;
import com.twasyl.slideshowfx.utils.*;
import com.twasyl.slideshowfx.utils.beans.Pair;
import com.twasyl.slideshowfx.utils.beans.binding.WildcardBinding;
import com.twasyl.slideshowfx.utils.concurrent.SlideshowFXTask;
import com.twasyl.slideshowfx.utils.concurrent.TaskAction;
import com.twasyl.slideshowfx.utils.concurrent.actions.DisableAction;
import com.twasyl.slideshowfx.utils.concurrent.actions.EnableAction;
import com.twasyl.slideshowfx.utils.keys.KeyEventUtils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *  This class is the controller of the {@code Slideshow.fxml} file. It defines all actions possible inside the view
 *  represented by the FXML.
 *  
 *  @author Thierry Wasyczenko
 *  @version 1.1
 *  @since SlideshowFX 1.0
 */
public class SlideshowFXController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(SlideshowFXController.class.getName());

    private final EventHandler<ActionEvent> addSlideActionEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            try {

                final PresentationEngine presentation = Presentations.getCurrentDisplayedPresentation();
                final PresentationViewController view = SlideshowFXController.this.getCurrentPresentationView();
                final Object userData = ((MenuItem) actionEvent.getSource()).getUserData();

                if (userData instanceof SlideTemplate && view != null && presentation != null) {
                    presentation.addSlide((SlideTemplate) userData, view.getCurrentSlideNumber());

                    final ReloadPresentationViewTask task = new ReloadPresentationViewTask(view);
                    SlideshowFXController.this.taskInProgress.setCurrentTask(task);
                    TaskDAO.getInstance().startTask(task);

                    SlideshowFXController.this.updateSlideSplitMenu();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error when adding a slide", e);
            }
        }
    };

    private final EventHandler<ActionEvent> moveSlideActionEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            final PresentationEngine presentation = Presentations.getCurrentDisplayedPresentation();
            final PresentationViewController view = SlideshowFXController.this.getCurrentPresentationView();

            if(view != null && presentation != null) {
                final SlideMenuItem menunItem = (SlideMenuItem) actionEvent.getSource();
                final Slide slideToMove = presentation.getConfiguration().getSlideById(view.getCurrentSlideId());
                final Slide beforeSlide = menunItem.getSlide();

                presentation.moveSlide(slideToMove, beforeSlide);

                final ReloadPresentationViewTask task = new ReloadPresentationViewTask(view);
                SlideshowFXController.this.taskInProgress.setCurrentTask(task);
                TaskDAO.getInstance().startTask(task);

                SlideshowFXController.this.updateSlideSplitMenu();
            }

        }
    };

    @FXML private BorderPane root;

    /* Main ToolBar elements */
    @FXML private SplitMenuButton addSlideButton;
    @FXML private SplitMenuButton moveSlideButton;
    @FXML private ComboBox<String> serverIpAddress;
    @FXML private TextField serverPort;
    @FXML private TextField twitterHashtag;
    @FXML private Button startServerButton;
    @FXML private CheckBox leapMotionEnabled;

    /* Main application UI elements */
    @FXML private TabPane openedPresentationsTabPane;

    /* Main application menu elements */
    @FXML private Menu uploadersMenu;
    @FXML private Menu downloadersMenu;
    @FXML private MenuItem openWebApplicationMenuItem;

    /* Notification center */
    @FXML private NotificationCenter taskInProgress;

    /* List of controls */
    @FXML private ObservableList<Object> saveElementsGroup;
    @FXML private ObservableList<Object> openElementsGroup;
    @FXML private ObservableList<Object> whenNoDocumentOpened;

    /* All methods called by the FXML */

    /**
     * Loads a SlideshowFX template. This method displays an open dialog which only allows to open template files (with
     * .sfxt archiveExtension) and then call the {@link #openTemplateOrPresentation(File)} method.
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
     * (with the .sfx archiveExtension) and then call {@link #openTemplateOrPresentation(File)} method.
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
            } catch (IllegalAccessException | FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Can not open the presentation", e);
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
     * Close the given presentation
     * @param presentation The presentation to close.
     * @param waitToFinish Indicates if the method should wait before exiting.
     */
    public void closePresentation(final PresentationEngine presentation, final boolean waitToFinish) {
        if(presentation != null && presentation.isModifiedSinceLatestSave()) {

            final String message = presentation.getArchive() != null ?
                    String.format("Do you want to save the modifications on %1$s?", presentation.getArchive().getName()) :
                    "Do you want to save this presentation?";

            final ButtonType answer = DialogHelper.showConfirmationAlert("Save the presentation", message);

            if(answer == ButtonType.YES) {
                SlideshowFXController.this.savePresentation(presentation, waitToFinish);
            }

            AutoSavingService.cancelFor(presentation);
        }
    }

    /**
     * Close all opened presentations. If a presentation hasn't been saved, the user is prompted if he wants to save
     * the modifications.
     * @param waitToFinish Indicates the method must wait for all presentations to be closed before exiting.
     */
    public void closeAllPresentations(final boolean waitToFinish) {
        PlatformHelper.run(() -> {
            this.openedPresentationsTabPane.getTabs()
                    .filtered(tab -> tab.getUserData() != null && tab.getUserData() instanceof PresentationViewController)
                    .forEach(tab -> {
                        final PresentationEngine presentation = ((PresentationViewController) tab.getUserData()).getPresentation();
                        this.closePresentation(presentation, waitToFinish);
                    });
            Platform.exit();
        });
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
                    .filter(file -> file.getName().endsWith(PresentationEngine.DEFAULT_DOTTED_ARCHIVE_EXTENSION)
                            || file.getName().endsWith(TemplateEngine.DEFAULT_DOTTED_ARCHIVE_EXTENSION))
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
                    .filter(file -> file.getName().endsWith(PresentationEngine.DEFAULT_DOTTED_ARCHIVE_EXTENSION)
                            || file.getName().endsWith(TemplateEngine.DEFAULT_DOTTED_ARCHIVE_EXTENSION))
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
     * Open the current presentation inside the default browser of the user.
     * @param event The event associated to this request.
     */
    @FXML private void openPresentationInBrowser(ActionEvent event) {
        if(Desktop.isDesktopSupported()) {
            final PresentationEngine presentation = Presentations.getCurrentDisplayedPresentation();

            if(presentation != null) {
                final File presentationFile = presentation.getConfiguration().getPresentationFile();

                if(presentationFile != null && presentationFile.exists()) {
                    try {
                        Desktop.getDesktop().browse(presentationFile.toURI());
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Can not open working directory", e);
                    }
                }
            }
        }
    }

    /**
     * Open the current working directory in the file explorer of the system.
     * @param event The event associated to the request.
     */
    @FXML private void openWorkingDirectory(ActionEvent event) {
        final PresentationEngine presentation = Presentations.getCurrentDisplayedPresentation();

        if(presentation != null) {
            final File workingDir = presentation.getWorkingDirectory();

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
            .addStep(new Tour.Step("#serverIpAddress", "The IP address of the embedded server. If nothing is provided, an automatic IP will be used."))
            .addStep(new Tour.Step("#serverPort", "The port of the embedded server. If nothing is provided, 8080 will be used."))
            .addStep(new Tour.Step("#twitterHashtag", "Look for the given hashtag on Twitter. If left blank, the Twitter service will not be started."))
            .addStep(new Tour.Step("#startServerButton", "Start or stop the embedded server."))
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
        new AboutStage().show();
    }

    /**
     * Displays an internal browser in a specific tab.
     * @param event The source event.
     */
    @FXML private void displayInternalBrowser(ActionEvent event) {
        try {
            final Parent root = FXMLLoader.load(ResourceHelper.getURL("/com/twasyl/slideshowfx/fxml/InternalBrowser.fxml"));
            final Tab tab = new Tab("Internal browser", root);

            this.openedPresentationsTabPane.getTabs().addAll(tab);
            this.openedPresentationsTabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not open internal browser", e);
        }
    }

    @FXML private void displayWebApplication(final ActionEvent event) {
        try {
            final Parent root = FXMLLoader.load(ResourceHelper.getURL("/com/twasyl/slideshowfx/fxml/SlideshowFXWebApplication.fxml"));
            final Tab tab = new Tab("Web application", root);

            this.openedPresentationsTabPane.getTabs().addAll(tab);
            this.openedPresentationsTabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not open web application", e);
        }
    }

    /**
     * Displays the window allowing to display the logs.
     * @param event The source event.
     */
    @FXML private void displayLogs(final ActionEvent event) {
        new LogsStage().show();
    }

    /**
     * Displays the help stage.
     * @param event The source event.
     */
    @FXML private void displayHelp(final ActionEvent event) {
        new HelpStage().show();
    }

    /**
     * Displays the plugin center.
     * @param event The source event.
     */
    @FXML private void displayPluginCenter(final ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(ResourceHelper.getURL("/com/twasyl/slideshowfx/fxml/PluginCenter.fxml"));
        try {
            final Parent root = loader.load();
            final PluginCenterController controller = loader.getController();

            final ButtonType response = DialogHelper.showCancellableDialog("Plugin center", root);

            if(response.equals(ButtonType.OK)) {
                controller.validatePluginsConfiguration();

                this.openedPresentationsTabPane.getTabs()
                        .stream()
                        .filter(tab -> tab.getUserData() != null && tab.getUserData() instanceof PresentationViewController)
                        .forEach(tab -> {
                            ((PresentationViewController) tab.getUserData()).refreshMarkupSyntax();
                            ((PresentationViewController) tab.getUserData()).refreshContentExtensions();
                        });

                this.refreshHostingConnectors();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not open plugin center view", e);
        }
    }

    /**
     * Copy the slide, update the menu of available slides and reload the presentation.
     * The copy is delegated to {@link PresentationEngine#duplicateSlide(Slide)}.
     *
     * @param event
     */
    @FXML private void copySlide(ActionEvent event) {
        final PresentationEngine presentation = Presentations.getCurrentDisplayedPresentation();
        final PresentationViewController view = getCurrentPresentationView();

        if(presentation != null && view != null) {
            final Slide source = presentation.getConfiguration().getSlideById(view.getCurrentSlideId());

            this.copySlide(presentation, source);
            this.updateSlideSplitMenu();

            final ReloadPresentationViewTask task = new ReloadPresentationViewTask(view);
            this.taskInProgress.setCurrentTask(task);
            TaskDAO.getInstance().startTask(task);
        }
    }

    /**
     * Delete a slide from the presentation. The deletion is delegated to {@link PresentationEngine#deleteSlide(String)}.
     *
     * @param event
     */
    @FXML private void deleteSlide(ActionEvent event) {
        final PresentationEngine presentation = Presentations.getCurrentDisplayedPresentation();
        final PresentationViewController view = this.getCurrentPresentationView();

        if(presentation != null && view != null) {
            final String slideNumberToDelete = view.getCurrentSlideNumber();

            if(slideNumberToDelete != null) {
                final Slide slideBefore = presentation.getConfiguration().getSlideBefore(slideNumberToDelete);

                presentation.deleteSlide(slideNumberToDelete);

                if(slideBefore == null) this.reloadPresentation(view);
                else reloadPresentationAndGoToSlide(view, slideBefore.getId());
            }
        }
    }

    /**
     * Simply reload the presentation by calling {@link javafx.scene.web.WebEngine#reload()}.
     *
     * @param event
     */
    @FXML private void reload(ActionEvent event) {
        reloadPresentation(this.getCurrentPresentationView());
    }

    /**
     * Save the current presentation. If the presentation has never been saved a save dialog is displayed.
     * Then the presentation is saved where the user has chosen or opened the presentation.
     * The saving is delegated to {@link #savePresentation(boolean)}
     *
     * @param event
     */
    @FXML private void save(ActionEvent event) {
        this.savePresentation(false);
    }

    /**
     * Saves a copy of the existing presentation. A save dialog is displayed to the user.
     * The saving is delegated to {@link #savePresentation(File, boolean)}.
     *
     * @param event
     */
    @FXML private void saveAs(ActionEvent event) {
        File presentationArchive = null;
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PRESENTATION_FILES);
        presentationArchive = chooser.showSaveDialog(SlideshowFX.getStage());

        this.savePresentation(presentationArchive, false);
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
        this.startSlideshow(false);
    }

    @FXML private void slideshowFromCurrentSlide(ActionEvent event) {
        this.startSlideshow(true);
    }

    /**
     * This methods starts the chat when the ENTER key is pressed in the IP address, port number or Twitter's hashtag textfields.
     *
     * @param event
     */
    @FXML private void startServerByKeyPressed(KeyEvent event) {
        if(event.getCode().equals(KeyCode.ENTER)) this.startServer();
    }

    /**
     * This methods starts the chat when the button for starting the chat is clicked.
     *
     * @param event
     */
    @FXML private void startServerByButton(ActionEvent event) {
        this.startServer();
    }

    /**
     * This method is called in order to create a template from scratch.
     * @param event The event associated to the click that should open the template builder.
     */
    @FXML private void createTemplate(ActionEvent event) {
        final TemplateEngine engine = new TemplateEngine();
        engine.setWorkingDirectory(engine.generateWorkingDirectory());

        if(engine.getWorkingDirectory().mkdir()) {
            final File templateConfigurationFile = new File(engine.getWorkingDirectory(), engine.getConfigurationFilename());

            try {
                Files.createFile(templateConfigurationFile.toPath());
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Can not create template configuration file", e);
            }
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
            engine.setArchive(file);

            if(engine.getWorkingDirectory().mkdir()) {
                try {
                    ZipUtils.unzip(file, engine.getWorkingDirectory());

                    this.showTemplateBuilder(engine);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Can not unzip the template", e);
                }
            }
        }
    }

    /**
     * This method is called in order to edit the current template for the opened presentation.
     * @param event The event associated to the click that should open the template builder.
     */
    @FXML private void editTemplate(ActionEvent event) {
        final PresentationEngine presentation = Presentations.getCurrentDisplayedPresentation();

        if(presentation != null) {
            final TemplateEngine engine = new TemplateEngine();
            engine.setWorkingDirectory(presentation.getWorkingDirectory());

            this.showTemplateBuilder(engine);
        }
    }

    /**
     * This method exits the application. If any opened presentation is not saved, the user will be asked if he wants to
     * save the modifications or not.
     *
     * @param event
     */
    @FXML private void exitApplication(ActionEvent event) {
        this.closeAllPresentations(true);
    }

    /**
     * This method shows a dialog for options of SlideshowFX.
     * @param event
     */
    @FXML private void showOptionsDialog(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(ResourceHelper.getURL("/com/twasyl/slideshowfx/fxml/OptionsView.fxml"));
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
     * Open the dataFile. If the name ends with {@code .sfx} the file is considered as a presentation,
     * if it ends with {@code .sfx} it is considered as a template.
     *
     * @param dataFile the file corresponding to either a template or a presentation.
     * @throws IllegalArgumentException If the file is null.
     * @throws FileNotFoundException      If dataFile does not exist.
     * @throws IllegalAccessException   If the file can not be accessed.
     */
    public void openTemplateOrPresentation(final File dataFile) throws IllegalArgumentException, IllegalAccessException, FileNotFoundException {
        if (dataFile == null) throw new IllegalArgumentException("The dataFile can not be null");
        if (!dataFile.exists()) throw new FileNotFoundException("The dataFile does not exist");
        if (!dataFile.canRead()) throw new IllegalAccessException("The dataFile can not be accessed");

        final SlideshowFXTask<PresentationEngine> loadingTask = dataFile.getName().endsWith(PresentationEngine.DEFAULT_DOTTED_ARCHIVE_EXTENSION)
                ? new LoadPresentationTask(dataFile) : dataFile.getName().endsWith(TemplateEngine.DEFAULT_DOTTED_ARCHIVE_EXTENSION) ?
                new LoadTemplateTask(dataFile) : null;

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
                        final WildcardBinding presentationModifiedBinding = new WildcardBinding(controller.presentationModifiedProperty());
                        final StringExpression tabTitle = controller.getPresentationName().concat(presentationModifiedBinding);
                        tab.textProperty().bind(tabTitle);
                        tab.setUserData(controller);
                        tab.setContent(parent);
                        tab.setOnCloseRequest(event -> SlideshowFXController.this.closePresentation(loadingTask.getValue(), false));

                        this.openedPresentationsTabPane.getTabs().addAll(tab);
                        this.openedPresentationsTabPane.getSelectionModel().select(tab);

                        this.updateSlideTemplatesSplitMenu();
                        this.updateSlideSplitMenu();

                        final AutoSavingService autoSavingService = new AutoSavingService(loadingTask.getValue());
                        if(GlobalConfiguration.isAutoSavingEnabled()) {
                            PlatformHelper.run(() -> autoSavingService.start());
                        }
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
        new TemplateBuilderStage(engine).show();
    }

    /**
     * Update the control hosting the slides' templates. This method get the templates from the current displayed presentation.
     * If no presentation is opened the list of templates is cleared.
     */
    private void updateSlideTemplatesSplitMenu() {
        this.addSlideButton.getItems().clear();

        final PresentationEngine presentation = Presentations.getCurrentDisplayedPresentation();
        if (presentation != null) {
            List<SlideTemplate> templates = presentation.getTemplateConfiguration().getSlideTemplates();

            if(templates != null) {
                templates.forEach(template -> {
                    final MenuItem item = new MenuItem();
                    item.setText(template.getName());
                    item.setUserData(template);
                    item.setOnAction(addSlideActionEvent);
                    this.addSlideButton.getItems().add(item);
                });
            }
        }
    }

    /**
     * Update the list of existing slides for the current presentation. If no presentation is opened or no slides are
     * defined, the control is cleared.
     */
    protected void updateSlideSplitMenu() {
        SlideshowFXController.this.moveSlideButton.getItems().clear();

        final PresentationEngine presentation = Presentations.getCurrentDisplayedPresentation();
        if (presentation != null) {
            List<Slide> slides = presentation.getConfiguration().getSlides();

            if(slides != null) {
                slides.forEach(slide -> {
                    final SlideMenuItem menuItem = new SlideMenuItem(slide);
                    menuItem.setOnAction(SlideshowFXController.this.moveSlideActionEvent);
                    this.moveSlideButton.getItems().add(menuItem);
                });
            }
        }
    }

    /**
     * Copy a given {@link Slide slide} of the {@link PresentationEngine presentation}.
     * @param presentation The presentation where the slide will be copied.
     * @param slide The slide to copy
     */
    private void copySlide(final PresentationEngine presentation, final Slide slide) {
        try {
            presentation.duplicateSlide(slide);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when copying the slide", e);
        }
    }

    /**
     * Save the presentation that is currently displayed, if any. This method retrieves the displayed presentation and
     * calls {@link #savePresentation(PresentationEngine, boolean)}.
     * @param waitToFinish Indicates if the method should wait before exiting.
     */
    private void savePresentation(boolean waitToFinish) {
        this.savePresentation(Presentations.getCurrentDisplayedPresentation(), waitToFinish);
    }

    /**
     * Save the given presentation. If the presentation hasn't been already saved, the user is prompted
     * to choose where to save it. Once the choice is validated, the method calls {@link #savePresentation(File, boolean)}.
     * @param presentation The presentation to save.
     * @param waitToFinish Indicates if the method should wait before exiting.
     */
    private void savePresentation(final PresentationEngine presentation, final boolean waitToFinish) {
        if(presentation != null) {
            File presentationArchive;

            if (!presentation.isPresentationAlreadySaved()) {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PRESENTATION_FILES);
                presentationArchive = chooser.showSaveDialog(SlideshowFX.getStage());

                final AutoSavingService autoSavingService = new AutoSavingService(presentation);
                if(GlobalConfiguration.isAutoSavingEnabled()) {
                    PlatformHelper.run(() -> autoSavingService.start());
                }
            } else presentationArchive = presentation.getArchive();

            this.savePresentation(presentationArchive, waitToFinish);
        }
    }

    /**
     * Save the current opened presentation to the given {@param archiveFile}. The process for
     * saving the presentation is only started if the given {@param archiveFile} is not {@code null}. If the process is
     * started, a {@link SavePresentationTask} is started with the current presentation
     * and {@code archiveFile}.
     * @param archiveFile The file to save the presentation in.
     * @param waitToFinish Indicates if the method should wait before exiting.
     */
    private void savePresentation(final File archiveFile, final boolean waitToFinish) {
        if(archiveFile != null) {
            final PresentationEngine presentation = Presentations.getCurrentDisplayedPresentation();
            presentation.setArchive(archiveFile);

            final SlideshowFXTask saveTask = new SavePresentationTask(presentation);

            TaskAction.forTask(saveTask)
                    .when().stateIs(Worker.State.RUNNING).perform(DisableAction.forElements(this.saveElementsGroup).and(this.openElementsGroup))
                    .when().stateIs(Worker.State.SUCCEEDED).perform(EnableAction.forElements(this.saveElementsGroup).and(this.openElementsGroup))
                    .when().stateIs(Worker.State.FAILED).perform(EnableAction.forElements(this.saveElementsGroup).and(this.openElementsGroup))
                    .when().stateIs(Worker.State.CANCELLED).perform(EnableAction.forElements(this.saveElementsGroup).and(this.openElementsGroup));
            this.taskInProgress.setCurrentTask(saveTask);

            TaskDAO.getInstance().startTask(saveTask);

            if(waitToFinish) {
                PlatformHelper.run(() -> SlideshowFX.getStage().getScene().setCursor(Cursor.WAIT));
                try {
                    saveTask.get();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Can not wait for the presentation to be saved", e);
                } finally {
                    PlatformHelper.run(() -> SlideshowFX.getStage().getScene().setCursor(Cursor.DEFAULT));
                }

            }
        }
    }

    /**
     * Reloads a presentation for a given view.
     * @param view The view of the presentation to reload.
     */
    private void reloadPresentation(final PresentationViewController view) {
        reloadPresentationAndGoToSlide(view, null);
    }

    /**
     * Reloads a presentation contained in a given {@link PresentationViewController view} and then go to a given slide
     * identified by its id. If the provided ID is {@code null}, the presentation will only be reloaded.
     * @param view The view of the presentation to reload.
     * @param id The ID of the slide to go to when the presentation has been successfully reloaded.
     */
    private void reloadPresentationAndGoToSlide(final PresentationViewController view, final String id) {
        if(view != null) {
            final ReloadPresentationViewTask task;

            if(id != null && !id.isEmpty()) {
                task = new ReloadPresentationViewAndGoToTask(view, id);
            } else {
                task = new ReloadPresentationViewTask(view);
            }

            SlideshowFXController.this.taskInProgress.setCurrentTask(task);
            TaskDAO.getInstance().startTask(task);
        }
    }

    /**
     * Start the chat. This method takes the text entered in the IP, port and Twitter's hashtag fields to start the chat.
     * If no text is entered for the IP address and the port number, the IP address of the computer is used and the port 80 is chosen.
     */
    private void startServer() {
        FontAwesomeIconView icon;
        final Tooltip tooltip = new Tooltip();

        if (SlideshowFXServer.getSingleton() != null) {
            SlideshowFXServer.getSingleton().stop();

            icon = new FontAwesomeIconView(FontAwesomeIcon.PLAY);
            icon.setGlyphSize(20);
            icon.setGlyphStyle("-fx-fill: green");

            tooltip.setText("Start the server");
        } else {
            String ip = this.serverIpAddress.getValue();
            if (ip == null || ip.isEmpty()) {
                ip = NetworkUtils.getIP();
            }

            int port = 80;
            if (this.serverPort.getText() != null && !this.serverPort.getText().isEmpty()) {
                try {
                    port = Integer.parseInt(this.serverPort.getText());
                } catch(NumberFormatException ex) {
                    LOGGER.log(Level.WARNING, "Can not parse given chat port, use the default one instead", ex);
                }
            }

            this.serverIpAddress.setValue(ip);
            this.serverPort.setText(port + "");

            SlideshowFXServer.create(ip, port, this.twitterHashtag.getText()).start(
                    AttendeeChatService.class,
                    PresenterChatService.class,
                    QuizService.class,
                    TwitterService.class
            );

            icon = new FontAwesomeIconView(FontAwesomeIcon.POWER_OFF);
            icon.setGlyphSize(20);
            icon.setGlyphStyle("-fx-fill: app-color-orange");

            tooltip.setText("Stop the server");
        }

        this.startServerButton.setGraphic(icon);
        this.startServerButton.setTooltip(tooltip);
        this.serverIpAddress.setDisable(!this.serverIpAddress.isDisable());
        this.serverPort.setDisable(!this.serverPort.isDisable());
        this.twitterHashtag.setDisable(!this.twitterHashtag.isDisable());
        this.openWebApplicationMenuItem.setDisable(SlideshowFXServer.getSingleton() == null);
    }

    /**
     * Refresh the {@link IHostingConnector hosting connectors} items in the UI. The items for downloading and
     * uploading presentations will be updated.
     */
    private void refreshHostingConnectors() {
        this.downloadersMenu.getItems().clear();
        this.uploadersMenu.getItems().clear();

        OSGiManager.getInstalledServices(IHostingConnector.class)
                .stream()
                .sorted((hostingConnector1, hostingConnector2) -> hostingConnector1.getName().compareTo(hostingConnector2.getName()))
                .forEach(hostingConnector -> {
                    createUploaderMenuItem(hostingConnector);
                    createDownloaderMenuItem(hostingConnector);
                });
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
                    try {
                        hostingConnector.authenticate();
                    } catch (HostingConnectorException e) {
                        final Pair<String, String> pair = e.getTitleAndMessage();
                        DialogHelper.showError(pair.getKey(), pair.getValue());
                    }
                }

                if (hostingConnector.isAuthenticated()) {
                    // Prompts the user where to upload the presentation
                    final RemoteFile destination;
                    try {
                        destination = hostingConnector.chooseFile(true, false);

                        if (destination != null) {
                            final UploadPresentationTask task = new UploadPresentationTask(Presentations.getCurrentDisplayedPresentation(),
                                    hostingConnector, destination);
                            SlideshowFXController.this.taskInProgress.setCurrentTask(task);
                            TaskDAO.getInstance().startTask(task);
                        }
                    } catch (HostingConnectorException e) {
                        final Pair<String, String> pair = e.getTitleAndMessage();
                        DialogHelper.showError(pair.getKey(), pair.getValue());
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
                    try {
                        hostingConnector.authenticate();
                    } catch (HostingConnectorException e) {
                        final Pair<String, String> pair = e.getTitleAndMessage();
                        DialogHelper.showError(pair.getKey(), pair.getValue());
                    }
                }

                if (hostingConnector.isAuthenticated()) {
                    // Prompts the user which file to download
                    try {
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

                                        if (response != null && response == ButtonType.YES) {
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
                    } catch (HostingConnectorException e) {
                        final Pair<String, String> pair = e.getTitleAndMessage();
                        DialogHelper.showError(pair.getKey(), pair.getValue());
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

    /**
     * Start the slideshow. This methods determines the current presentation and if the LeapMotion controller should be
     * enabled or not.
     * If the {@code fromCurrentSlide} parameter is set to {@code true}, the current slide is also determined.
     *
     * @param fromCurrentSlide Indicates if the slideshow must be started from the current slide or not.
     */
    public void startSlideshow(boolean fromCurrentSlide) {
        final PresentationViewController view = this.getCurrentPresentationView();

        if(view != null) {
            final PresentationEngine presentation = Presentations.getCurrentDisplayedPresentation();
            final String currentSlideId = fromCurrentSlide ? view.getCurrentSlideId() : null;
            final boolean enabledLeapMotion = !this.leapMotionEnabled.disabledProperty().get()
                    && this.leapMotionEnabled.selectedProperty().get();

            if (presentation.getConfiguration() != null
                    && presentation.getConfiguration().getPresentationFile() != null
                    && presentation.getConfiguration().getPresentationFile().exists()) {

                final Context context = new Context();
                context.setLeapMotionEnabled(enabledLeapMotion);
                context.setStartAtSlideId(currentSlideId);
                context.setPresentation(presentation);

                final SlideshowStage stage = new SlideshowStage(context);
                stage.onClose(() -> {
                    final String slideId = stage.getDisplayedSlideId();
                    view.goToSlide(slideId);
                });
                stage.show();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Create a LeapMotion controller in order to check/uncheck the checkbox enabling the LeapMotion or not.
        // This allows the checkbox to be in a correct state whether the LeapMotion is connected or not.
        final Controller leapMotionController = new Controller();
        leapMotionController.addListener(new Listener() {
            @Override
            public void onInit(Controller controller) {
                if(!controller.isConnected()) {
                    SlideshowFXController.this.leapMotionEnabled.setDisable(true);
                } else {
                    SlideshowFXController.this.leapMotionEnabled.setDisable(false);
                    SlideshowFXController.this.leapMotionEnabled.setSelected(true);
                }

            }

            @Override
            public void onConnect(Controller controller) {
                SlideshowFXController.this.leapMotionEnabled.setDisable(false);
                // We don't select the checkbox because even if the LeapMotion becom available, the user may not want
                // to enable it.
            }

            @Override
            public void onDisconnect(Controller controller) {
                SlideshowFXController.this.leapMotionEnabled.setDisable(true);
                SlideshowFXController.this.leapMotionEnabled.setSelected(false);
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

        refreshHostingConnectors();

        this.openedPresentationsTabPane.getSelectionModel().selectedItemProperty().addListener((value, oldSelection, newSelection) -> {
            if(newSelection != null) {
                final Object userData = newSelection.getUserData();

                if(userData != null && userData instanceof PresentationViewController) {
                    final PresentationViewController view = (PresentationViewController) userData;
                    view.setAsCurrentPresentation();

                    this.updateSlideSplitMenu();
                    this.updateSlideTemplatesSplitMenu();

                    // Bind the title of the presentation with the application bar title
                    if(SlideshowFX.getStage().titleProperty().isBound()) SlideshowFX.getStage().titleProperty().unbind();

                    final WildcardBinding presentationModifiedBinding = new WildcardBinding(view.presentationModifiedProperty());
                    final StringExpression title = new SimpleStringProperty("SlideshowFX - ")
                                                            .concat(view.getPresentationName())
                                                            .concat(presentationModifiedBinding);
                    SlideshowFX.getStage().titleProperty().bind(title);
                } else {
                    Presentations.setCurrentDisplayedPresentation(null);
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

        // Define global shortcuts of the application
        this.root.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            boolean consumed = false;

            if(event.isShortcutDown()) {
                if(KeyEventUtils.isShortcutSequence("R", event)) {
                    consumed = true;
                    this.reload(null);
                 }
            } else if(KeyCode.DELETE.equals(event.getCode())) {
                consumed = true;
                this.deleteSlide(null);
            }

            if(consumed) event.consume();
        });
    }
}
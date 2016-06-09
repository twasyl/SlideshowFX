package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.utils.DialogHelper;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrintQuality;
import javafx.print.PrinterJob;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;

/**
 * A browser that displays a presentation and provides methods for interacting with the presentation (like go to a given
 * slide, define the content of a slide, etc).
 * The browser is composed by a {@link #backendProperty()} which will be set as a member of the displayed page in the
 * browser under the name returned by {@link TemplateConfiguration#getJsObject()} variable stored in the {@link #presentationProperty()}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 */
public final class PresentationBrowser extends StackPane {
    private static final Logger LOGGER = Logger.getLogger(PresentationBrowser.class.getName());

    private final BooleanProperty interactionAllowed = new SimpleBooleanProperty(true);
    private final ObjectProperty<PresentationEngine> presentation = new SimpleObjectProperty<>();
    private final ObjectProperty<Object> backend = new SimpleObjectProperty<>();

    private final WebView internalBrowser = new WebView();
    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    public PresentationBrowser() {
        this.initializeProgressIndicator();
        this.initializeBrowser();

        this.setAlignment(Pos.TOP_LEFT);
        this.getChildren().add(this.internalBrowser);
    }

    /**
     * Get the internal browser of this {@link PresentationBrowser}. The internal browser is the {@link WebView} that is
     * used to display the presentation and is never null.
     * CAUTION: in order to manipulate the presentation, you should use the methods present in the {@link PresentationBrowser}
     * class and not the internal browser.
     * @return The internal browser of this {@link PresentationBrowser}.
     */
    public WebView getInternalBrowser() { return this.internalBrowser; }

    /**
     * The presentation associated to this browser.
     * @return The property of the presentation associated to this browser.
     */
    public ObjectProperty<PresentationEngine> presentationProperty() { return presentation; }

    /**
     * Get the presentation associated to this browser.
     * @return The presentation associated to this browser or {@code null} if it hasn't been defined yet.
     */
    public PresentationEngine getPresentation() { return presentation.get(); }

    /**
     * Defines the presentation associated to this browser.
     * @param presentation The presentation associated to this browser.
     */
    public void setPresentation(PresentationEngine presentation) { this.presentation.set(presentation); }

    /**
     * The backend associated to this browser. The backend is defined as member of the page displayed under the name
     * returned by the {@link TemplateConfiguration#getJsObject()} method of the current {@link #presentationProperty()}.
     * @return The property for backend object that has been defined.
     */
    public ObjectProperty<Object> backendProperty() { return backend; }

    /**
     * The backend associated to this browser. The backend is defined as member of the page displayed under the name
     * returned by the {@link TemplateConfiguration#getJsObject()} method of the current {@link #presentationProperty()}.
     * @return The backend object that has been defined or {@code null} if it hasn't been defined yet.
     */
    public Object getBackend() { return backend.get(); }

    /**
     * Defines the backend associated to this browser. The backend is defined as member of the page displayed under the
     * name returned by the {@link TemplateConfiguration#getJsObject()} method of the current {@link #presentationProperty()}.
     * @param backend The backend to set as member of the displayed page.
     */
    public void setBackend(Object backend) { this.backend.set(backend); }

    /**
     * Initializes the node indicating the status of the page's loading.
     */
    private final void initializeProgressIndicator() {
        final DoubleBinding size = this.widthProperty().divide(15d);

        this.progressIndicator.prefWidthProperty().bind(size);
        this.progressIndicator.minWidthProperty().bind(size);
        this.progressIndicator.maxWidthProperty().bind(size);

        this.progressIndicator.prefHeightProperty().bind(size);
        this.progressIndicator.minHeightProperty().bind(size);
        this.progressIndicator.maxHeightProperty().bind(size);

        this.progressIndicator.translateXProperty().bind(this.widthProperty().divide(2d).subtract(this.progressIndicator.widthProperty().divide(2)));
        this.progressIndicator.translateYProperty().bind(this.heightProperty().divide(2d).subtract(this.progressIndicator.heightProperty().divide(2)));
    }

    /**
     * Initializes the internal browser.
     */
    private final void initializeBrowser() {
        this.internalBrowser.disableProperty().bind(this.interactionAllowed.not());
        this.internalBrowser.getEngine().setJavaScriptEnabled(true);
        this.internalBrowser.getEngine().getLoadWorker().stateProperty().addListener((stateValue, oldState, newState) -> {

            if (newState == Worker.State.RUNNING) {
                this.progressIndicator.setProgress(-1d);
                this.getChildren().add(this.progressIndicator);
            } else {
                this.progressIndicator.setProgress(0);
                this.getChildren().remove(this.progressIndicator);
            }

            PresentationBrowser.this.injectBackend(this.getBackend());
            PresentationBrowser.this.injectServer(SlideshowFXServer.getSingleton());
        });
        this.internalBrowser.getEngine().setOnError(errorEvent -> {
            LOGGER.log(Level.SEVERE, "An error occurred in the internal browser", errorEvent.getException());
        });
        this.internalBrowser.getEngine().setOnAlert(event -> {
            DialogHelper.showAlert("SlideshowFX", event.getData());
        });
    }

    /**
     * Injects the backend object as member of the displayed page only if the given {@code backend} is not {@code null}
     * as well as the {@link #presentationProperty()}.
     * The backend is defined under the name returned by the
     * {@link TemplateConfiguration#getJsObject()} method of the current {@link #presentationProperty()}.
     * @param backend The backend object to inject into the page.
     */
    private final void injectBackend(Object backend) {
        if(backend != null
                && this.internalBrowser.getEngine().getLoadWorker().getState() == Worker.State.SUCCEEDED
                && this.getPresentation() != null
                && this.getPresentation().getTemplateConfiguration() != null
                && this.getPresentation().getTemplateConfiguration().getJsObject() != null) {

            JSObject window = (JSObject) this.internalBrowser.getEngine().executeScript("window");

            // Only inject the backend if it is not already present
            final Object member = window.getMember(this.getPresentation().getTemplateConfiguration().getJsObject());
            if("undefined".equals(member)) {
                window.setMember(PresentationBrowser.this.getPresentation().getTemplateConfiguration().getJsObject(), backend);
            }
        }
    }

    /**
     * Injects the {@code server} inside the displayed page under the named returned by the
     * {@link TemplateConfiguration#getSfxServerObject()} method for the current {@link #presentationProperty()} only if
     * the given {@code server}is not {@code null}.
     * @param server The server to inject within the displayed page.
     */
    private final void injectServer(final SlideshowFXServer server) {
        if(server != null
                && this.internalBrowser.getEngine().getLoadWorker().getState() == Worker.State.SUCCEEDED
                && this.getPresentation() != null
                && this.getPresentation().getTemplateConfiguration() != null
                && this.getPresentation().getTemplateConfiguration().getSfxServerObject() != null) {

            JSObject window = (JSObject) this.internalBrowser.getEngine().executeScript("window");

            // Only inject the server if it is not already present
            final Object member = window.getMember(this.getPresentation().getTemplateConfiguration().getSfxServerObject());
            if("undefined".equals(member)) {
                window.setMember(this.getPresentation().getTemplateConfiguration().getSfxServerObject(), server);
            }
        }
    }

    /**
     * Indicates if the user can interact with the internal browser, meaning click on it and so on.
     * @return The property indicating if user interaction is allowed for the internal browser.
     */
    public BooleanProperty interactionAllowedProperty() { return interactionAllowed; }

    /**
     * Indicates if the user can interact with the internal browser, meaning click on it and so on.
     * @return {@code true} if user interactions are allowed for the internal browser, {@code false} otherwise.
     */
    public boolean isInteractionAllowed() { return interactionAllowed.get(); }

    /**
     * Defines if user interactions are allowed for the internal browser.
     * @param interactionAllowed {@code true} if user interactions are allowed, {@code false} otherwise.
     */
    public void setInteractionAllowed(boolean interactionAllowed) { this.interactionAllowed.set(interactionAllowed); }

    /**
     * Loads the given presentation inside the browser.
     * @param presentation The presentation to load.
     */
    public final void loadPresentation(final PresentationEngine presentation) {
        this.loadPresentationAndDo(presentation, null);
    }

    public final void loadPresentationAndDo(final PresentationEngine presentation, Runnable action) {
        if(presentation != null) {
            this.presentation.set(presentation);

            final ChangeListener<Worker.State> stateListener = new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) {
                    if (newState != null && newState == Worker.State.SUCCEEDED) {
                        PresentationBrowser.this.internalBrowser.getEngine().getLoadWorker().stateProperty().removeListener(this);
                        PresentationBrowser.this.injectBackend(PresentationBrowser.this.getBackend());
                        PresentationBrowser.this.injectServer(SlideshowFXServer.getSingleton());

                        try {
                            if(action != null) action.run();
                        } catch(JSException jsex) {
                            LOGGER.log(Level.SEVERE, "Error while executing an action in the internal browser", jsex);
                        }
                    }
                }
            };

            this.internalBrowser.getEngine().getLoadWorker().stateProperty().addListener(stateListener);
            this.internalBrowser.getEngine().load(presentation.getConfiguration().getPresentationFile().toURI().toASCIIString());
        }
    }
    /**
     * Simply reloads the page displayed in the browser, not necessarily the {@link #presentationProperty()}.
     */
    public final void reload() {
        this.internalBrowser.getEngine().reload();
    }

    /**
     * Prints the current page displayed within the internal browser, not necessarily the {@link #presentationProperty()}.
     * If no printers are installed on the system, an awareness is displayed.
     */
    public final void print() {
        final PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null) {
            if (job.showPrintDialog(null)) {
                if(this.getPresentation().getArchive() != null) {
                    final String extension = ".".concat(this.getPresentation().getArchiveExtension());
                    final int indexOfExtension = this.getPresentation().getArchive().getName().indexOf(extension);
                    final String jobName = this.getPresentation().getArchive().getName().substring(0, indexOfExtension);
                    job.getJobSettings().setJobName(jobName);
                }

                job.getJobSettings().setPrintQuality(PrintQuality.HIGH);
                job.getJobSettings().setPageLayout(job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, 0, 0, 0, 0));

                this.internalBrowser.getEngine().print(job);
                job.endJob();
            } else {
                job.cancelJob();
            }
        } else {
            DialogHelper.showError("No printer", "There is no printer installed on your system.");
        }
    }

    /**
     * Get the current ID of the slide displayed. The ID is retrieved from the JavaScript method identified by the name
     * returned by the {@link TemplateConfiguration#getGetCurrentSlideMethod()} method of the current
     * {@link #presentationProperty()}.
     * @return The ID of the slide currently displayed, depending on the implementation of the JavaScript method for getting
     * it.
     */
    public final String getCurrentSlideId() {
        final String slideId = (String) this.internalBrowser.getEngine().executeScript(this.getPresentation().getTemplateConfiguration().getGetCurrentSlideMethod() + "();");
        return slideId;
    }

    /**
     * Defines the content of an element within a slide of the current {@link #presentationProperty()}. The {@code htmlContent}
     * must not be Base64 encoded.
     * The JavaScript method identified by the name returned by the {@link TemplateConfiguration#getContentDefinerMethod()}
     * of the current {@link #presentationProperty()} will be called to define the content.
     * @param slideNumber The number of the slide to define the content for an element.
     * @param elementName The name of the element to define the content for.
     * @param htmlContent The HTML content, not Base64 encoded, for the element to define.
     */
    public final void defineContent(final String slideNumber, final String elementName, final String htmlContent) {
        String clearedContent = Base64.getEncoder().encodeToString(htmlContent.getBytes(getDefaultCharset()));
        String jsCommand = String.format("%1$s(%2$s, \"%3$s\", '%4$s');",
                this.getPresentation().getTemplateConfiguration().getContentDefinerMethod(),
                slideNumber,
                elementName,
                clearedContent);

        this.internalBrowser.getEngine().executeScript(jsCommand);
    }

    /**
     * Updates the output of a console within the presentation. The {@code consoleLine} must not be Base64 encoded.
     * The JavaScript method identified by the name returned by the
     * {@link TemplateConfiguration#getUpdateCodeSnippetConsoleMethod()} method of the current {@link #presentationProperty()}
     * will be called in order to update the console output.
     * @param consoleOutputId The ID of the console to update.
     * @param consoleLine The line to insert in the console output.
     */
    public final void updateCodeSnippetConsole(final String consoleOutputId, final String consoleLine) {
        this.internalBrowser.getEngine().executeScript(
                            String.format("%1$s('%2$s', '%3$s');",
                                    this.getPresentation().getTemplateConfiguration().getUpdateCodeSnippetConsoleMethod(),
                                    consoleOutputId,
                                    Base64.getEncoder().encodeToString(consoleLine.getBytes(getDefaultCharset()))
                            ));
    }

    /**
     * Go to the slide identified by the given {@code slideId}.
     * @param slideId The ID of the slide to go to.
     */
    public void slide(final String slideId) {
        if(slideId != null) {
            this.internalBrowser.getEngine().executeScript(
                            String.format(
                                    "%1$s('%2$s');",
                                    this.getPresentation().getTemplateConfiguration().getGotoSlideMethod(),
                                    slideId
                            )
                    );
        }
    }

    /**
     * Go to the next slide of the presentation.
     */
    public final void nextSlide() {
        this.internalBrowser.getEngine().executeScript(
                    String.format(
                            "%1$s('%2$s')",
                            this.getPresentation().getTemplateConfiguration().getLeapMotionMethod(),
                            "RIGHT"
                    ));
    }

    /**
     * Go to the previous slide of the presentation.
     */
    public final void previousSlide() {
        this.internalBrowser.getEngine().executeScript(
                    String.format(
                            "%1$s('%2$s')",
                            this.getPresentation().getTemplateConfiguration().getLeapMotionMethod(),
                            "LEFT"
                    )
            );
    }
}

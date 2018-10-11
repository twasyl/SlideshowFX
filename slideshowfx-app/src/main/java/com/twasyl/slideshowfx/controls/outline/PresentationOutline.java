package com.twasyl.slideshowfx.controls.outline;

/*
 * Component holding an outline of a given {@link PresentationEngine}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.controls.PresentationBrowser;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.controls.outline.PresentationOutlineEvent.*;
import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getSnapshotDelay;
import static java.util.logging.Level.SEVERE;

/**
 * Component displaying the outline of a {@link PresentationEngine presentation}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class PresentationOutline extends ListView<ImageView> {

    private static Logger LOGGER = Logger.getLogger(PresentationOutline.class.getName());
    private final ObjectProperty<PresentationEngine> presentation = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<PresentationOutlineEvent>> slideMoved = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<PresentationOutlineEvent>> slideDeleted = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<PresentationOutlineEvent>> slideDeletionRequested = new SimpleObjectProperty<>();
    private final ReadOnlyBooleanProperty loading = new SimpleBooleanProperty(false);

    private Stage browserStage;
    private final PresentationBrowser browser = new PresentationBrowser();

    public PresentationOutline() {
        this.presentation.addListener((value, oldPresentation, newPresentation) -> {
            if (newPresentation != null) {
                this.reset();
                this.loadAllSlides();
            } else {
                this.reset();
            }
        });

        this.slideMoved.addListener((value, oldSlideMoved, newSlideMoved) -> {
            if (oldSlideMoved != null) {
                this.removeEventHandler(SLIDE_MOVED, oldSlideMoved);
            }
            if (newSlideMoved != null) {
                this.addEventHandler(SLIDE_MOVED, newSlideMoved);
            }
        });

        this.slideDeleted.addListener((value, oldSlideDeleted, newSlideDeleted) -> {
            if (oldSlideDeleted != null) {
                this.removeEventHandler(SLIDE_DELETED, oldSlideDeleted);
            }
            if (newSlideDeleted != null) {
                this.addEventHandler(SLIDE_DELETED, newSlideDeleted);
            }
        });

        this.slideDeletionRequested.addListener((value, oldRequest, newRequest) -> {
            if (oldRequest != null) {
                this.removeEventHandler(SLIDE_DELETION_REQUESTED, oldRequest);
            }
            if (newRequest != null) {
                this.addEventHandler(SLIDE_DELETION_REQUESTED, newRequest);
            }
        });

        this.browser.setInteractionAllowed(false);

        this.setCellFactory(param -> new PreviewCell());
        this.setPadding(Insets.EMPTY);
        this.setBorder(Border.EMPTY);
    }

    public ObjectProperty<PresentationEngine> presentationProperty() {
        return presentation;
    }

    public PresentationEngine getPresentation() {
        return presentation.get();
    }

    public void setPresentation(PresentationEngine presentation) {
        this.presentation.set(presentation);
    }

    public ReadOnlyBooleanProperty loadingProperty() {
        return loading;
    }

    public boolean isLoading() {
        return loading.get();
    }

    private void setLoading(final boolean loading) {
        ((SimpleBooleanProperty) this.loading).set(loading);
    }

    public void setOnSlideMoved(EventHandler<PresentationOutlineEvent> onSlideMoved) {
        this.slideMoved.set(onSlideMoved);
    }

    public void setOnSlideDeleted(EventHandler<PresentationOutlineEvent> onSlideDeleted) {
        this.slideDeleted.set(onSlideDeleted);
    }

    public void setOnSlideDeletionRequested(final EventHandler<PresentationOutlineEvent> event) {
        this.slideDeletionRequested.set(event);
    }

    /**
     * Add a preview of the given {@code slideId}.
     *
     * @param slideId Teh slide ID for which a preview should be created.
     */
    public void addPreview(final String slideId) {
        final Slide addedSlide = this.presentation.get().getConfiguration().getSlideById(slideId);

        if (addedSlide != null) {
            final ImageView newPreview = createPreview(slideId);
            final Slide after = this.presentation.get().getConfiguration().getSlideAfter(addedSlide.getSlideNumber());

            if (after != null) {
                final int previewIndex = this.findSlidePreviewIndex(after.getId());
                this.getItems().add(previewIndex, newPreview);
            } else {
                this.getItems().add(newPreview);
            }

            defineAndShowBrowserStage();
            this.browser.reloadAndDo(() -> {
                this.browser.slide(slideId);
                this.takeSnapshot(slideId, false, false);
            }, getSnapshotDelay());
        }
    }

    /**
     * Update the preview of the given {@code slideId}.
     *
     * @param slideId The slide ID for which to update the preview.
     */
    public void updatePreview(final String slideId) {
        defineAndShowBrowserStage();
        this.browser.reloadAndDo(() -> {
            this.browser.slide(slideId);
            this.takeSnapshot(slideId, false, false);
        }, getSnapshotDelay());
    }

    /**
     * Delete a slide preview for the given {@code slideId}. If the preview doesn't exist, nothing is performed.
     *
     * @param slideId The ID of the slide to delete the preview.
     */
    public void deletePreview(final String slideId) {
        final ImageView view = this.getItems()
                .stream()
                .filter(preview -> slideId.equals(preview.getUserData()))
                .findAny()
                .orElse(null);

        if (view != null) {
            this.getItems().remove(view);
            this.browser.reload();
            this.fireEvent(new PresentationOutlineEvent(SLIDE_DELETED, slideId, null));
        }
    }

    /**
     * Take a snapshot of the browser. According the value of the {@code recursive} parameter, snapshots of other
     * slides after the current one are taken.
     *
     * @param slideId   The current displayed slide.
     * @param recursive Indicates if a snapshot of slides after the current slide should be taken.
     */
    public void takeSnapshot(String slideId, boolean recursive, boolean javascriptCall) {
        if (!javascriptCall) {
            this.setLoading(true);
            this.browser.getInternalBrowser().getEngine().executeScript(
                    String.format("window.setTimeout(function() { sfx.takeSnapshot(slideshowFXGetCurrentSlide(), %1$s, true); }, %2$s);", recursive, getSnapshotDelay()));
        } else {
            if (!isLoading()) {
                this.setLoading(true);
            }

            try {
                final ImageView preview = this.findSlidePreview(slideId);

                if (preview != null) {
                    final Image snapshot = this.browser.snapshot(null, null);

                    if (snapshot != null) {
                        preview.setImage(snapshot);

                        if (preview.fitHeightProperty().isBound()) {
                            preview.fitHeightProperty().unbind();
                        }

                        double imageRatio = snapshot.getWidth() / snapshot.getHeight();
                        preview.fitHeightProperty().bind(preview.fitWidthProperty().divide(imageRatio));

                        if (recursive) {
                            final Slide slide = this.presentation.get().getConfiguration().getSlideById(slideId);
                            final Slide after = this.presentation.get().getConfiguration().getSlideAfter(slide.getSlideNumber());

                            if (after != null) {
                                this.browser.slide(after.getId());
                                this.takeSnapshot(after.getId(), true, false);
                            } else {
                                closeBrowserStage();
                                this.setLoading(false);
                            }
                        } else {
                            closeBrowserStage();
                            this.setLoading(false);
                        }
                    }
                } else {
                    this.setLoading(false);
                }
            } catch (Exception e) {
                LOGGER.log(SEVERE, "Can't take snapshot of browser", e);
                this.setLoading(false);
            }
        }
    }

    /**
     * Get the slide ID at the given index.
     *
     * @param index The index to get the slide ID for.
     * @return The slide ID at the given index or {@code null} if the index is invalid.
     */
    public String getSlideIdAtIndex(final int index) {
        String slideId = null;

        try {
            final ImageView preview = this.getItems().get(index);
            slideId = (String) preview.getUserData();
        } catch (IndexOutOfBoundsException e) {
            LOGGER.log(Level.INFO, "Invalid index for getting the slide ID", e);
        } finally {
            return slideId;
        }
    }

    private void reset() {
        this.getItems().clear();
    }

    /**
     * Loads all the slides of this presentation and take care of starting the process of cascading snapshots of it.
     */
    private void loadAllSlides() {
        if (this.presentation.get() != null && this.presentation.get().getConfiguration().hasSlides()) {
            defineAndShowBrowserStage();

            this.presentation.get().getConfiguration().getSlides().forEach(slide -> {
                final ImageView preview = this.createPreview(slide.getId());
                this.getItems().add(preview);
            });

            final String firstSlideId = this.presentation.get().getConfiguration().getFirstSlide().getId();
            this.browser.loadPresentationAndDo(this.presentation.get(), () -> {
                this.browser.slide(firstSlideId);
                this.takeSnapshot(firstSlideId, true, false);
            }, getSnapshotDelay());
        }
    }

    /**
     * The {@link #browserStage} will be created with proper configuration. If the
     * stage isn't {@code null} when calling this method, this method will take care of closing it before recreating it.
     */
    private void defineAndShowBrowserStage() {
        closeBrowserStage();

        this.browser.setBackend(this);
        this.browser.setSpinnerAllowed(false);

        final Scene scene = new Scene(this.browser);
        browserStage = new Stage();
        browserStage.setTitle("SlideshowFX");
        browserStage.setScene(scene);
        browserStage.setIconified(false);
        browserStage.setMaximized(true);
        browserStage.setOpacity(0);
        browserStage.show();
        SlideshowFX.getStage().requestFocus();
    }

    /**
     * Close the stage hosting the browser if it is not {@code null}.
     */
    private void closeBrowserStage() {
        if (this.browserStage != null) {
            this.browserStage.getScene().setRoot(new Region());
            this.browserStage.close();
            this.browserStage = null;
        }
    }

    /**
     * Create a {@link ImageView} that will host the preview of a slide. The image is not set by this method. The given
     * {@code slideId} will be set as {@link ImageView#setUserData(Object)}.
     *
     * @param slideId The slide ID that the created {@link ImageView} will host.
     * @return A well created {@link ImageView}.
     */
    private ImageView createPreview(final String slideId) {
        final ImageView preview = new ImageView();
        preview.setUserData(slideId);
        preview.setPreserveRatio(true);
        preview.fitWidthProperty().bind(this.widthProperty());

        return preview;
    }

    /**
     * Find the {@link ImageView} hosting the preview of the given {@code slideId}.
     *
     * @param slideId The slide ID for which find the preview.
     * @return The {@link ImageView} for the given {@code slideId} or {@code null} if it not found.
     */
    private ImageView findSlidePreview(final String slideId) {
        return this.getItems()
                .stream()
                .filter(preview -> slideId.equals(preview.getUserData()))
                .findAny()
                .orElse(null);
    }

    /**
     * Find the index of the {@link ImageView} hosting the preview of the given {@code slideId}.
     *
     * @param slideId The slide ID for which find the index's preview.
     * @return The index of the control hosting the given slide ID or {@code -1} if it's not found.
     */
    private int findSlidePreviewIndex(final String slideId) {
        int previewIndex = -1;

        for (int index = 0; previewIndex == -1 && index < this.getItems().size(); index++) {
            if (slideId.equals(this.getItems().get(index).getUserData())) {
                previewIndex = index;
            }
        }

        return previewIndex;
    }
}

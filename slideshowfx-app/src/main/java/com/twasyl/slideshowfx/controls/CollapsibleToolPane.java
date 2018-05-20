package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.icons.FontAwesome;
import javafx.animation.TranslateTransition;
import javafx.beans.property.*;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import static javafx.geometry.HPos.LEFT;
import static javafx.geometry.HPos.RIGHT;

/**
 * This pane is used to display icons. If an icon is triggered, the content associated to this this icon is displayed
 * by translating in the scene.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class CollapsibleToolPane extends Region {

    private final ObjectProperty<Region> content = new SimpleObjectProperty<>();
    private final ReadOnlyBooleanProperty collapsed = new SimpleBooleanProperty(true);
    private final ReadOnlyObjectProperty<HPos> position = new SimpleObjectProperty<>(RIGHT);

    private final VBox toolbar = new VBox(5);
    private final ToggleGroup iconsGroup = new ToggleGroup();

    public CollapsibleToolPane() {
        this.getStylesheets().add(CollapsibleToolPane.class.getResource("/com/twasyl/slideshowfx/css/collapsible-tool-pane.css").toExternalForm());

        /* Ensure that when the scene is shown, the panel is placed completely
         * on the right/left of the screen, only displaying the toolbar
         */
        this.sceneProperty().addListener((sceneValue, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((widthValue, oldValue, newWidth) -> {
                    this.placeAccordingSceneAndPosition();
                });
            }
        });

        this.positionProperty().addListener((value, oldPos, newPos) -> {
            this.placeAccordingSceneAndPosition();
        });

        this.toolbar.setLayoutX(0);
        this.toolbar.setLayoutY(0);

        // Ensure the content is always next to the toolbar
        this.content.addListener((value, oldContent, newContent) -> {
            if (oldContent != null) {
                this.getChildren().remove(oldContent);
            }

            if (newContent != null) {
                if (this.getPosition() == RIGHT) {
                    newContent.layoutXProperty().bind(this.toolbar.widthProperty());
                } else {
                    newContent.layoutXProperty().bind(this.toolbar.layoutXProperty());
                }

                newContent.setLayoutY(0);

                this.getChildren().add(newContent);
                this.layout();
            }
        });

        this.getChildren().add(this.toolbar);
    }

    /**
     * Indicates if this panel is collapse or not.
     *
     * @return The property indicating if this panel is collapsed.
     */
    public ReadOnlyBooleanProperty collapsedProperty() {
        return collapsed;
    }

    /**
     * Indicates if this panel is collapse or not.
     *
     * @return <code>true</code> if this panel is collapsed, <code>false</code> otherwise.
     */
    public boolean isCollapsed() {
        return collapsed.get();
    }

    /**
     * Get the position of this pane.
     *
     * @return The property corresponding to the position of this pane.
     */
    public ReadOnlyObjectProperty<HPos> positionProperty() {
        return position;
    }

    /**
     * Get the position of this pane.
     *
     * @return The position of this pane.
     */
    public HPos getPosition() {
        return position.get();
    }

    /**
     * Set the position of this pane. The position can only be {@link HPos#LEFT} or {@link HPos#RIGHT}.
     *
     * @throws IllegalArgumentException If the position is not valid.
     */
    public void setPosition(HPos position) {
        if (position == RIGHT || position == LEFT) {
            ((SimpleObjectProperty) this.position).set(position);
        } else {
            throw new IllegalArgumentException("Invalid position " + position.name() + ". Only RIGHT or LEFT allowed");
        }
    }

    /**
     * Adds an icon associated to its content to this panel.
     *
     * @param icon    The icon that will always be visible in the toolbar.
     * @param content The content that will be displayed when the icon is triggered.
     * @return Return this panel.
     */
    public CollapsibleToolPane addContent(final FontAwesome icon, final Region content) {
        final ToggleButton button = new ToggleButton();
        button.setToggleGroup(this.iconsGroup);
        button.setGraphic(icon);

        button.setOnAction(event -> {

            // If the panel is already opened, close it and only open it if the current content is different of the given content
            if (!this.isCollapsed()) {
                double byX = this.content.get().getWidth();

                if(this.getPosition() == RIGHT) {
                    byX += this.toolbar.getWidth();
                } else {
                    byX -= this.toolbar.getWidth();
                }

                final TranslateTransition translation = new TranslateTransition(Duration.millis(500), getNodeToTranslate());
                translation.setByX(byX);

                translation.setOnFinished(animationEvent -> {
                    ((SimpleBooleanProperty) this.collapsedProperty()).set(true);

                    // Reopen the panel if this content is different from the given one
                    if (this.content.get() != content) {
                        this.content.set(content);

                        double newContentByX = this.content.get().getWidth();
                        if(this.getPosition() == RIGHT) {
                            newContentByX *= -1;
                        } else {
                            newContentByX += this.toolbar.getWidth();
                        }

                        final TranslateTransition internalTranslation = new TranslateTransition(Duration.millis(500), getNodeToTranslate());
                        internalTranslation.setByX(newContentByX);

                        internalTranslation.setOnFinished(internalAnimationEvent -> ((SimpleBooleanProperty) this.collapsedProperty()).set(false));

                        internalTranslation.play();
                    }

                });

                translation.play();
            } else {
                this.content.set(content);

                double byX = this.content.get().getWidth();
                if(this.getPosition() == RIGHT) {
                    byX *= -1;
                } else {
                    byX += this.toolbar.getWidth();
                }

                final TranslateTransition translation = new TranslateTransition(Duration.millis(500), getNodeToTranslate());
                translation.setByX(byX);

                translation.setOnFinished(animationEvent -> ((SimpleBooleanProperty) this.collapsedProperty()).set(false));

                translation.play();
            }
        });

        this.toolbar.getChildren().add(button);

        return this;
    }

    private Node getNodeToTranslate() {
        if (getPosition() == RIGHT) {
            return this;
        } else {
            return this.content.get();
        }
    }

    private void placeAccordingSceneAndPosition() {
        if (this.getPosition() != null) {
            if (getPosition() == RIGHT && this.getScene() != null) {
                this.setTranslateX(this.getScene().getWidth() - this.toolbar.getWidth());
            } else if (getPosition() == LEFT) {
               // this.setTranslateX(-this.toolbar.getWidth());
            }
        }
    }
}

package com.twasyl.slideshowfx.controls;

import javafx.animation.FadeTransition;
import javafx.beans.property.*;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
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

    private static final PseudoClass LEFT_STATE = PseudoClass.getPseudoClass("left");
    private static final PseudoClass RIGHT_STATE = PseudoClass.getPseudoClass("right");

    private final ObjectProperty<Region> content = new SimpleObjectProperty<>();
    private final DoubleProperty contentWidth = new SimpleDoubleProperty(Double.NaN);
    private final ReadOnlyDoubleProperty toolbarWidth = new SimpleDoubleProperty();
    private final ReadOnlyBooleanProperty collapsed = new SimpleBooleanProperty(true);
    private final ReadOnlyObjectProperty<HPos> position = new SimpleObjectProperty<>();

    private final VBox toolbar = new VBox(5);
    private final ToggleGroup iconsGroup = new ToggleGroup();

    public CollapsibleToolPane() {
        this.getStyleClass().add("collapsible-tool-pane");
        this.getStylesheets().add(CollapsibleToolPane.class.getResource("/com/twasyl/slideshowfx/css/collapsible-tool-pane.css").toExternalForm());

        ((SimpleDoubleProperty) this.toolbarWidth).bind(this.toolbar.widthProperty());

        /* Ensure that when the scene is shown, the panel is placed completely
         * on the right/left of the screen, only displaying the toolbar
         */
        this.parentProperty().addListener((parentValue, oldParent, newParent) -> {
            if (newParent != null && newParent instanceof Region) {
                ((Region) newParent).widthProperty().addListener((widthValue, oldValue, newWidth) -> {
                    this.placeToolbarAccordingParentAndPosition();
                });
            }
        });

        this.positionProperty().addListener((value, oldPos, newPos) -> {
            this.toolbar.getChildren().forEach(button -> this.setPseudoClassAccordingPosition(button));
            this.placeToolbarAccordingParentAndPosition();
        });

        this.toolbar.setLayoutY(0);

        // Ensure the content is always next to the toolbar
        this.content.addListener((value, oldContent, newContent) -> {
            if (oldContent != null) {
                this.getChildren().remove(oldContent);
            }

            if (newContent != null) {
                if (this.getPosition() == RIGHT) {
                    newContent.layoutXProperty().bind(this.toolbar.layoutXProperty().subtract(newContent.widthProperty()));
                } else {
                    newContent.layoutXProperty().bind(this.toolbar.layoutXProperty().add(this.toolbar.widthProperty()));
                }

                newContent.setLayoutY(0);

                if (!this.contentWidthProperty().getValue().isNaN()) {
                    newContent.prefWidthProperty().bind(this.contentWidthProperty());
                }

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
     * @return {@code true} if this panel is collapsed, {@code false} otherwise.
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
     * Get the width of the toolbar, i.e. the container hosting all icons.
     *
     * @return The width of the toolbar.
     */
    public ReadOnlyDoubleProperty toolbarWidthProperty() {
        return toolbarWidth;
    }

    /**
     * Get the width of the toolbar, i.e. the container hosting all icons.
     *
     * @return The width of the toolbar.
     */
    public double getToolbarWidth() {
        return toolbarWidth.get();
    }

    /**
     * Get the width the content of the pane should have. Default value is {@link Double#NaN} meaning the content will
     * keep it's original size.
     *
     * @return The width the content of the pane should have.
     */
    public DoubleProperty contentWidthProperty() {
        return contentWidth;
    }

    /**
     * Get the width the content of the pane should have. Default value is {@link Double#NaN} meaning the content will
     * keep it's original size.
     *
     * @return The width the content of the pane should have.
     */
    public double getContentWidth() {
        return contentWidth.get();
    }

    /**
     * Set the width the content of the pane should have. To reset the width, the value {@link Double#NaN} can be used.
     */
    public void setContentWidth(double contentWidth) {
        this.contentWidth.set(contentWidth);
    }

    /**
     * Adds an icon associated to its content to this panel.
     *
     * @param icon    The icon that will always be visible in the toolbar.
     * @param content The content that will be displayed when the icon is triggered.
     * @return Return this panel.
     */
    public CollapsibleToolPane addContent(final Node icon, final Region content) {
        final ToggleButton button = new ToggleButton();
        button.setToggleGroup(this.iconsGroup);
        button.setGraphic(icon);
        button.setFocusTraversable(false);
        this.setPseudoClassAccordingPosition(button);

        button.setOnAction(event -> {
            // If the panel is already opened, close it and only open it if the current content is different of the given content
            if (this.isCollapsed()) {
                this.content.set(content);
                displayContent(content);
            } else {
                displayContent(content);
            }
        });

        this.toolbar.getChildren().add(button);

        return this;
    }

    /**
     * Adds an icon associated to its content to this panel.
     *
     * @param text    The text acting as icon that will always be visible in the toolbar.
     * @param content The content that will be displayed when the icon is triggered.
     * @return Return this panel.
     */
    public CollapsibleToolPane addContent(final String text, final Region content) {
        final Label icon = new Label(text);
        if (getPosition() == LEFT) {
            icon.setRotate(-90);
        } else {
            icon.setRotate(90);
        }

        return this.addContent(new Group(icon), content);
    }

    /**
     * Display the given content according the {@link #isCollapsed() state} of this pane. If a content is already
     * displayed, the pane will be collapsed and opened again with the new content.
     *
     * @param content The content to be displayed.
     */
    private void displayContent(final Region content) {
        final FadeTransition transition = new FadeTransition(Duration.millis(100));
        transition.setFromValue(this.isCollapsed() ? 0 : 1);
        transition.setToValue(this.isCollapsed() ? 1 : 0);
        transition.setNode(content);

        if (this.isCollapsed()) {
            transition.setOnFinished(event -> ((SimpleBooleanProperty) this.collapsedProperty()).set(false));
        } else {
            transition.setOnFinished(event -> {
                ((SimpleBooleanProperty) this.collapsedProperty()).set(true);

                // Reopen the panel if this content is different from the given one
                if (this.content.get() != content) {
                    this.content.set(content);
                    displayContent(content);
                }
            });
        }

        transition.play();
    }

    /**
     * Places the {@link #toolbar} according the parent's position.
     */
    private void placeToolbarAccordingParentAndPosition() {
        if (this.getPosition() != null) {
            if (this.getParent() != null && this.getParent() instanceof Region) {
                final Region parent = (Region) this.getParent();

                if (getPosition() == RIGHT) {
                    this.toolbar.setLayoutX(parent.getWidth() - this.toolbar.getWidth());
                } else if (getPosition() == LEFT) {
                    this.toolbar.setLayoutX(0);
                }
            }
        }
    }

    /**
     * Set the correct {@link PseudoClass} on the given node according the {@link #getPosition() position} of the pane.
     *
     * @param node The node to set the {@link PseudoClass} on.
     */
    private void setPseudoClassAccordingPosition(final Node node) {
        if (getPosition() == RIGHT) {
            node.pseudoClassStateChanged(RIGHT_STATE, true);
            node.pseudoClassStateChanged(LEFT_STATE, false);
        } else if (getPosition() == LEFT) {
            node.pseudoClassStateChanged(LEFT_STATE, true);
            node.pseudoClassStateChanged(RIGHT_STATE, false);
        }
    }
}

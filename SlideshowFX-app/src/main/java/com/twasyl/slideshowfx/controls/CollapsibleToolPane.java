package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.utils.ResourceHelper;
import de.jensd.fx.glyphs.GlyphIcon;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * This pane is used to display icons. If an icon is triggered, the content associated to this this icon is displayed
 * by translating in the scene.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class CollapsibleToolPane extends Region {

    private final ObjectProperty<Region> content = new SimpleObjectProperty<>();
    private final ReadOnlyBooleanProperty collapsed = new SimpleBooleanProperty(true);
    private final VBox toolbar = new VBox(5);
    private final ToggleGroup iconsGroup = new ToggleGroup();

    public CollapsibleToolPane() {
        this.getStylesheets().add(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/css/collapsible-tool-pane.css"));

        /* Ensure that when the scene is shown, the panel is placed completely
         * on the right of the screen, only displaying the toolbar
         */
        this.sceneProperty().addListener((sceneValue, oldScene, newScene) -> {
            if(newScene != null) {
                newScene.widthProperty().addListener((widthValue, oldValue, newWidth) -> {
                    if (newWidth != null) {
                        this.setTranslateX(newWidth.doubleValue() - this.toolbar.getWidth());
                    }
                });
            }
        });

        this.toolbar.setLayoutX(0);
        this.toolbar.setLayoutY(0);

        // Ensure the content is always next to the toolbar
        this.content.addListener((value, oldContent, newContent) -> {
            if(oldContent != null) {
                this.getChildren().remove(oldContent);
            }

            if(newContent != null) {
                newContent.layoutXProperty().bind(this.toolbar.widthProperty());
                newContent.setLayoutY(0);

                this.getChildren().add(newContent);
                this.layout();
            }
        });

        this.getChildren().add(this.toolbar);
    }

    /**
     * Indicates if this panel is collapse or not.
     * @return The property indicating if this panel is collapsed.
     */
    public ReadOnlyBooleanProperty collapsedProperty() { return collapsed; }

    /**
     * Indicates if this panel is collapse or not.
     * @return <code>true</code> if this panel is collapsed, <code>false</code> otherwise.
     */
    public boolean isCollapsed() { return collapsed.get(); }

    /**
     * Adds an icon associated to its content to this panel.
     * @param icon The icon that will always be visible in the toolbar.
     * @param content The content that will be displayed when the icon is triggered.
     * @return Return this panel.
     */
    public CollapsibleToolPane addContent(final GlyphIcon icon, final Region content) {
        final ToggleButton button = new ToggleButton();
        button.setToggleGroup(this.iconsGroup);
        button.setGraphic(icon);

        button.setOnAction(event -> {

            // If the panel is already opened, close it and only open it if the current content is different of the given content
            if(!this.isCollapsed()) {
                final TranslateTransition translation = new TranslateTransition(Duration.millis(500), this);
                translation.setByX(this.content.get().getWidth());

                translation.setOnFinished(animationEvent -> {
                    ((SimpleBooleanProperty) this.collapsedProperty()).set(true);

                    // Reopen the panel if this content is different from the given one
                    if(this.content.get() != content) {
                        this.content.set(content);

                        final TranslateTransition internalTranslation = new TranslateTransition(Duration.millis(500), this);
                        internalTranslation.setByX(-this.content.get().getWidth());

                        internalTranslation.setOnFinished(internalAnimationEvent -> {
                            ((SimpleBooleanProperty) this.collapsedProperty()).set(false);
                        });

                        internalTranslation.play();
                    }

                });

                translation.play();
            } else {
                this.content.set(content);

                final TranslateTransition translation = new TranslateTransition(Duration.millis(500), this);
                translation.setByX(-this.content.get().getWidth());

                translation.setOnFinished(animationEvent -> {
                    ((SimpleBooleanProperty) this.collapsedProperty()).set(false);
                });

                translation.play();
            }
        });

        this.toolbar.getChildren().add(button);

        return this;
    }
}

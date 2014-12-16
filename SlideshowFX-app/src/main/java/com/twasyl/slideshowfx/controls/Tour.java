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

package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.utils.PlatformHelper;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * This class allows to create a guided tour for a given screen. A list of {@link com.twasyl.slideshowfx.controls.Tour.Step}
 * is provided and must be filled. They are displayed when the user * uses the <code>RIGHT</code> arrow key to move
 * forward and the <code>LEFT</code> arrow key to move backward. The <code>ESCAPE</code> key is used to exit the tour.
 * In order to start the tour, the {@link #start()} method must be called. To end the tour, the user must hit the
 * <code>ESCAPE</code> key or the {@link #end()} method must be called.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class Tour extends StackPane {

    public static class Step {
        private String selector;
        private String tooltip;

        public Step(String selector, String tooltip) {
            this.selector = selector;
            this.tooltip = tooltip;
        }

        public String getSelector() { return selector; }
        public void setSelector(String selector) { this.selector = selector; }

        public String getTooltip() { return tooltip; }
        public void setTooltip(String tooltip) { this.tooltip = tooltip; }
    }

    private ObservableList<Step> steps = FXCollections.observableArrayList();
    private int currentStep = -1;

    private Scene scene;
    private Parent initialParent;

    private Group tourGroup = new Group();
    private Rectangle tourBackground;
    private Rectangle tourHighlight;
    private Tooltip tourTooltip;

    public Tour(Scene scene) {
        this.scene = scene;
        this.setAlignment(Pos.TOP_LEFT);
    }

    public Tour addStep(Step step) {
        this.steps.add(step);
        return this;
    }

    /**
     * Start the tour. This method is mandatory in order to display the tour. It initializes the graphical elements that
     * are needed to display the tour (the background, the highlight). Keys for navigating in the tour are also defined
     * in this method.
     */
    public void start() {
        this.initialParent = this.scene.getRoot();

        // Initialize the background
        this.tourBackground = new Rectangle(0, 0, this.scene.getWidth(), this.scene.getHeight());
        this.tourBackground.setFill(Color.WHITE);
        this.tourBackground.setOpacity(0);

        // Initialize the highlight
        this.tourHighlight = new Rectangle(0, 0, 1, 1);
        this.tourHighlight.setFill(Color.BLACK);
        this.tourHighlight.setOpacity(1);

        // Start tour screen with information
        final Node instructionsPane = this.getInstructionsNode();

        // Initialize the Tooltip
        this.tourTooltip = new Tooltip();
        this.tourTooltip.setAutoHide(false);
        this.tourTooltip.setHideOnEscape(false);
        this.tourTooltip.setWrapText(true);
        this.tourTooltip.setStyle("-fx-font-size: 20pt;");
        this.tourTooltip.setMaxWidth(this.initialParent.getLayoutBounds().getWidth() - 10);
        Tooltip.install(this.tourHighlight, this.tourTooltip);

        // Group all
        this.tourGroup = new Group(this.tourBackground, this.tourHighlight);
        this.tourGroup.setBlendMode(BlendMode.DIFFERENCE);

        // Set the next and previous listeners
        this.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.RIGHT) this.next();
            else if (event.getCode() == KeyCode.LEFT) this.previous();
            else if (event.getCode() == KeyCode.ESCAPE) this.end();
        });

        // Prepare transitions for fade in
        final FadeTransition tourBackgroundTransition = new FadeTransition(Duration.millis(300), this.tourBackground);
        tourBackgroundTransition.setToValue(0.3);

        PlatformHelper.run(() -> {
            this.getChildren().addAll(this.initialParent, this.tourGroup);
            this.scene.setRoot(this);
            this.requestFocus();

            tourBackgroundTransition.play();
            tourBackgroundTransition.setOnFinished(event -> this.moveHighlight(null, this.getInstructionsNode()));
        });
    }

    /**
     * End the tour and restore the initial view of the scene.
     */
    public void end() {
        PlatformHelper.run(() -> {
            Tooltip.uninstall(this.tourHighlight, this.tourTooltip);
            this.tourTooltip.hide();

            final FadeTransition tourBackgroundFadeOut = new FadeTransition(Duration.millis(300), this.tourBackground);
            tourBackgroundFadeOut.setToValue(0);

            final FadeTransition tourHighlightFadeOut = new FadeTransition(Duration.millis(300), this.tourHighlight);
            tourHighlightFadeOut.setToValue(0);

            final ParallelTransition globalFadeOut = new ParallelTransition(tourBackgroundFadeOut, tourHighlightFadeOut);
            globalFadeOut.setOnFinished(event -> {
                PlatformHelper.run(() -> {
                    this.getChildren().remove(this.initialParent);
                    this.scene.setRoot(this.initialParent);
                });
            });

            globalFadeOut.play();
        });
    }

    /**
     * Go to the next step of the tour. When the end of the tour is reach, the reload screen is displayed.
     */
    public synchronized void next() {
        if(this.currentStep < this.steps.size() - 1) {
            this.moveHighlight(this.steps.get(++this.currentStep), null);
        } else {
            this.moveHighlight(null, this.getReloadNode());
        }
    }

    /**
     * Go to the previous step of the tour. When the beginning of the tour is reached, the instructions screen is
     * displayed.
     */
    public synchronized void previous() {
        if(this.currentStep > 0) {
            this.moveHighlight(this.steps.get(--this.currentStep), null);
        } else if(this.currentStep == 0) {
            this.moveHighlight(null, this.getInstructionsNode());
        }
    }

    /**
     * Reload the tour by displaying the first step of it.
     */
    public synchronized void reload() {
        this.currentStep = -1;
        this.next();
    }

    /**
     * Moves the highlight to the target represented by {@code step.getSelector()}. The additional {@code graphic} is added
     * to the tooltip as part of the {@code step.getTooltip()}. Both {@code step} and {@code graphic} could be null, but
     * the displayed result may not be accurate.
     * @param step
     * @param graphic
     */
    private synchronized void moveHighlight(final Step step, final Node graphic) {
        final Node target = step != null ? this.initialParent.lookup(step.getSelector()) : null;

        this.tourTooltip.hide();

        final double scaleToX = target == null ? 1 : (target.getLayoutBounds().getWidth() + 10) / this.tourHighlight.getWidth();
        final double scaleToY = target == null ? 1 : (target.getLayoutBounds().getHeight() + 10) / this.tourHighlight.getHeight();

        final ScaleTransition scale = new ScaleTransition(Duration.millis(500), this.tourHighlight);
        scale.setToX(scaleToX);
        scale.setToY(scaleToY);

        final Bounds targetBounds = target == null ? new BoundingBox(0, 0, 0, 0) :
                                                     target.localToScene(target.getLayoutBounds());

        final TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), this.tourHighlight);
        translateTransition.setToX(targetBounds.getMinX() + scaleToX / 2 - 5);
        translateTransition.setToY(targetBounds.getMinY() + scaleToY / 2 - 5);

        final ParallelTransition parallelTransition = new ParallelTransition(scale, translateTransition);
        parallelTransition.setOnFinished(event -> this.updateTooltip(step, graphic));

        PlatformHelper.run(() -> parallelTransition.play());
    }

    /**
     * Update the tooltip with the given {@code step} and {@code graphic}. If the {@code step} is null or it's text, the
     * text of the tooltip is set to {@code null}. If the {@code graphic} is null, then the graphic of the tooltip will also be null.
     * @param step The step to display the tooltip for. May be null.
     * @param graphic The graphic that is set to the tooltip. May be null
     */
    private synchronized void updateTooltip(final Step step, final Node graphic) {
        if(step == null || step.getTooltip() == null) this.tourTooltip.setText(null);
        else  this.tourTooltip.setText(step.getTooltip());

        this.tourTooltip.setGraphic(graphic);

        this.tourTooltip.show(this.scene.getWindow());

        final double anchorX = this.scene.getWindow().getX() + 5 + this.initialParent.getLayoutBounds().getWidth() / 2 - this.tourTooltip.getWidth() / 2;
        final double anchorY = this.scene.getWindow().getY() + 5 + this.initialParent.getLayoutBounds().getHeight() / 2 - this.tourTooltip.getHeight() / 2;
        this.tourTooltip.setAnchorX(anchorX);
        this.tourTooltip.setAnchorY(anchorY);
    }

    /**
     * Create the node that contains the instructions to use the tour.
     * @return The Node containing all the instructions necessary to use the tour.
     */
    private Node getInstructionsNode() {
        final ImageView escKey = new ImageView(getClass().getResource("/com/twasyl/slideshowfx/images/esc_key.png").toExternalForm());
        final ImageView leftKey = new ImageView(getClass().getResource("/com/twasyl/slideshowfx/images/left_key.png").toExternalForm());
        final ImageView rightKey = new ImageView(getClass().getResource("/com/twasyl/slideshowfx/images/right_key.png").toExternalForm());

        final Label escLabel = new Label("Exit the tour");
        escLabel.setLabelFor(escKey);
        escLabel.setStyle("-fx-text-fill: white;");

        final Label leftLabel = new Label("Previous step of the tour");
        leftLabel.setLabelFor(leftKey);
        leftLabel.setStyle("-fx-text-fill: white;");

        final Label rightLabel = new Label("Next step of the tour");
        rightLabel.setLabelFor(rightKey);
        rightLabel.setStyle("-fx-text-fill: white;");

        final GridPane instructionsPane = new GridPane();
        instructionsPane.addColumn(0, escKey, leftKey, rightKey);
        instructionsPane.addColumn(1, escLabel, leftLabel, rightLabel);

        return instructionsPane;
    }

    /**
     * Create the Node that displays the reload screen.
     * @return The Node for displaying the reload screen.
     */
    private Node getReloadNode() {
        final ImageView reloadIcon = new ImageView(getClass().getResource("/com/twasyl/slideshowfx/images/reload_white.png").toExternalForm());

        final Button  reloadButton = new Button();
        reloadButton.setGraphic(reloadIcon);
        reloadButton.setOnAction(event -> reload());

        return reloadButton;
    }
}

package com.twasyl.slideshowfx.controls.builder.elements;

import javafx.animation.FadeTransition;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default behavior and implementation for {@see ITemplateElement}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractTemplateElement<T extends Object> extends HBox implements ITemplateElement<T> {

    private static final Logger LOGGER = Logger.getLogger(AbstractTemplateElement.class.getName());

    protected final StringProperty name = new SimpleStringProperty();
    protected final ObjectProperty<T> value = new SimpleObjectProperty<>();
    protected final ObjectProperty<Path> workingPath = new SimpleObjectProperty<>();
    protected final BooleanProperty deletable = new SimpleBooleanProperty(true);

    protected final Label text = new Label();
    protected final Button delete = new Button();

    public AbstractTemplateElement() {
        this.name.addListener((value, oldName, newName) -> {
            if(newName == null || newName.isEmpty()) AbstractTemplateElement.this.text.setText("");
            else AbstractTemplateElement.this.text.setText(newName + ":");
        });

        final Image deleteIcon = new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/round_delete.png"));
        this.delete.setGraphic(new ImageView(deleteIcon));
        this.delete.getStyleClass().add("template-element-delete-button");
        this.delete.setTooltip(new Tooltip("Delete this element"));
        this.delete.setOpacity(0);
        this.delete.setTranslateY(-10);
        this.delete.setTranslateX(15);

        this.delete.setOnAction(event -> {
            if(AbstractTemplateElement.this.getParent() != null && AbstractTemplateElement.this.getParent() instanceof Pane) {
                ((Pane) AbstractTemplateElement.this.getParent()).getChildren().remove(this);
            }
        });

        this.delete.setOnMouseEntered(event -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), this.delete);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            fadeIn.play();
        });

        this.delete.setOnMouseExited(event -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), this.delete);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            fadeOut.play();
        });

        this.deletable.addListener((value, oldValue, newValue) -> {
            if(newValue != null) {
                this.delete.setDisable(!newValue);
                this.delete.setOpacity(0);
            }
        });

        this.getChildren().addAll(this.delete, this.text);
        this.setSpacing(5);
        this.setAlignment(Pos.BASELINE_LEFT);
    }

    @Override
    public StringProperty nameProperty() { return this.name; }

    @Override
    public String getName() {return this.nameProperty().get(); }

    @Override
    public void setName(String name) { this.nameProperty().set(name); }

    @Override
    public ObjectProperty<T> valueProperty() { return this.value; }

    @Override
    public T getValue() { return this.valueProperty().get(); }

    @Override
    public void setValue(T value) { this.valueProperty().set(value); }

    @Override
    public ObjectProperty<Path> workingPathProperty() { return workingPath; }

    @Override
    public Path getWorkingPath() { return workingPath.get(); }

    @Override
    public void setWorkingPath(Path workingPath) {
        if (workingPath != null) {
            try {
                this.workingPath.set(workingPath.toRealPath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not determine real path", e);
            }
        }
    }

    @Override
    public BooleanProperty deletableProperty() { return this.deletable; }

    @Override
    public boolean isDeletable() { return this.deletable.get(); }

    @Override
    public void setDeletable(boolean deletable) { this.deletable.set(deletable); }

    @Override
    public void appendContent(Node ... nodes) {
        if(nodes == null) throw new NullPointerException("At least one node should be provided");

        Arrays.stream(nodes).forEach(node -> {
            if (node == null) throw new NullPointerException("Can not add a null element to the template element");
            this.getChildren().add(/*this.getChildren().size() - 1, */node);
        });
    }
}

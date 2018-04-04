package com.twasyl.slideshowfx.controls.builder.nodes;

import com.twasyl.slideshowfx.icons.FontAwesome;
import com.twasyl.slideshowfx.icons.Icon;
import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import com.twasyl.slideshowfx.utils.DialogHelper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isInteger;
import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isNotEmpty;

/**
 * Control allowing to define a slide. The control extends the {@link TitledPane} in order to properly be displayed
 * within the editor.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.3
 */
public class SlideDefinition extends TitledPane {
    private static final Logger LOGGER = Logger.getLogger(SlideDefinition.class.getName());

    private ExtendedTextField slideId = new ExtendedTextField("ID", true, 2);
    private ExtendedTextField name = new ExtendedTextField("Name", true);
    private ExtendedTextField file = new ExtendedTextField("File", true);
    private Button delete = new Button();
    private Button addSlideElementButton = new Button();
    private VBox slideElementsPane = new VBox(5);
    private List<SlideElementDefinition> slideElements = new ArrayList<>();

    public SlideDefinition() {
        this.textProperty().bind(this.name.textProperty());
        this.setCollapsible(true);

        this.initializeMandatoryFields();
        this.initializeDeleteButton();
        this.initializeAddSlideElementButton();

        final HBox elements = new HBox(5, new Label("Elements:"), this.addSlideElementButton);
        elements.setAlignment(Pos.BASELINE_LEFT);

        final VBox internalContainer = new VBox(5, getSlideConfigurationPane(), elements, this.slideElementsPane);

        this.setContent(internalContainer);
    }

    private FlowPane getSlideConfigurationPane() {
        final HBox group1 = new HBox(5, this.file, this.delete);
        group1.setAlignment(Pos.BOTTOM_LEFT);

        final FlowPane pane = new FlowPane(5, 5, this.slideId, this.name, group1);
        return pane;
    }

    private void initializeMandatoryFields() {
        this.slideId.setValidator(isInteger());
        this.name.setValidator(isNotEmpty());
        this.file.setValidator(isNotEmpty());
    }

    private void initializeDeleteButton() {
        this.delete.getStyleClass().add("delete-slide");
        this.delete.setGraphic(new FontAwesome(Icon.TRASH_ALT));
        this.delete.setTooltip(new Tooltip("Delete this slide"));
    }

    private void initializeAddSlideElementButton() {
        this.addSlideElementButton.setText("Add element");
        this.addSlideElementButton.setTooltip(new Tooltip("Add a slide element"));

        this.addSlideElementButton.setOnAction(event -> {
            this.addSlideElement();
        });
    }

    public void setOnDelete(final EventHandler<ActionEvent> action) {
        this.delete.setOnAction(action);
    }

    public SlideElementDefinition addSlideElement() {
        final SlideElementDefinition element = new SlideElementDefinition();
        element.setOnDelete(event -> {
            final ButtonType answer = DialogHelper.showConfirmationAlert("Delete slide element", "Are you sure you want to delete this slide element?");
            if (answer == ButtonType.YES) {
                this.slideElements.remove(element);
                this.slideElementsPane.getChildren().remove(element);
            }
        });

        this.slideElements.add(element);
        this.slideElementsPane.getChildren().add(element);

        return element;
    }

    public int getSlideId() {
        try {
            return Integer.parseInt(this.slideId.getText());
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.WARNING, "Can not parse slide ID", ex);
            return -1;
        }
    }

    public void setSlideId(final int slideId) {
        this.slideId.setText(String.valueOf(slideId));
    }

    public String getName() {
        return this.name.getText();
    }

    public void setName(final String name) {
        this.name.setText(name);
    }

    public String getFile() {
        return this.file.getText();
    }

    public void setFile(final String file) {
        this.file.setText(file);
    }

    public List<SlideElementDefinition> getSlideElements() {
        return this.slideElements;
    }

    /**
     * Check if the given slide definition is valid. The slide is valid if it's ID, name, file and all
     * {@link SlideElementDefinition elements} are valid.
     *
     * @return {@code true} if the slide is valid, {@code false} otherwise.
     */
    public boolean isValid() {
        return this.isIdValid() && this.isNameValid() && this.isFileValid() && areElementsValid();
    }

    protected boolean isIdValid() {
        return this.slideId.isValid();
    }

    protected boolean isNameValid() {
        return this.name.isValid();
    }

    protected boolean isFileValid() {
        return this.file.isValid();
    }

    protected boolean areElementsValid() {
        boolean valid = true;

        if (!this.slideElements.isEmpty()) {
            int index = 0;
            while (valid && index < this.slideElements.size()) {
                valid = this.slideElements.get(index++).isValid();
            }
        }

        return valid;
    }
}

package com.twasyl.slideshowfx.controls.builder.nodes;

import com.twasyl.slideshowfx.icons.FontAwesome;
import com.twasyl.slideshowfx.icons.Icon;
import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isInteger;
import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isNotEmpty;


/**
 * Bean providing UI elements used to define a slide element.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.3
 */
public class SlideElementDefinition extends FlowPane {
    private static final Logger LOGGER = Logger.getLogger(SlideElementDefinition.class.getName());

    private ExtendedTextField id = new ExtendedTextField("ID", true, 2);
    private ExtendedTextField htmlId = new ExtendedTextField("HTML ID", true);
    private ExtendedTextField defaultContent = new ExtendedTextField("Default content", true);
    private Button delete = new Button();

    public SlideElementDefinition() {
        super(5, 5);

        this.initializeMandatoryFields();
        this.initializeDeleteButton();

        final HBox group = new HBox(5, this.defaultContent, this.delete);
        group.setAlignment(Pos.BOTTOM_LEFT);

        this.getChildren().addAll(this.id, this.htmlId, group);
    }

    private void initializeMandatoryFields() {
        this.id.setValidator(isInteger());
        this.htmlId.setValidator(isNotEmpty());
        this.defaultContent.setValidator(isNotEmpty());
    }

    private void initializeDeleteButton() {
        this.delete.getStyleClass().add("delete-slide-element");
        this.delete.setGraphic(new FontAwesome(Icon.TRASH_ALT));
        this.delete.setTooltip(new Tooltip("Delete this slide element"));
    }

    public int getElementId() {
        try {
            return Integer.parseInt(id.getText());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Can not parse slide element ID", e);
            return -1;
        }
    }

    public void setElementId(final int elementId) {
        this.id.setText(String.valueOf(elementId));
    }

    public String getHtmlId() {
        return this.htmlId.getText();
    }

    public void setHtmlId(final String htmlId) {
        this.htmlId.setText(htmlId);
    }

    public String getDefaultContent() {
        return this.defaultContent.getText();
    }

    public void setDefaultContent(final String defaultContent) {
        this.defaultContent.setText(defaultContent);
    }

    public void setOnDelete(final EventHandler<ActionEvent> action) {
        this.delete.setOnAction(action);
    }

    /**
     * Check if the given slide element is valid. This element is considered valid if it's ID, HTML ID and default
     * content are valid.
     *
     * @return {@code true} if the element is valid, {@code false} otherwise.
     */
    public boolean isValid() {
        return this.isIdValid() && this.isHtmlIdValid() && this.isDefaultContentValid();
    }

    protected boolean isIdValid() {
        return this.id.isValid();
    }

    protected boolean isHtmlIdValid() {
        return this.htmlId.isValid();
    }

    protected boolean isDefaultContentValid() {
        return this.defaultContent.isValid();
    }
}

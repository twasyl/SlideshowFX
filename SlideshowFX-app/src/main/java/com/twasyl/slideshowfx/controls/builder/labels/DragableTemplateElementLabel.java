package com.twasyl.slideshowfx.controls.builder.labels;

import com.twasyl.slideshowfx.controls.builder.elements.ITemplateElement;
import com.twasyl.slideshowfx.controls.builder.elements.TemplateElementFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a {@see Label} that can be dragable for a {@link com.twasyl.slideshowfx.controls.builder.elements.ITemplateElement}
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class DragableTemplateElementLabel extends Label {

    private static final Logger LOGGER = Logger.getLogger(DragableTemplateElementLabel.class.getName());
    private final StringProperty templateElementName = new SimpleStringProperty();
    private final StringProperty templateElementClassName = new SimpleStringProperty();

    /**
     * Creates a default DragableTemplateElementLabel. This method defines the {@link javafx.scene.Node#setOnDragDetected(javafx.event.EventHandler)}
     * in order the label to be dragable. Note that the drag event is only valid if the {@link #templateElementClassNameProperty()} is not null.
     */
    public DragableTemplateElementLabel() {
        super();


        this.setOnDragDetected(event -> {
            if(DragableTemplateElementLabel.this.getTemplateElementClassName() != null) {

                Dragboard dragboard = DragableTemplateElementLabel.this.startDragAndDrop(TransferMode.COPY);

                ClipboardContent clipboard = new ClipboardContent();
                clipboard.putString(TemplateElementFactory.buildStringRepresentation(DragableTemplateElementLabel.this));
                dragboard.setContent(clipboard);
            }
        });
    }

    public DragableTemplateElementLabel(String text, final String templateElementClassName, final String templateElementName) {
        super();

        this.setText(text);
        this.setTemplateElementClassName(templateElementClassName);
        this.setTemplateElementName(templateElementName);
    }

    /**
     * The name associated to the generated Template Element.
     * @return The property corresponding to the name of the generated template element.
     */
    public StringProperty templateElementNameProperty() { return this.templateElementName; }

    /**
     * Get the name associated to the generated Template Element.
     * @return The name of the Template Element
     */
    public String getTemplateElementName() { return this.templateElementName.get(); }

    /**
     * Set the name that will be associated to a generated Template Element.
     * @param templateElementName The new new name of the generated Template Element.
     */
    public void setTemplateElementName(String templateElementName) { this.templateElementName.set(templateElementName); }

    /**
     * The class representing which Template Element will be generated.
     * @return The property corresponding to the class of the Template Element that will be generated.
     */
    public StringProperty templateElementClassNameProperty() { return templateElementClassName; }

    /**
     * Get the class representing which Template Element will be generated.
     * @return The class corresponding to the class of the Template Element that will be generated.
     */
    public String getTemplateElementClassName() { return this.templateElementClassName.get(); }

    /**
     * Set the new class representing which Template Element will be generated.
     * @param templateElementClassName The new class representing the Template Element that will be generated.
     */
    public void setTemplateElementClassName(String templateElementClassName) { this.templateElementClassName.set(templateElementClassName); }

    /**
     * Get the class representing which Template Element will be generated.
     * @return The class representing which Template Element will be generated.
     */
    public Class<? extends ITemplateElement> getTemplateElementClass() {
        Class<? extends ITemplateElement> clazz = null;

        if(getTemplateElementClassName() != null) {
            try {
                clazz = (Class<? extends ITemplateElement>) Class.forName(getTemplateElementClassName());
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Can not convert the class of the Template Element", e);
            }
        }

        return clazz;
    }
}

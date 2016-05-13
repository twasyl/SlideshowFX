package com.twasyl.slideshowfx.controls.builder.elements;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

import java.nio.file.Path;

/**
 * @author Thierry Wasylczenko
 */
public interface ITemplateElement<T> {

    /**
     * The property representing the name of this Template Element.
     * @return The property indicating the name of this element.
     */
    StringProperty nameProperty();

    /**
     * Get the name of this element.
     * @return The name of this element.
     */
    String getName();

    /**
     * Set the name of this template element.
     * @param name The new name of this template element.
     */
    void setName(String name);

    /**
     * The property representing the value of this template element.
     * @return The property indicating the name of this element.
     */
    ObjectProperty<T> valueProperty();

    /**
     * Get the value of this element.
     * @return The value of this element.
     */
    T getValue();

    /**
     * Set the value of this template element.
     * @param value The new value of this template element.
     */
    void setValue(T value);

    /**
     * Get the Path that corresponds to the working directory of the template being used.
     * @return The Path of the working directory.
     */
    ObjectProperty<Path> workingPathProperty();

    /**
     * Get the Path that corresponds to the working directory of the template being used.
     * @return The Path of the working directory.
     */
    Path getWorkingPath();

    /**
     * Set the Path that corresponds to the working directory of the template being used.
     * @param workingPath The Path of the working directory.
     */
    void setWorkingPath(Path workingPath);

    /**
     * The property indicating if this template element can be deleted from it's parent.
     * @return The property indicating if this template element can be deleted from it's parent.
     */
    BooleanProperty deletableProperty();

    /**
     * Indicates if this template element can be deleted from it's parent.
     * @return true if this template element is deletable from it's parent, false otherwise.
     */
    boolean isDeletable();

    /**
     * Defines if this template element can be removed from it's parent or not.
     * @param deletable true if this template element is deletable from it's parent, false otherwise.
     */
    void setDeletable(boolean deletable);

    /**
     * Add the given nodes to this template element.
     * @param nodes The nodes to add to this template element.
     * @throws java.lang.NullPointerException if a given node is null.
     */
    void appendContent(Node ... nodes);

    /**
     * This methods takes all children and generates a String representation that is the template structure.
     * @return The String representation of the template.
     */
    String getAsString();
}

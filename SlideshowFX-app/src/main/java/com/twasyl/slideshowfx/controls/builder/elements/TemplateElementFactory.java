package com.twasyl.slideshowfx.controls.builder.elements;

import com.twasyl.slideshowfx.controls.builder.labels.ChoiceBoxDragableTemplateElement;
import com.twasyl.slideshowfx.controls.builder.labels.DragableTemplateElementLabel;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides utility methods for working with TemplateElements.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 *
 */
public final class TemplateElementFactory {

    private static final Logger LOGGER = Logger.getLogger(TemplateElementFactory.class.getName());
    private static final String FIELD_CLASS_NAME = "className";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_VALUES = "values";

    public static String buildStringRepresentation(DragableTemplateElementLabel label) {
        if(label == null) throw new NullPointerException("The label can not be null");

        JsonObject object = new JsonObject();
        object.put(FIELD_CLASS_NAME, label.getTemplateElementClassName());

        /**
         * Only put the name if the values is not null
         */
        if(label.getTemplateElementName() != null) object.put(FIELD_NAME, label.getTemplateElementName());

        if(label instanceof ChoiceBoxDragableTemplateElement) {
            ChoiceBoxDragableTemplateElement choiceBox = (ChoiceBoxDragableTemplateElement) label;
            /**
             * Only put the values if the list is not null and not empty
             */
            if(choiceBox.getValues() != null && !choiceBox.getValues().isEmpty()) {
                final JsonArray valuesArray = new JsonArray();
                choiceBox.getValues().stream().forEach(value -> valuesArray.add(value));
                object.put(FIELD_VALUES, valuesArray);
            }
        }

        return object.encodePrettily();
    }

    public static ITemplateElement buildTemplateElement(String stringRepresentation) {
        JsonObject object = new JsonObject(stringRepresentation);

        ITemplateElement element = null;

        try {
            Class clazz = Class.forName(object.getString(FIELD_CLASS_NAME));
            Constructor constructor = clazz.getConstructor(String.class);

            element = (ITemplateElement) constructor.newInstance(object.getString(FIELD_NAME));

            if(element != null && clazz.equals(ChoiceBoxTemplateElement.class) && object.getJsonArray(FIELD_VALUES) != null) {
                JsonArray valuesArray = object.getJsonArray(FIELD_VALUES);

                if(valuesArray.size() > 0) {
                    Iterator<Object> iterator = valuesArray.iterator();

                    while(iterator.hasNext()) {
                        ((ChoiceBoxTemplateElement) element).addChoice((String) iterator.next());
                    }
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException  | InvocationTargetException e) {
            LOGGER.log(Level.WARNING, "Can not build the ITemplateElement", e);
        }

        return element;
    }
}

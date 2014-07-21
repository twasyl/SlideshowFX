package com.twasyl.slideshowfx.controls.builder.editor;

import com.twasyl.slideshowfx.controls.builder.elements.ArrayTemplateElement;
import com.twasyl.slideshowfx.controls.builder.elements.ITemplateElement;
import com.twasyl.slideshowfx.controls.builder.elements.ListTemplateElement;
import com.twasyl.slideshowfx.controls.builder.elements.StringTemplateElement;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import java.io.*;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a control, based on a {@link javafx.scene.control.Tab} to
 * edit a file.
 * The class defines action for accepting drag and drop event that allows to drag other
 * elements to insert text directly in the editor.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class ConfigurationFileEditor extends AbstractFileEditor<ListTemplateElement> {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationFileEditor.class.getName());

    public ConfigurationFileEditor() {
        super();

        final ListTemplateElement element = new ListTemplateElement(null);
        element.setDeletable(false);
        element.setPadding(new Insets(10, 10, 10 ,10));

        final ScrollPane scrollPane = new ScrollPane();

        this.setFileContent(element);
        this.setEditorScrollPane(scrollPane);
    }

    public ConfigurationFileEditor(File file) {
        this();
        this.setFile(file);
    }

    public ConfigurationFileEditor(Path workingPath, File file) {
        this(file);
        this.setWorkingPath(workingPath);
        this.getFileContent().setWorkingPath(this.getWorkingPath());
    }

    @Override
    public void updateFileContent() {
        if(getFile() == null) throw new NullPointerException("The file to read can not be null.");
        if(!getFile().exists()) throw new IllegalArgumentException("The file does not exist.");
        if(!getFile().canRead()) throw new IllegalArgumentException("The file can not be read.");

        try(final BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
            final StringBuilder builder = new StringBuilder();

            reader.lines().forEach(line -> builder.append(line));

            if(!builder.toString().isEmpty()) {
                JsonObject object = new JsonObject(builder.toString());
                ITemplateElement templateElement = this.buildTemplateElementStructure(object);
                this.setFileContent((ListTemplateElement) templateElement);
            } else {

            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not read the content of the file", e);
        }
    }

    public ITemplateElement buildTemplateElementStructure(JsonElement jsonElement) {

        ITemplateElement templateElement;

        if(jsonElement.isObject()) {

            final JsonObject object = jsonElement.asObject();
            templateElement = new ListTemplateElement(null);
            ITemplateElement element;
            Object value;

            for(String fieldName : object.getFieldNames()) {
                value = object.getField(fieldName);

                if(value instanceof JsonElement) {
                    element = buildTemplateElementStructure((JsonElement) value);

                } else {
                    element = new StringTemplateElement(null);
                    element.setValue(value.toString());
                }

                element.setName(fieldName);
                ((ListTemplateElement) templateElement).getValue().add(element);
            }

        } else if(jsonElement.isArray()) {

            final JsonArray array = jsonElement.asArray();
            templateElement = new ArrayTemplateElement(null);
            ITemplateElement element;
            Object value;

            for(int index = 0; index < array.size(); index++) {
                value = array.get(index);

                if(value instanceof JsonElement) {
                    element = buildTemplateElementStructure((JsonElement) value);
                } else {
                    element = new StringTemplateElement(null);
                    element.setValue(value.toString());
                }

                ((ArrayTemplateElement) templateElement).getValue().add(element);
            }
        } else {
            templateElement = new StringTemplateElement(null);
        }

        if(this.getWorkingPath() != null) templateElement.setWorkingPath(this.getWorkingPath());

        return templateElement;
    }

    @Override
    public void saveContent() {
        if(getFile() == null) throw new NullPointerException("The fileProperty is null");

        try(final FileWriter writer = new FileWriter(getFile())) {
            JsonObject json = new JsonObject(this.getFileContent().getAsString());
            writer.write(json.encodePrettily());
            writer.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not save the content", e);
        }
    }
}

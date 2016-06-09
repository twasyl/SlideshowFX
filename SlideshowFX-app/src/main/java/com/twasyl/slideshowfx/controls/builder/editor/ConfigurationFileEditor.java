package com.twasyl.slideshowfx.controls.builder.editor;

import com.twasyl.slideshowfx.controls.builder.elements.*;
import com.twasyl.slideshowfx.utils.io.DefaultCharsetReader;
import com.twasyl.slideshowfx.utils.io.DefaultCharsetWriter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
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
 * @since SlideshowFX 1.0
 */
public class ConfigurationFileEditor extends AbstractFileEditor<ListTemplateElement> {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationFileEditor.class.getName());

    private static final String[] FILES_ATTRIBUTES = {"file"};

    private static final String[] DIRECTORIES_ATTRIBUTES = {"presentation-directory", "resources-directory",
            "template-directory", "thumbnail-directory"};

    private static final String[] BASE64_ATTRIBUTES = {"value"};

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

        try(final DefaultCharsetReader reader = new DefaultCharsetReader(getFile())) {
            final StringBuilder builder = new StringBuilder();

            reader.lines().forEach(line -> builder.append(line));

            if(!builder.toString().isEmpty()) {
                JsonObject object = new JsonObject(builder.toString());
                ITemplateElement templateElement = this.buildTemplateElementStructure(object);
                this.setFileContent((ListTemplateElement) templateElement);
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not read the content of the file", e);
        }
    }

    /**
     * <p>Build the structure for a template template element from a given JSON structure. The given {@code jsonElement}
     * must be either of type {@link JsonObject} or {@link JsonArray}.</p>
     * @param jsonElement The template element to build.
     * @return The template element that has been built.
     */
    public ITemplateElement buildTemplateElementStructure(Object jsonElement) {

        ITemplateElement templateElement;

        if(jsonElement instanceof JsonObject) {

            final JsonObject object = (JsonObject) jsonElement;
            templateElement = new ListTemplateElement(null);
            ITemplateElement element;
            Object value;

            for(String fieldName : object.fieldNames()) {
                value = object.getValue(fieldName);

                if(value instanceof JsonObject || value instanceof JsonArray) {
                    element = buildTemplateElementStructure(value);
                } else if(Arrays.binarySearch(FILES_ATTRIBUTES, fieldName) >= 0) {
                    element = new FileTemplateElement(null);
                    element.setWorkingPath(this.getWorkingPath());
                    element.setValue(new File(value.toString()));
                } else if(Arrays.binarySearch(DIRECTORIES_ATTRIBUTES, fieldName) >= 0) {
                    element = new DirectoryTemplateElement(null);
                    element.setWorkingPath(this.getWorkingPath());
                    element.setValue(new File(value.toString()));
                } else if(value instanceof Number) {
                    element = new IntegerTemplateElement(null);
                    element.setWorkingPath(this.getWorkingPath());
                    element.setValue(((Number) value).intValue());
                } else {
                    if(this.isBase64Field(fieldName, value.toString())) element = new Base64TemplateElement(null);
                    else element = new StringTemplateElement(null);
                    element.setWorkingPath(this.getWorkingPath());
                    element.setValue(value.toString());
                }

                element.setName(fieldName);

                ((ListTemplateElement) templateElement).getValue().add(element);
            }

        } else if(jsonElement instanceof JsonArray) {

            final JsonArray array = (JsonArray) jsonElement;
            templateElement = new ArrayTemplateElement(null);
            ITemplateElement element;
            //Object value;

            for(Object value : array) {
                if(value instanceof JsonObject || value instanceof JsonArray) {
                    element = buildTemplateElementStructure(value);
                    element.setWorkingPath(this.getWorkingPath());
                } else if(value instanceof Number) {
                    element = new IntegerTemplateElement(null);
                    element.setWorkingPath(this.getWorkingPath());
                    element.setValue(((Number) value).intValue());
                } else {
                    if(this.isBase64Field(null, value.toString())) element = new Base64TemplateElement(null);
                    else element = new StringTemplateElement(null);
                    element.setWorkingPath(this.getWorkingPath());
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

        try(final DefaultCharsetWriter writer = new DefaultCharsetWriter(getFile())) {
            JsonObject json = new JsonObject(this.getFileContent().getAsString());
            writer.write(json.encodePrettily());
            writer.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not save the content", e);
        }
    }

    /**
     * Determine if a JSON field identified by its name and value is a Base64 field. In order to check it, the method
     * tries to decode the value and if it succeeds and the name of the field is the one of the identified Base64 field,
     * then the field is considered as a Base64 field.
     * @param fieldName The name of the JSON field to identify.
     * @param fieldValue The value of the JSON field to identify
     * @return {@code true} if the field is identified as a Base64 one, {@code false} otherwise.
     */
    private boolean isBase64Field(final String fieldName, final String fieldValue) {
        boolean result = fieldName == null ? true : Arrays.binarySearch(BASE64_ATTRIBUTES, fieldName) >= 0;

        if(result) {
            try {
                Base64.getDecoder().decode(fieldValue);
            } catch(IllegalArgumentException e) {
                result = false;
            }
        }

        return result;
    }
}

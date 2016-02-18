package com.twasyl.slideshowfx.utils;


import com.twasyl.slideshowfx.utils.io.DefaultCharsetReader;
import com.twasyl.slideshowfx.utils.io.DefaultCharsetWriter;
import io.vertx.core.json.JsonObject;

import java.io.*;

/**
 * This class provides utility methods for manipulating JSON objects.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class JSONHelper {

    /**
     * Read a JSON file and returns it to a JsonObject
     * @param file The file containing a JSON object.
     *
     * @return the JSON object corresponding to the content of the file.
     * @throws java.io.IOException If an error occurs when reading the file.
     */
    public static JsonObject readFromFile(File file) throws IOException {
        if(file == null) throw new NullPointerException("The file to read can not be null.");
        if(!file.exists()) throw new FileNotFoundException("The file to read does not exist.");

        final StringBuilder dataAsString = new StringBuilder();
        String line;

        try(final BufferedReader reader = new DefaultCharsetReader(file)) {
            while((line = reader.readLine()) != null) {
                dataAsString.append(line);
            }
        }

        JsonObject jsonObject = new JsonObject(dataAsString.toString());

        return jsonObject;
    }

    /**
     * Save a JSON object to a file. The JSON object is pretty formatted.
     *
     * @param object The object to save.
     * @param file The file where the JSON object is saved.
     * @throws java.io.FileNotFoundException If the file is not found.
     */
    public static void writeObject(JsonObject object, File file) throws IOException {
        if(object == null) throw new NullPointerException("The JSON object to save can not be null");
        if(file == null) throw new NullPointerException("The file can not be null");

        try(final Writer writer = new DefaultCharsetWriter(file)) {
            writer.write(object.encodePrettily());
        }
    }
}

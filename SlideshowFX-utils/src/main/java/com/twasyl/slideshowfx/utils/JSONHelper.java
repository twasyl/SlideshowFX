package com.twasyl.slideshowfx.utils;


import com.twasyl.slideshowfx.utils.io.DefaultCharsetReader;
import com.twasyl.slideshowfx.utils.io.DefaultCharsetWriter;
import io.vertx.core.json.JsonObject;

import java.io.*;

/**
 * This class provides utility methods for manipulating JSON objects.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class JSONHelper {

    /**
     * Read a JSON file and returns it to a JsonObject
     * @param file The file containing a JSON object.
     *
     * @return the JSON object corresponding to the content of the file.
     * @throws IOException If an error occurs when reading the file.
     */
    public static JsonObject readFromFile(File file) throws IOException {
        if(file == null) throw new NullPointerException("The file to read can not be null.");
        if(!file.exists()) throw new FileNotFoundException("The file to read does not exist.");

        return readFromReader(new DefaultCharsetReader(file));
    }

    /**
     * Read a JSON string and returns it to a JsonObject
     * @param reader The reader containing a JSON object.
     *
     * @return the JSON object corresponding to the content of the reader.
     * @throws IOException If an error occurs when reading the reader.
     */
    public static JsonObject readFromReader(Reader reader) throws IOException {
        if(reader == null) throw new NullPointerException("The reader to read can not be null.");

        final StringBuilder dataAsString = new StringBuilder();
        String line;

        char[] buffer = new char[1024];
        int bytesRead;

        try {
            while ((bytesRead = reader.read(buffer)) != -1) {
                dataAsString.append(buffer, 0, bytesRead);
            }
        } finally {
            reader.close();
        }


        final JsonObject jsonObject;

        if(!dataAsString.toString().trim().isEmpty()) {
            jsonObject = new JsonObject(dataAsString.toString().trim());
        } else {
            jsonObject = new JsonObject();
        }

        return jsonObject;
    }

    /**
     * Save a JSON object to a file. The JSON object is pretty formatted.
     *
     * @param object The object to save.
     * @param file The file where the JSON object is saved.
     * @throws FileNotFoundException If the file is not found.
     */
    public static void writeObject(JsonObject object, File file) throws IOException {
        if(object == null) throw new NullPointerException("The JSON object to save can not be null");
        if(file == null) throw new NullPointerException("The file can not be null");

        writeObject(object, new DefaultCharsetWriter(file));
    }

    /**
     * Save a JSON object to a given {@link Writer}. The JSON object is pretty formatted. The {@link Writer}
     * is closed when the write is done.
     *
     * @param object The object to save.
     * @param writer The writer where the JSON object is saved.
     */
    public static void writeObject(JsonObject object, Writer writer) throws IOException {
        if (object == null) throw new NullPointerException("The JSON object to save can not be null");
        if (writer == null) throw new NullPointerException("The writer can not be null");

        try {
            writer.write(object.encodePrettily());
            writer.flush();
        } finally {
            writer.close();
        }
    }
}

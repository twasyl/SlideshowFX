package com.twasyl.slideshowfx.utils;

import com.oracle.javafx.jmx.json.JSONDocument;
import com.oracle.javafx.jmx.json.JSONFactory;
import com.oracle.javafx.jmx.json.JSONReader;
import org.vertx.java.core.json.JsonObject;

import java.io.*;

/**
 * This class provides utility methods for manipulating JSON objects.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class JSONHelper {

    /**
     * Read a JSON file and returns it to a JsonObject
     * @param file The file containing a JSON object.
     *
     * @return the JSON object corresponding to the content of the file.
     */
    public static JSONDocument readFromFile(File file) throws IOException {
        if(file == null) throw new NullPointerException("The file to read can not be null.");
        if(!file.exists()) throw new FileNotFoundException("The file to read does not exist.");

        final JSONReader jsonReader = JSONFactory.instance().makeReader(new InputStreamReader(new FileInputStream(file)));
        final JSONDocument document = jsonReader.build();

        return document;
    }
}

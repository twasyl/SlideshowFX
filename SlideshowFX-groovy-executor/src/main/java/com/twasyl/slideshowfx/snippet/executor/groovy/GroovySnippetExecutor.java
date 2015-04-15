/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.snippet.executor.groovy;

import com.sun.javafx.PlatformUtil;
import com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of {@link com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor} that allows to execute
 * Java code snippets.
 * This implementation is identified with the code {@code JAVA}.
 *
 * @author Thierry Wasyczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class GroovySnippetExecutor  extends AbstractSnippetExecutor {

    private static final Logger LOGGER = Logger.getLogger(GroovySnippetExecutor.class.getName());

    /**
     * Indicates if the code should be wrapped in a main or run method (depending it is a Groovy Script or Class)
     */
    private static final String WRAP_IN_METHOD_RUNNER = "wrapInMethodRunner";
    private static final String IMPORTS_PROPERTY = "imports";
    private static final String CLASS_NAME_PROPERTY = "class";
    private static final String MAKE_SCRIPT = "makeScript";

    public GroovySnippetExecutor() {
        super("GROOVY", "Groovy", "groovy");
    }

    @Override
    public Parent getUI(final CodeSnippet codeSnippet) {
        final TextField classTextField = new TextField();
        classTextField.setPromptText("Class name");
        classTextField.setPrefColumnCount(10);
        classTextField.setTooltip(new Tooltip("The class name of this code snippet"));
        classTextField.textProperty().addListener((textValue, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) codeSnippet.putProperty(CLASS_NAME_PROPERTY, null);
            else codeSnippet.putProperty(CLASS_NAME_PROPERTY, newText);
        });

        final StringProperty codeEncapsulationType = new SimpleStringProperty("main");

        final Tooltip wrapInTooltip = new Tooltip();
        wrapInTooltip.textProperty().bind(new SimpleStringProperty("Wrap the provided code snippet in a Groovy").concat(codeEncapsulationType).concat(" method"));

        final CheckBox wrapInMethodRunner = new CheckBox();
        wrapInMethodRunner.textProperty().bind(new SimpleStringProperty("Wrap code snippet in ").concat(codeEncapsulationType));
        wrapInMethodRunner.setTooltip(wrapInTooltip);
        wrapInMethodRunner.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
            if(newSelected != null) codeSnippet.putProperty(WRAP_IN_METHOD_RUNNER, newSelected.toString());
        });

        final CheckBox makeScript = new CheckBox("Make Groovy Script");
        makeScript.setTooltip(new Tooltip("Create a Groovy Script instead of a Groovy class"));
        makeScript.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
            if(newSelected != null) codeSnippet.putProperty(MAKE_SCRIPT, newSelected.toString());

            if(newSelected != null && newSelected) codeEncapsulationType.set("script");
            else codeEncapsulationType.set("main");
        });

        final TextArea imports = new TextArea();
        imports.setPromptText("Imports");
        imports.setPrefColumnCount(15);
        imports.setPrefRowCount(15);
        imports.setWrapText(true);
        imports.textProperty().addListener((textValue, oldText, newText) -> {
            if(newText.isEmpty()) codeSnippet.putProperty(IMPORTS_PROPERTY, null);
            else codeSnippet.putProperty(IMPORTS_PROPERTY, newText);
        });

        final VBox ui = new VBox(5);
        ui.getChildren().addAll(classTextField, wrapInMethodRunner, makeScript, imports);

        return ui;
    }

    @Override
    public ObservableList<String> execute(final CodeSnippet codeSnippet) {
        final ObservableList<String> consoleOutput = FXCollections.observableArrayList();

        final Thread snippetThread = new Thread(() -> {

            // Build code file content according properties
            final StringBuilder codeBuilder = new StringBuilder();
            String className = "Snippet";

            final Boolean wrapInMain = codeSnippet.getProperties().containsKey(WRAP_IN_METHOD_RUNNER) ?
                    Boolean.parseBoolean(codeSnippet.getProperties().get(WRAP_IN_METHOD_RUNNER)) :
                    false;

            final Boolean makeScript = codeSnippet.getProperties().containsKey(MAKE_SCRIPT) ?
                    Boolean.parseBoolean(codeSnippet.getProperties().get(MAKE_SCRIPT)) :
                    false;

            if(wrapInMain) {
                if(makeScript) codeBuilder.append("import org.codehaus.groovy.runtime.InvokerHelper\n");

                final String imports = codeSnippet.getProperties().get(IMPORTS_PROPERTY);
                if(imports != null) codeBuilder.append(imports).append("\n");

                if(codeSnippet.getProperties().get(CLASS_NAME_PROPERTY) != null && !codeSnippet.getProperties().get(CLASS_NAME_PROPERTY).isEmpty()) {
                    className = codeSnippet.getProperties().get(CLASS_NAME_PROPERTY);
                }

                codeBuilder.append("\nclass ").append(className).append(makeScript ? " extends Script" : "").append(" {\n");

                if(makeScript) codeBuilder.append("\tdef run() {\n");
                else codeBuilder.append("\tdef static main(String...args) {\n");

                codeBuilder.append("\t\t").append(codeSnippet.getCode()).append("\n")
                           .append("\t}\n}");
            } else {
                if(makeScript) codeBuilder.append("import org.codehaus.groovy.runtime.InvokerHelper\n");

                codeBuilder.append(codeSnippet.getCode());
            }

            final File codeFile = new File(this.getTemporaryDirectory(), className.concat(".groovy"));
            try (final FileWriter codeFileWriter = new FileWriter(codeFile)) {
                codeFileWriter.write(codeBuilder.toString());
                codeFileWriter.flush();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not write code to snippet file", e);
                consoleOutput.add("ERROR: ".concat(e.getMessage()));
            }

            // Execute the class
            final File groovyExecutable = PlatformUtil.isWindows() ?
                    new File(this.getSdkHome(), "bin/groovy.bat") :
                    new File(this.getSdkHome(), "bin/groovy");

            final String[] executionCommand = {groovyExecutable.getAbsolutePath(), codeFile.getName()};

            Process process = null;
            try {
                process = new ProcessBuilder()
                        .redirectErrorStream(true)
                        .command(executionCommand)
                        .directory(this.getTemporaryDirectory())
                        .start();
                try (final BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    inputStream.lines().forEach(line -> consoleOutput.add(line));
                }

                try (final BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    errorStream.lines().forEach(line -> consoleOutput.add(line));
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not execute code snippet", e);
                consoleOutput.add("ERROR: ".concat(e.getMessage()));
            } finally {
                if(process != null) {
                    try {
                        process.waitFor();
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.SEVERE, "Can not wait for process to end", e);
                    }
                }
            }

            codeFile.delete();
        });
        snippetThread.start();

        return consoleOutput;
    }
}

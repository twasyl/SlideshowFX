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

package com.twasyl.slideshowfx.snippet.executor.java;

import com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
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
public class JavaSnippetExecutor extends AbstractSnippetExecutor {
    private static final Logger LOGGER = Logger.getLogger(JavaSnippetExecutor.class.getName());

    private static final String WRAP_IN_MAIN_PROPERTY = "wrapInMain";
    private static final String IMPORTS_PROPERTY = "imports";
    private static final String CLASS_NAME_PROPERTY = "class";

    public JavaSnippetExecutor() {
        super("JAVA", "Java", "java");
    }

    @Override
    public Parent getUI(final CodeSnippet codeSnippet) {
        final TextField classTextField = new TextField();
        classTextField.setPromptText("Class name");
        classTextField.setPrefColumnCount(10);
        classTextField.setTooltip(new Tooltip("The class name of this code snippet"));
        classTextField.textProperty().addListener((textValue, oldText, newText) -> {
            if(newText == null || newText.isEmpty()) codeSnippet.putProperty(CLASS_NAME_PROPERTY, null);
            else codeSnippet.putProperty(CLASS_NAME_PROPERTY, newText);
        });

        final CheckBox wrapInMain = new CheckBox("Wrap code snippet in main");
        wrapInMain.setTooltip(new Tooltip("Wrap the provided code snippet in a Java main method"));
        wrapInMain.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
            if(newSelected != null) codeSnippet.putProperty(WRAP_IN_MAIN_PROPERTY, newSelected.toString());
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
        ui.getChildren().addAll(classTextField, wrapInMain, imports);

        return ui;
    }

    @Override
    public ObservableList<String> execute(final CodeSnippet codeSnippet) {
        final ObservableList<String> consoleOutput = FXCollections.observableArrayList();

        final Thread snippetThread = new Thread(() -> {

            // Build code file content according properties
            final StringBuilder codeBuilder = new StringBuilder();
            String className = "Snippet";

            final Boolean wrapInMain = codeSnippet.getProperties().containsKey(WRAP_IN_MAIN_PROPERTY) ?
                    Boolean.parseBoolean(codeSnippet.getProperties().get(WRAP_IN_MAIN_PROPERTY)) :
                    false;

            if(wrapInMain) {
                final String imports = codeSnippet.getProperties().get(IMPORTS_PROPERTY);
                if(imports != null) codeBuilder.append(imports).append("\n");

                if(codeSnippet.getProperties().get(CLASS_NAME_PROPERTY) != null && !codeSnippet.getProperties().get(CLASS_NAME_PROPERTY).isEmpty()) {
                    className = codeSnippet.getProperties().get(CLASS_NAME_PROPERTY);
                }

                codeBuilder.append("\npublic class ").append(className).append(" {\n")
                        .append("\tpublic static void main(String[] args) {\n")
                        .append("\t\t").append(codeSnippet.getCode()).append("\n")
                        .append("\t}\n}");
            } else {
                codeBuilder.append(codeSnippet.getCode());
            }

            final File codeFile = new File(this.getTemporaryDirectory(), className.concat(".java"));
            try (final FileWriter codeFileWriter = new FileWriter(codeFile)) {
                codeFileWriter.write(codeBuilder.toString());
                codeFileWriter.flush();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not write code to snippet file", e);
                consoleOutput.add("ERROR: ".concat(e.getMessage()));
            }

            // Compile the Java class
            final File javacExecutable = new File(this.getSdkHome(), "bin/javac");

            final String[] compilationCommand = {javacExecutable.getAbsolutePath(), codeFile.getName()};

            Process process = null;
            try {
                process = new ProcessBuilder()
                            .redirectErrorStream(true)
                            .command(compilationCommand)
                            .directory(this.getTemporaryDirectory())
                            .start();

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

            // Execute the class only if the compilation was successful
            if(process != null && process.exitValue() == 0) {
                final File classFile = new File(this.getTemporaryDirectory(), className);
                final File javaExecutable = new File(this.getSdkHome(), "bin/java");
                final String[] executionCommand = {javaExecutable.getAbsolutePath(), className};

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

                classFile.delete();
            }
        });
        snippetThread.start();

        return consoleOutput;
    }
}

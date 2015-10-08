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

package com.twasyl.slideshowfx.snippet.executor.golo;

import com.sun.javafx.PlatformUtil;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.utils.beans.converter.FileStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of {@link com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor} that allows to execute
 * Golo code snippets.
 * This implementation is identified with the code {@code GOLO}.
 *
 * @author Thierry Wasyczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class GoloSnippetExecutor extends AbstractSnippetExecutor<GoloSnippetExecutorOptions> {
    private static final Logger LOGGER = Logger.getLogger(GoloSnippetExecutor.class.getName());

    private static final String GOLO_HOME_PROPERTY_SUFFIX = ".home";
    private static final String WRAP_IN_MAIN_PROPERTY = "wrapInMain";
    private static final String IMPORTS_PROPERTY = "imports";

    public GoloSnippetExecutor() {
        super("GOLO", "Golo", "language-java");
        this.setOptions(new GoloSnippetExecutorOptions());

        final String goloHome = GlobalConfiguration.getProperty(this.getConfigurationBaseName().concat(GOLO_HOME_PROPERTY_SUFFIX));
        if(goloHome != null) {
            try {
                this.getOptions().setGoloHome(new File(goloHome));
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Can not set the GOLO_HOME", e);
            }
        }
    }

    @Override
    public Parent getUI(final CodeSnippet codeSnippet) {
        final CheckBox wrapInMain = new CheckBox("Wrap code snippet in main");
        wrapInMain.setTooltip(new Tooltip("Wrap the provided code snippet in a Golo main method"));
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
        ui.getChildren().addAll(wrapInMain, imports);

        return ui;
    }

    @Override
    public Node getConfigurationUI() {
        this.newOptions = new GoloSnippetExecutorOptions();
        try {
            this.newOptions.setGoloHome(this.getOptions().getGoloHome());
        } catch (FileNotFoundException | NullPointerException e) {
            LOGGER.log(Level.FINE, "Can not duplicate GOLO_HOME", e);
        }

        final Label label = new Label(this.getLanguage().concat(":"));

        final TextField javaHomeField = new TextField();
        javaHomeField.textProperty().bindBidirectional(this.newOptions.goloHomeProperty(), new FileStringConverter());
        javaHomeField.setPrefColumnCount(20);

        final Button browse = new Button("...");
        browse.setOnAction(event -> {
            final DirectoryChooser chooser = new DirectoryChooser();
            final File sdkHomeDir = chooser.showDialog(null);
            if(sdkHomeDir != null) {
                javaHomeField.setText(sdkHomeDir.getAbsolutePath());
            }

        });

        final HBox box = new HBox(5);
        box.getChildren().addAll(label, javaHomeField, browse);

        return box;
    }

    @Override
    public void saveNewOptions() {
        if(this.getNewOptions() != null) {
            this.setOptions(this.getNewOptions());

            if(this.getOptions().getGoloHome() != null) {
                GlobalConfiguration.setProperty(this.getConfigurationBaseName().concat(GOLO_HOME_PROPERTY_SUFFIX),
                        this.getOptions().getGoloHome().getAbsolutePath().replaceAll("\\\\", "/"));
            }
        }
    }

    @Override
    public ObservableList<String> execute(final CodeSnippet codeSnippet) {
        final ObservableList<String> consoleOutput = FXCollections.observableArrayList();

        final Thread snippetThread = new Thread(() -> {

            // Build code file content according properties
            final Boolean wrapInMain = codeSnippet.getProperties().containsKey(WRAP_IN_MAIN_PROPERTY) ?
                                                    Boolean.parseBoolean(codeSnippet.getProperties().get(WRAP_IN_MAIN_PROPERTY)) :
                                                    false;

            final StringBuilder codeBuilder = new StringBuilder("module slideshowfx.Snippet\n\n");

            if(wrapInMain) {
                codeBuilder.append("\tfunction main = |args| {\n")
                            .append("\t")
                            .append(codeSnippet.getCode()).append("\n")
                            .append("\t}");
            } else {
                codeBuilder.append(codeSnippet.getCode());
            }

            final File codeFile = new File(this.getTemporaryDirectory(), "snippet.golo");
            try (final FileWriter codeFileWriter = new FileWriter(codeFile)) {
                codeFileWriter.write(codeBuilder.toString());
                codeFileWriter.flush();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not write code to snippet file", e);
                consoleOutput.add("ERROR: ".concat(e.getMessage()));
            }

            final File executable = PlatformUtil.isWindows() ?
                    new File(this.getOptions().getGoloHome(), "bin/golo.bat") :
                    new File(this.getOptions().getGoloHome(), "bin/golo");

            final String[] command = {executable.getAbsolutePath(), "golo", "--files", codeFile.getAbsolutePath()};

            Process process = null;
            try {
                process = new ProcessBuilder().redirectErrorStream(true).command(command).start();

                try (final BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    inputStream.lines().forEach(line -> consoleOutput.add(line));
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

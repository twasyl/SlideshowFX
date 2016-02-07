package com.twasyl.slideshowfx.snippet.executor.go;

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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of {@link com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor} that allows to execute
 * Go code snippets.
 * This implementation is identified with the code {@code GO}.
 *
 * @author Thierry Wasyczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class GoSnippetExecutor extends AbstractSnippetExecutor<GoSnippetExecutorOptions> {
    private static final Logger LOGGER = Logger.getLogger(GoSnippetExecutor.class.getName());

    private static final String GO_HOME_PROPERTY_SUFFIX = ".home";
    private static final String WRAP_IN_MAIN_PROPERTY = "wrapInMain";
    private static final String IMPORTS_PROPERTY = "imports";

    public GoSnippetExecutor() {
        super("GO", "Go", "language-go");
        this.setOptions(new GoSnippetExecutorOptions());

        final String javaHome = GlobalConfiguration.getProperty(this.getConfigurationBaseName().concat(GO_HOME_PROPERTY_SUFFIX));
        if(javaHome != null) {
            try {
                this.getOptions().setGoHome(new File(javaHome));
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Can not set the JAVA_HOME", e);
            }
        }
    }

    @Override
    public Parent getUI(final CodeSnippet codeSnippet) {
        final CheckBox wrapInMain = new CheckBox("Wrap code snippet in main");
        wrapInMain.setTooltip(new Tooltip("Wrap the provided code snippet in a Go main method"));
        wrapInMain.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
            if(newSelected != null) codeSnippet.putProperty(WRAP_IN_MAIN_PROPERTY, newSelected.toString());
        });

        final TextArea imports = new TextArea();
        imports.setPromptText("Imports");
        imports.setTooltip(new Tooltip("Specify packages that must be imported. Provide one package per line, not surrounded by double-quotes"));
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
        this.newOptions = new GoSnippetExecutorOptions();
        try {
            this.newOptions.setGoHome(this.getOptions().getGoHome());
        } catch (FileNotFoundException | NullPointerException e) {
            LOGGER.log(Level.FINE, "Can not duplicate GO_HOME", e);
        }

        final Label label = new Label(this.getLanguage().concat(":"));

        final TextField javaHomeField = new TextField();
        javaHomeField.textProperty().bindBidirectional(this.newOptions.goHomeProperty(), new FileStringConverter());
        javaHomeField.setPrefColumnCount(20);

        final Button browse = new Button("...");
        browse.setOnAction(event -> {
            final DirectoryChooser chooser = new DirectoryChooser();
            final File sdkHomeDir = chooser.showDialog(null);
            if (sdkHomeDir != null) {
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

            if(this.getOptions().getGoHome() != null) {
                GlobalConfiguration.setProperty(this.getConfigurationBaseName().concat(GO_HOME_PROPERTY_SUFFIX),
                        this.getOptions().getGoHome().getAbsolutePath().replaceAll("\\\\", "/"));
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

            final StringBuilder codeBuilder = new StringBuilder();

            // Declare the default package
            codeBuilder.append("package main\n\n");

            // Manage imports
            final String imports = codeSnippet.getProperties().get(IMPORTS_PROPERTY);
            if(imports != null && !imports.isEmpty()) {
                codeBuilder.append("import (\n");

                final String[] importsArray = imports.split("\n");
                Arrays.stream(importsArray).forEach(packageToImport -> {
                    codeBuilder.append("\t\"").append(packageToImport).append("\"\n");
                });

                codeBuilder.append(")\n\n");
            }

            // Manage if a main method must be generated or not
            if(wrapInMain) {
                codeBuilder.append("func main() {\n")
                        .append("\t").append(codeSnippet.getCode()).append("\n")
                        .append("}");
            } else {
                codeBuilder.append(codeSnippet.getCode());
            }

            final File codeFile = new File(this.getTemporaryDirectory(), "Snippet.go");
            try (final FileWriter codeFileWriter = new FileWriter(codeFile)) {
                codeFileWriter.write(codeBuilder.toString());
                codeFileWriter.flush();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not write code to snippet file", e);
                consoleOutput.add("ERROR: ".concat(e.getMessage()));
            }

            // Execute the Go file
            final File goExecutable = PlatformUtil.isWindows() ?
                    new File(this.getOptions().getGoHome(), "bin/go.exe") :
                    new File(this.getOptions().getGoHome(), "bin/go");

            final String[] compilationCommand = {goExecutable.getAbsolutePath(), "run", codeFile.getName()};

            Process process = null;
            try {
                process = new ProcessBuilder()
                        .redirectErrorStream(true)
                        .command(compilationCommand)
                        .directory(this.getTemporaryDirectory())
                        .start();

                try (final BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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

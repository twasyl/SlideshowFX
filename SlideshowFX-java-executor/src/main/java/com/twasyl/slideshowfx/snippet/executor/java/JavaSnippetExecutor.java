package com.twasyl.slideshowfx.snippet.executor.java;

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
 * Java code snippets.
 * This implementation is identified with the code {@code JAVA}.
 *
 * @author Thierry Wasyczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class JavaSnippetExecutor extends AbstractSnippetExecutor<JavaSnippetExecutorOptions> {
    private static final Logger LOGGER = Logger.getLogger(JavaSnippetExecutor.class.getName());

    private static final String JAVA_HOME_PROPERTY_SUFFIX = ".home";
    private static final String WRAP_IN_MAIN_PROPERTY = "wrapInMain";
    private static final String IMPORTS_PROPERTY = "imports";
    private static final String CLASS_NAME_PROPERTY = "class";

    public JavaSnippetExecutor() {
        super("JAVA", "Java", "language-java");
        this.setOptions(new JavaSnippetExecutorOptions());

        final String javaHome = GlobalConfiguration.getProperty(this.getConfigurationBaseName().concat(JAVA_HOME_PROPERTY_SUFFIX));
        if(javaHome != null) {
            try {
                this.getOptions().setJavaHome(new File(javaHome));
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Can not set the JAVA_HOME", e);
            }
        }
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
    public Node getConfigurationUI() {
        this.newOptions = new JavaSnippetExecutorOptions();
        try {
            this.newOptions.setJavaHome(this.getOptions().getJavaHome());
        } catch (FileNotFoundException | NullPointerException e) {
            LOGGER.log(Level.FINE, "Can not duplicate JAVA_HOME", e);
        }

        final Label label = new Label(this.getLanguage().concat(":"));

        final TextField javaHomeField = new TextField();
        javaHomeField.textProperty().bindBidirectional(this.newOptions.javaHomeProperty(), new FileStringConverter());
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

            if(this.getOptions().getJavaHome() != null) {
                GlobalConfiguration.setProperty(this.getConfigurationBaseName().concat(JAVA_HOME_PROPERTY_SUFFIX),
                        this.getOptions().getJavaHome().getAbsolutePath().replaceAll("\\\\", "/"));
            }
        }
    }

    @Override
    public ObservableList<String> execute(final CodeSnippet codeSnippet) {
        final ObservableList<String> consoleOutput = FXCollections.observableArrayList();

        final Thread snippetThread = new Thread(() -> {

            // Build code file content according properties
            // Manage the class name
            String className = codeSnippet.getProperties().get(CLASS_NAME_PROPERTY);
            if(className == null || className.isEmpty()) className = "Snippet";

            final Boolean wrapInMain = codeSnippet.getProperties().containsKey(WRAP_IN_MAIN_PROPERTY) ?
                    Boolean.parseBoolean(codeSnippet.getProperties().get(WRAP_IN_MAIN_PROPERTY)) :
                    false;

            final StringBuilder codeBuilder = new StringBuilder();

            // Manage imports
            final String imports = codeSnippet.getProperties().get(IMPORTS_PROPERTY);
            if(imports != null) codeBuilder.append(imports).append("\n");

            codeBuilder.append("\npublic class ").append(className).append(" {\n");

            // Manage if a main method must be generated or not
            if(wrapInMain) {
                codeBuilder.append("\tpublic static void main(String[] args) {\n")
                            .append("\t\t").append(codeSnippet.getCode()).append("\n")
                            .append("\t}");
            } else {
                codeBuilder.append(codeSnippet.getCode());
            }

            codeBuilder.append("\n}");

            final File codeFile = new File(this.getTemporaryDirectory(), className.concat(".java"));
            try (final FileWriter codeFileWriter = new FileWriter(codeFile)) {
                codeFileWriter.write(codeBuilder.toString());
                codeFileWriter.flush();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not write code to snippet file", e);
                consoleOutput.add("ERROR: ".concat(e.getMessage()));
            }

            // Compile the Java class
            final File javacExecutable = new File(this.getOptions().getJavaHome(), "bin/javac");

            final String[] compilationCommand = {javacExecutable.getAbsolutePath(), codeFile.getName()};

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

            // Execute the class only if the compilation was successful
            if(process != null && process.exitValue() == 0) {
                final File classFile = new File(this.getTemporaryDirectory(), className);
                final File javaExecutable = new File(this.getOptions().getJavaHome(), "bin/java");
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

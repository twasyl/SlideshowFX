package com.twasyl.slideshowfx.snippet.executor.scala;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.plugin.Plugin;
import com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.utils.beans.converter.FileStringConverter;
import com.twasyl.slideshowfx.utils.io.DefaultCharsetReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of {@link com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor} that allows to execute
 * Scala code snippets.
 * This implementation is identified with the code {@code SCALA}.
 *
 * @author Thierry Wasyczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX 1.0
 */
@Plugin
public class ScalaSnippetExecutor extends AbstractSnippetExecutor<ScalaSnippetExecutorOptions> {
    private static final Logger LOGGER = Logger.getLogger(ScalaSnippetExecutor.class.getName());

    protected static final String SCALA_HOME_PROPERTY_SUFFIX = ".home";
    protected static final String WRAP_IN_MAIN_PROPERTY = "wrapInMain";
    protected static final String IMPORTS_PROPERTY = "imports";
    protected static final String CLASS_NAME_PROPERTY = "class";

    public ScalaSnippetExecutor() {
        super("SCALA", "Scala", "language-scala");
        this.setOptions(new ScalaSnippetExecutorOptions());

        final String scalaHome = GlobalConfiguration.getProperty(this.getConfigurationBaseName().concat(SCALA_HOME_PROPERTY_SUFFIX));
        if (scalaHome != null) {
            try {
                this.getOptions().setScalaHome(new File(scalaHome));
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Can not set the SCALA_HOME", e);
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
            if (newText == null || newText.isEmpty()) codeSnippet.putProperty(CLASS_NAME_PROPERTY, null);
            else codeSnippet.putProperty(CLASS_NAME_PROPERTY, newText);
        });

        final CheckBox wrapInMain = new CheckBox("Wrap code snippet in main");
        wrapInMain.setTooltip(new Tooltip("Wrap the provided code snippet in a Scala main method"));
        wrapInMain.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
            if (newSelected != null) codeSnippet.putProperty(WRAP_IN_MAIN_PROPERTY, newSelected.toString());
        });

        final TextArea imports = new TextArea();
        imports.setPromptText("Imports");
        imports.setPrefColumnCount(15);
        imports.setPrefRowCount(15);
        imports.setWrapText(true);
        imports.textProperty().addListener((textValue, oldText, newText) -> {
            if (newText.isEmpty()) codeSnippet.putProperty(IMPORTS_PROPERTY, null);
            else codeSnippet.putProperty(IMPORTS_PROPERTY, newText);
        });

        final VBox ui = new VBox(5);
        ui.getChildren().addAll(classTextField, wrapInMain, imports);

        return ui;
    }

    @Override
    public Node getConfigurationUI() {
        this.newOptions = new ScalaSnippetExecutorOptions();
        try {
            this.newOptions.setScalaHome(this.getOptions().getScalaHome());
        } catch (FileNotFoundException | NullPointerException e) {
            LOGGER.log(Level.FINE, "Can not duplicate SCALA_HOME", e);
        }

        final Label label = new Label(this.getLanguage().concat(":"));

        final TextField scalaHomeField = new TextField();
        scalaHomeField.textProperty().bindBidirectional(this.newOptions.scalaHomeProperty(), new FileStringConverter());
        scalaHomeField.setPrefColumnCount(20);

        final Button browse = new Button("...");
        browse.setOnAction(event -> {
            final DirectoryChooser chooser = new DirectoryChooser();
            final File sdkHomeDir = chooser.showDialog(null);
            if (sdkHomeDir != null) {
                scalaHomeField.setText(sdkHomeDir.getAbsolutePath());
            }

        });

        final HBox box = new HBox(5);
        box.getChildren().addAll(label, scalaHomeField, browse);

        return box;
    }

    @Override
    public void saveNewOptions() {
        if (this.getNewOptions() != null) {
            this.setOptions(this.getNewOptions());

            if (this.getOptions().getScalaHome() != null) {
                GlobalConfiguration.setProperty(this.getConfigurationBaseName().concat(SCALA_HOME_PROPERTY_SUFFIX),
                        this.getOptions().getScalaHome().getAbsolutePath().replaceAll("\\\\", "/"));
            }
        }
    }

    @Override
    public ObservableList<String> execute(final CodeSnippet codeSnippet) {
        final ObservableList<String> consoleOutput = FXCollections.observableArrayList();

        final Thread snippetThread = new Thread(() -> {
            File codeFile = null;
            try {
                codeFile = createSourceCodeFile(codeSnippet);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not write code to snippet file", e);
                consoleOutput.add("ERROR: ".concat(e.getMessage()));
            }

            // Compile the Scala class
            final File scalacExecutable = new File(this.getOptions().getScalaHome(), "bin/scalac");

            final String[] compilationCommand = {scalacExecutable.getAbsolutePath(), codeFile.getName()};

            Process process = null;
            try {
                process = new ProcessBuilder()
                        .redirectErrorStream(true)
                        .command(compilationCommand)
                        .directory(this.getTemporaryDirectory())
                        .start();

                try (final BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    errorStream.lines().forEach(consoleOutput::add);
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not execute code snippet", e);
                appendErrorMessageToConsole(consoleOutput, e);
            } finally {
                waitForProcess(process);
            }

            deleteGeneratedFile(codeFile);

            // Execute the class only if the compilation was successful
            if (process != null && process.exitValue() == 0) {

                final File scalaExecutable = new File(this.getOptions().getScalaHome(), "bin/scala");
                final String[] executionCommand = {scalaExecutable.getAbsolutePath(), determineClassName(codeSnippet)};

                try {
                    process = new ProcessBuilder()
                            .redirectErrorStream(true)
                            .command(executionCommand)
                            .directory(this.getTemporaryDirectory())
                            .start();

                    try (final BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        inputStream.lines().forEach(consoleOutput::add);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Can not execute code snippet", e);
                    appendErrorMessageToConsole(consoleOutput, e);
                } finally {
                    waitForProcess(process);
                }

                deleteGeneratedFile(new File(this.getTemporaryDirectory(), determineClassName(codeSnippet)));
            }
        });
        snippetThread.start();

        return consoleOutput;
    }

    /**
     * Create the source code file for the given code snippet.
     *
     * @param codeSnippet The code snippet.
     * @return The file created and containing the source code.
     */
    protected File createSourceCodeFile(final CodeSnippet codeSnippet) throws IOException {
        final File codeFile = new File(this.getTemporaryDirectory(), determineClassName(codeSnippet).concat(".scala"));
        try (final FileWriter codeFileWriter = new FileWriter(codeFile)) {
            codeFileWriter.write(buildSourceCode(codeSnippet));
            codeFileWriter.flush();
        }

        return codeFile;
    }

    /**
     * Build code file content according properties. The source code can then be written properly inside a file in order
     * to be compiled and then executed.
     *
     * @param codeSnippet The code snippet to build the source code for.
     * @return The content of the source code file.
     */
    protected String buildSourceCode(final CodeSnippet codeSnippet) throws IOException {
        final StringBuilder sourceCode = new StringBuilder();

        if (hasImports(codeSnippet)) {
            sourceCode.append(getImports(codeSnippet)).append("\n\n");
        }

        sourceCode.append(getStartClassDefinition(codeSnippet)).append("\n");

        if (mustBeWrappedIn(codeSnippet, WRAP_IN_MAIN_PROPERTY)) {
            sourceCode.append("\tdef main(args: Array[String]) {\n")
                    .append(codeSnippet.getCode())
                    .append("\n\t}");
        } else {
            sourceCode.append(codeSnippet.getCode());
        }

        sourceCode.append("\n}");

        return sourceCode.toString();
    }

    /**
     * Get the imports to be added to the source code.
     *
     * @param codeSnippet The code snippet.
     */
    protected boolean hasImports(final CodeSnippet codeSnippet) {
        final String imports = codeSnippet.getProperties().get(IMPORTS_PROPERTY);
        return imports != null && !imports.isEmpty();
    }

    /**
     * Get the imports for the code snippets. If some lines of the imports don't contain the {@code import} keyword, it
     * will be added properly.
     *
     * @param codeSnippet The code snippet.
     * @return A well formatted string containing all imports.
     */
    protected String getImports(final CodeSnippet codeSnippet) throws IOException {
        final StringJoiner imports = new StringJoiner("\n");

        try (final StringReader stringReader = new StringReader(codeSnippet.getProperties().get(IMPORTS_PROPERTY));
             final BufferedReader reader = new DefaultCharsetReader(stringReader)) {

            reader.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .forEach(line -> imports.add(formatImportLine(line)));
        }

        return imports.toString();
    }

    /**
     * Format an import line by make sure it starts with the {@code import} keyword.
     *
     * @param importLine The import line to format.
     * @return A well formatted import line.
     */
    protected String formatImportLine(final String importLine) {
        final String importLineBeginning = "import ";
        String formattedImportLine;

        if (importLine.startsWith(importLineBeginning)) {
            formattedImportLine = importLine;
        } else {
            formattedImportLine = importLineBeginning.concat(importLine);
        }

        return formattedImportLine;
    }

    /**
     * Get the definition of the class.
     *
     * @param codeSnippet The code snippet.
     */
    protected String getStartClassDefinition(final CodeSnippet codeSnippet) {
        return "object ".concat(determineClassName(codeSnippet)).concat(" {");
    }

    /**
     * Determine the class name of the code snippet. It looks inside the code snippet's properties and check the value
     * of the {@link #CLASS_NAME_PROPERTY} property. If {@code null} or empty, {@code Snippet} will be returned.
     *
     * @param codeSnippet The code snippet.
     * @return The class name of the code snippet.
     */
    protected String determineClassName(final CodeSnippet codeSnippet) {
        String className = codeSnippet.getProperties().get(CLASS_NAME_PROPERTY);
        if (className == null || className.isEmpty()) className = "Snippet";
        return className;
    }
}

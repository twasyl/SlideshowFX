package com.twasyl.slideshowfx.snippet.executor.golo;

import com.sun.javafx.PlatformUtil;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
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
 * Golo code snippets.
 * This implementation is identified with the code {@code GOLO}.
 *
 * @author Thierry Wasyczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class GoloSnippetExecutor extends AbstractSnippetExecutor<GoloSnippetExecutorOptions> {
    private static final Logger LOGGER = Logger.getLogger(GoloSnippetExecutor.class.getName());

    protected static final String GOLO_HOME_PROPERTY_SUFFIX = ".home";
    protected static final String WRAP_IN_MAIN_PROPERTY = "wrapInMain";
    protected static final String IMPORTS_PROPERTY = "imports";
    protected static final String MODULE_NAME_PROPERTY = "module";

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
        final TextField moduleTextField = new TextField();
        moduleTextField.setPromptText("Module name");
        moduleTextField.setPrefColumnCount(10);
        moduleTextField.setTooltip(new Tooltip("The module name of this code snippet"));
        moduleTextField.textProperty().addListener((textValue, oldText, newText) -> {
            if(newText == null || newText.isEmpty()) codeSnippet.putProperty(MODULE_NAME_PROPERTY, null);
            else codeSnippet.putProperty(MODULE_NAME_PROPERTY, newText);
        });

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
        ui.getChildren().addAll(moduleTextField, wrapInMain, imports);

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

            File codeFile = null;
            try {
                codeFile = createSourceCodeFile(codeSnippet);
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

    /**
     * Create the source code file for the given code snippet.
     * @param codeSnippet The code snippet.
     * @return The file created and containing the source code.
     */
    protected File createSourceCodeFile(final CodeSnippet codeSnippet) throws IOException {
        final File codeFile = new File(this.getTemporaryDirectory(), determineModuleName(codeSnippet).concat(".golo"));
        try (final FileWriter codeFileWriter = new FileWriter(codeFile)) {
            codeFileWriter.write(buildSourceCode(codeSnippet));
            codeFileWriter.flush();
        }

        return codeFile;
    }

    /**
     *
     * Build code file content according properties. The source code can then be written properly inside a file in order
     * to be compiled and then executed.
     * @param codeSnippet The code snippet to build the source code for.
     * @return The content of the source code file.
     */
    protected String buildSourceCode(final CodeSnippet codeSnippet) throws IOException {
        final StringBuilder sourceCode = new StringBuilder();

        sourceCode.append(getStartModuleDefinition(codeSnippet)).append("\n\n");

        if(hasImports(codeSnippet)) {
            sourceCode.append(getImports(codeSnippet)).append("\n\n");
        }

        if(mustBeWrappedInMain(codeSnippet)) {
            sourceCode.append(getStartMainMethod()).append("\n")
                    .append(codeSnippet.getCode())
                    .append("\n").append(getEndMainMethod());
        } else {
            sourceCode.append(codeSnippet.getCode());
        }

        sourceCode.append("\n").append(getEndModuleDefinition(codeSnippet));

        return sourceCode.toString();
    }

    /**
     * Get the imports to be added to the source code.
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
     * @param importLine The import line to format.
     * @return A well formatted import line.
     */
    protected String formatImportLine(final String importLine) {
        final String importLineBeginning = "import ";

        String formattedImportLine;

        if(importLine.startsWith(importLineBeginning)) {
            formattedImportLine = importLine;
        } else {
            formattedImportLine = importLineBeginning.concat(importLine);
        }

        return formattedImportLine;
    }

    /**
     * Get the definition of the class.
     * @param codeSnippet The code snippet.
     */
    protected String getStartModuleDefinition(final CodeSnippet codeSnippet) {
        return "module ".concat(determineModuleName(codeSnippet));
    }

    /**
     * Determine the module name of the code snippet. It looks inside the code snippet's properties and check the value
     * of the {@link #MODULE_NAME_PROPERTY} property. If {@code null} or empty, {@code Snippet} will be returned.
     * @param codeSnippet The code snippet.
     * @return The module name of the code snippet.
     */
    protected String determineModuleName(final CodeSnippet codeSnippet) {
        String moduleName = codeSnippet.getProperties().get(MODULE_NAME_PROPERTY);
        if(moduleName == null || moduleName.isEmpty()) moduleName = "slideshowfx.Snippet";
        return moduleName;
    }

    /**
     * Determine if the code snippet must be wrapped inside a main method. It is determined by the presence and value of
     * the {@link #WRAP_IN_MAIN_PROPERTY} property.
     * @param codeSnippet The code snippet.
     * @return {@code true} if the snippet must be wrapped in main, {@code false} otherwhise.
     */
    protected boolean mustBeWrappedInMain(final CodeSnippet codeSnippet) {
        final Boolean wrapInMain = codeSnippet.getProperties().containsKey(WRAP_IN_MAIN_PROPERTY) ?
                Boolean.parseBoolean(codeSnippet.getProperties().get(WRAP_IN_MAIN_PROPERTY)) :
                false;
        return wrapInMain;
    }

    /**
     * Get the start of the declaration of the main method.
     * @return The start of the main method.
     */
    protected String getStartMainMethod() {
        return "function main = |args| {";
    }

    /**
     * Get the end of the declaration of the main method.
     * @return The end of the main method.
     */
    protected String getEndMainMethod() {
        return "}";
    }

    /**
     * Get the end of the definition of the class.
     * @param codeSnippet The code snippet.
     */
    protected String getEndModuleDefinition(final CodeSnippet codeSnippet) {
        return "";
    }
}

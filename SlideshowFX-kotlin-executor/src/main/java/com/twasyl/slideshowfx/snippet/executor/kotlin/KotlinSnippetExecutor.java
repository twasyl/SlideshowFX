package com.twasyl.slideshowfx.snippet.executor.kotlin;

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
 * An implementation of {@link AbstractSnippetExecutor} that allows to execute
 * Kotlin code snippets.
 * This implementation is identified with the code {@code KOTLIN}.
 *
 * @author Thierry Wasyczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class KotlinSnippetExecutor extends AbstractSnippetExecutor<KotlinSnippetExecutorOptions> {
    private static final Logger LOGGER = Logger.getLogger(KotlinSnippetExecutor.class.getName());

    private static final String KOTLIN_HOME_PROPERTY_SUFFIX = ".home";
    protected static final String WRAP_IN_MAIN_PROPERTY = "wrapInMain";
    protected static final String IMPORTS_PROPERTY = "imports";
    protected static final String PACKAGE_NAME_PROPERTY = "class";

    public KotlinSnippetExecutor() {
        super("KOTLIN", "Kotlin", "language-kotlin");
        this.setOptions(new KotlinSnippetExecutorOptions());

        final String kotlinHome = GlobalConfiguration.getProperty(this.getConfigurationBaseName().concat(KOTLIN_HOME_PROPERTY_SUFFIX));
        if(kotlinHome != null) {
            try {
                this.getOptions().setKotlinHome(new File(kotlinHome));
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Can not set the KOTLIN_HOME", e);
            }
        }
    }

    @Override
    public Parent getUI(final CodeSnippet codeSnippet) {
        final TextField packageTextField = new TextField();
        packageTextField.setPromptText("Package name");
        packageTextField.setPrefColumnCount(10);
        packageTextField.setTooltip(new Tooltip("The package name of this code snippet"));
        packageTextField.textProperty().addListener((textValue, oldText, newText) -> {
            if(newText == null || newText.isEmpty()) codeSnippet.putProperty(PACKAGE_NAME_PROPERTY, null);
            else codeSnippet.putProperty(PACKAGE_NAME_PROPERTY, newText);
        });

        final CheckBox wrapInMain = new CheckBox("Wrap code snippet in main");
        wrapInMain.setTooltip(new Tooltip("Wrap the provided code snippet in a Kotlin main method"));
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
        ui.getChildren().addAll(packageTextField, wrapInMain, imports);

        return ui;
    }

    @Override
    public Node getConfigurationUI() {
        this.newOptions = new KotlinSnippetExecutorOptions();
        try {
            this.newOptions.setKotlinHome(this.getOptions().getKotlinHome());
        } catch (FileNotFoundException | NullPointerException e) {
            LOGGER.log(Level.FINE, "Can not duplicate KOTLIN_HOME", e);
        }

        final Label label = new Label(this.getLanguage().concat(":"));

        final TextField kotlinHomeField = new TextField();
        kotlinHomeField.textProperty().bindBidirectional(this.newOptions.kotlinHomeProperty(), new FileStringConverter());
        kotlinHomeField.setPrefColumnCount(20);

        final Button browse = new Button("...");
        browse.setOnAction(event -> {
            final DirectoryChooser chooser = new DirectoryChooser();
            final File sdkHomeDir = chooser.showDialog(null);
            if (sdkHomeDir != null) {
                kotlinHomeField.setText(sdkHomeDir.getAbsolutePath());
            }

        });

        final HBox box = new HBox(5);
        box.getChildren().addAll(label, kotlinHomeField, browse);

        return box;
    }

    @Override
    public void saveNewOptions() {
        if(this.getNewOptions() != null) {
            this.setOptions(this.getNewOptions());

            if(this.getOptions().getKotlinHome() != null) {
                GlobalConfiguration.setProperty(this.getConfigurationBaseName().concat(KOTLIN_HOME_PROPERTY_SUFFIX),
                        this.getOptions().getKotlinHome().getAbsolutePath().replaceAll("\\\\", "/"));
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

            // Compile the Kotlin class
            final String jarFile = "Snippet.jar";
            final File koltincExecutable = new File(this.getOptions().getKotlinHome(), "bin/kotlinc");

            final String[] compilationCommand = {koltincExecutable.getAbsolutePath(), codeFile.getName(),
            "-include-runtime", "-d", jarFile};

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

            // Execute the Kotlin class only if the compilation was successful
            if(process != null && process.exitValue() == 0) {

                final File kotlinExecutable = new File(this.getOptions().getKotlinHome(), "bin/kotlin");
                final String[] executionCommand = {kotlinExecutable.getAbsolutePath(), jarFile};

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

                final File classFile = new File(this.getTemporaryDirectory(), jarFile);
                classFile.delete();
            }
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
        final File codeFile = new File(this.getTemporaryDirectory(), "Snippet.kt");
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

        if(hasPackage(codeSnippet)) {
            sourceCode.append(getPackage(codeSnippet)).append("\n\n");
        }

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

        return sourceCode.toString();
    }

    /**
     * Check if a package name has been specified.
     * @param codeSnippet The code snippet.
     * @return {@code true} if a package name has been defined, {@code false} otherwise.
     */
    protected boolean hasPackage(final CodeSnippet codeSnippet) {
        final String packageName = codeSnippet.getProperties().get(PACKAGE_NAME_PROPERTY);
        return packageName != null && !packageName.isEmpty();
    }

    /**
     * Get the package for the code snippet, defined by the {@link #PACKAGE_NAME_PROPERTY} property.
     *
     * @param codeSnippet The code snippet.
     * @return The value of the package property.
     */
    protected String getPackage(final CodeSnippet codeSnippet) {
        return formatPackageName(codeSnippet.getProperties().get(PACKAGE_NAME_PROPERTY));
    }

    /**
     * Formats the package name to include the {@code package} keyword to it if necessary.
     * @param packageName The name of the package to format.
     * @return A well formatted package name.
     */
    protected String formatPackageName(final String packageName) {
        final String packageLineBeginning = "package ";

        String formattedPackageLine;

        if(packageName.startsWith(packageLineBeginning)) {
            formattedPackageLine = packageName;
        } else {
            formattedPackageLine = packageLineBeginning.concat(packageName);
        }

        return formattedPackageLine;
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
        return "fun main(args: Array<String>) {";
    }

    /**
     * Get the end of the declaration of the main method.
     * @return The end of the main method.
     */
    protected String getEndMainMethod() {
        return "}";
    }
}

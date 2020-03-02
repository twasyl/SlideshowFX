package com.twasyl.slideshowfx.snippet.executor.groovy;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.plugin.Plugin;
import com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.utils.OSUtils;
import com.twasyl.slideshowfx.utils.beans.converter.FileStringConverter;
import com.twasyl.slideshowfx.utils.io.DefaultCharsetReader;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
 * Java code snippets.
 * This implementation is identified with the code {@code JAVA}.
 *
 * @author Thierry Wasyczenko
 * @version 1.1-SNAPSHOT
 * @since SlideshowFX 1.0
 */
@Plugin
public class GroovySnippetExecutor extends AbstractSnippetExecutor<GroovySnippetExecutorOptions> {

    private static final Logger LOGGER = Logger.getLogger(GroovySnippetExecutor.class.getName());

    private static final String GROOVY_HOME_PROPERTY_SUFFIX = ".home";
    /**
     * Indicates if the code should be wrapped in a main or run method (depending it is a Groovy Script or Class)
     */
    protected static final String WRAP_IN_METHOD_RUNNER = "wrapInMethodRunner";
    protected static final String IMPORTS_PROPERTY = "imports";
    protected static final String CLASS_NAME_PROPERTY = "class";
    protected static final String MAKE_SCRIPT = "makeScript";

    public GroovySnippetExecutor() {
        super("GROOVY", "Groovy", "language-groovy");
        this.setOptions(new GroovySnippetExecutorOptions());

        final String groovyHome = GlobalConfiguration.getProperty(this.getConfigurationBaseName().concat(GROOVY_HOME_PROPERTY_SUFFIX));
        if (groovyHome != null) {
            try {
                this.getOptions().setGroovyHome(new File(groovyHome));
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Can not set the GROOVY_HOME", e);
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

        final StringProperty codeEncapsulationType = new SimpleStringProperty("main");

        final Tooltip wrapInTooltip = new Tooltip();
        wrapInTooltip.textProperty().bind(new SimpleStringProperty("Wrap the provided code snippet in a Groovy ").concat(codeEncapsulationType).concat(" method"));

        final CheckBox wrapInMethodRunner = new CheckBox();
        wrapInMethodRunner.textProperty().bind(new SimpleStringProperty("Wrap code snippet in ").concat(codeEncapsulationType));
        wrapInMethodRunner.setTooltip(wrapInTooltip);
        wrapInMethodRunner.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
            if (newSelected != null) codeSnippet.putProperty(WRAP_IN_METHOD_RUNNER, newSelected.toString());
        });

        final CheckBox makeScript = new CheckBox("Make Groovy Script");
        makeScript.setTooltip(new Tooltip("Create a Groovy Script instead of a Groovy class"));
        makeScript.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
            if (newSelected != null) codeSnippet.putProperty(MAKE_SCRIPT, newSelected.toString());

            if (newSelected != null && newSelected) codeEncapsulationType.set("script");
            else codeEncapsulationType.set("main");
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
        ui.getChildren().addAll(classTextField, wrapInMethodRunner, makeScript, imports);

        return ui;
    }

    @Override
    public Node getConfigurationUI() {
        this.newOptions = new GroovySnippetExecutorOptions();
        try {
            this.newOptions.setGroovyHome(this.getOptions().getGroovyHome());
        } catch (FileNotFoundException | NullPointerException e) {
            LOGGER.log(Level.FINE, "Can not duplicate GROOVY_HOME", e);
        }

        final Label label = new Label(this.getLanguage().concat(":"));

        final TextField javaHomeField = new TextField();
        javaHomeField.textProperty().bindBidirectional(this.newOptions.groovyHomeProperty(), new FileStringConverter());
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
        if (this.getNewOptions() != null) {
            this.setOptions(this.getNewOptions());

            if (this.getOptions().getGroovyHome() != null) {
                GlobalConfiguration.setProperty(this.getConfigurationBaseName().concat(GROOVY_HOME_PROPERTY_SUFFIX),
                        this.getOptions().getGroovyHome().getAbsolutePath().replaceAll("\\\\", "/"));
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

            // Execute the class
            final File groovyExecutable = OSUtils.isWindows() ?
                    new File(this.getOptions().getGroovyHome(), "bin/groovy.bat") :
                    new File(this.getOptions().getGroovyHome(), "bin/groovy");

            final String[] executionCommand = {groovyExecutable.getAbsolutePath(), codeFile.getName()};

            Process process = null;
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
                consoleOutput.add("ERROR: ".concat(e.getMessage()));
            } finally {
                waitForProcess(process);
            }

            deleteGeneratedFile(codeFile);
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
        final File codeFile = new File(this.getTemporaryDirectory(), determineClassName(codeSnippet).concat(".groovy"));
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

        boolean someImportsPresent = false;

        if (makeScript(codeSnippet)) {
            sourceCode.append(getScriptImport()).append("\n");
            someImportsPresent = true;
        }

        if (hasImports(codeSnippet)) {
            sourceCode.append(getImports(codeSnippet)).append("\n");
            someImportsPresent = true;
        }

        if (someImportsPresent) sourceCode.append("\n");

        sourceCode.append(getStartClassDefinition(codeSnippet)).append("\n");

        if (mustBeWrappedIn(codeSnippet, WRAP_IN_METHOD_RUNNER)) {
            sourceCode.append("\t").append(getStartMainMethod(codeSnippet)).append("\n")
                    .append(codeSnippet.getCode())
                    .append("\n\t}");
        } else {
            sourceCode.append(codeSnippet.getCode());
        }

        sourceCode.append("\n}");

        return sourceCode.toString();
    }

    /**
     * Determine if the code snippet must make a groovy scriptr. It is determined by the presence and value of
     * the {@link #MAKE_SCRIPT} property.
     *
     * @param codeSnippet The code snippet.
     * @return {@code true} if the snippet must be created as a groovy script, {@code false} otherwise.
     */
    protected boolean makeScript(final CodeSnippet codeSnippet) {
        if (codeSnippet.getProperties().containsKey(MAKE_SCRIPT)) {
            return Boolean.parseBoolean(codeSnippet.getProperties().get(MAKE_SCRIPT));
        } else {
            return false;
        }
    }

    /**
     * Get the necessary import to create a groovy script.
     *
     * @return The well formatted import to create a groovy script.
     */
    protected String getScriptImport() {
        return formatImportLine("org.codehaus.groovy.runtime.InvokerHelper");
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
        final StringBuilder startClassDefinition = new StringBuilder("class ")
                .append(determineClassName(codeSnippet));

        if (makeScript(codeSnippet)) {
            startClassDefinition.append(" extends Script");
        }

        startClassDefinition.append(" {");

        return startClassDefinition.toString();
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

    /**
     * Get the start of the declaration of the main method.
     *
     * @return The start of the main method.
     */
    protected String getStartMainMethod(final CodeSnippet codeSnippet) {
        if (makeScript(codeSnippet)) return "def run() {";
        else return "def static main(String ... args) {";
    }
}

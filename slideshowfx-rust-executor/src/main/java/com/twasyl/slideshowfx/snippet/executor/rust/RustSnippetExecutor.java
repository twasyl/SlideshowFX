package com.twasyl.slideshowfx.snippet.executor.rust;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.plugin.Plugin;
import com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.utils.OSUtils;
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
 * Rust code snippets.
 * This implementation is identified with the code {@code RUST}.
 *
 * @author Thierry Wasyczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX 1.0
 */
@Plugin
public class RustSnippetExecutor extends AbstractSnippetExecutor<RustSnippetExecutorOptions> {
    private static final Logger LOGGER = Logger.getLogger(RustSnippetExecutor.class.getName());

    protected static final String CARGO_HOME_PROPERTY_SUFFIX = ".home";
    protected static final String WRAP_IN_MAIN_PROPERTY = "wrapInMain";
    protected static final String USES_PROPERTY = "uses";

    public RustSnippetExecutor() {
        super("RUST", "Rust", "language-rust");
        this.setOptions(new RustSnippetExecutorOptions());

        final String cargoHome = GlobalConfiguration.getProperty(this.getConfigurationBaseName().concat(CARGO_HOME_PROPERTY_SUFFIX));
        if (cargoHome != null) {
            try {
                this.getOptions().setCargoHome(new File(cargoHome));
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Can not set the CARGO_HOME", e);
            }
        }
    }

    @Override
    public Parent getUI(final CodeSnippet codeSnippet) {
        final CheckBox wrapInMain = new CheckBox("Wrap code snippet in main");
        wrapInMain.setTooltip(new Tooltip("Wrap the provided code snippet in a Rust main method"));
        wrapInMain.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
            if (newSelected != null) codeSnippet.putProperty(WRAP_IN_MAIN_PROPERTY, newSelected.toString());
        });

        final TextArea uses = new TextArea();
        uses.setPromptText("Uses");
        uses.setPrefColumnCount(15);
        uses.setPrefRowCount(15);
        uses.setWrapText(true);
        uses.textProperty().addListener((textValue, oldText, newText) -> {
            if (newText.isBlank()) codeSnippet.putProperty(USES_PROPERTY, null);
            else codeSnippet.putProperty(USES_PROPERTY, newText);
        });

        final VBox ui = new VBox(5);
        ui.getChildren().addAll(wrapInMain, uses);

        return ui;
    }

    @Override
    public Node getConfigurationUI() {
        this.newOptions = new RustSnippetExecutorOptions();
        try {
            this.newOptions.setCargoHome(this.getOptions().getCargoHome());
        } catch (FileNotFoundException | NullPointerException e) {
            LOGGER.log(Level.FINE, "Can not duplicate CARGO_HOME", e);
        }


        final Label label = new Label(this.getLanguage().concat(":"));

        final TextField rustHomeField = new TextField();
        rustHomeField.textProperty().bindBidirectional(this.newOptions.cargoHomeProperty(), new FileStringConverter());
        rustHomeField.setPrefColumnCount(20);

        final Button browse = new Button("...");
        browse.setOnAction(event -> {
            final DirectoryChooser chooser = new DirectoryChooser();
            final File sdkHomeDir = chooser.showDialog(null);
            if (sdkHomeDir != null) {
                rustHomeField.setText(sdkHomeDir.getAbsolutePath());
            }

        });

        return new VBox(5, new HBox(5, label, rustHomeField, browse));
    }

    @Override
    public void saveNewOptions() {
        if (this.getNewOptions() != null) {
            this.setOptions(this.getNewOptions());

            if (this.getOptions().getCargoHome() != null) {
                GlobalConfiguration.setProperty(this.getConfigurationBaseName().concat(CARGO_HOME_PROPERTY_SUFFIX),
                        this.sanitizePath(this.getOptions().getCargoHome()));
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

            // Compile the Rust file
            final String[] compilationCommand = {determineRustExecutable(), codeFile.getName()};

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

            // Execute the executable only if the compilation was successful
            if (process != null && process.exitValue() == 0) {

                final var executable = determineExecutableFile(codeFile);
                final String[] executionCommand = {executable.getAbsolutePath()};

                try {
                    process = new ProcessBuilder()
                            .redirectErrorStream(true)
                            .command(executionCommand)
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

                deleteGeneratedFile(executable);
            }
        });
        snippetThread.start();

        return consoleOutput;
    }

    private String determineRustExecutable() {
        final String executableExtension = OSUtils.isWindows() ? ".exe" : "";

        final File binDir = new File(this.getOptions().getCargoHome(), "bin");
        final File exec = new File(binDir, "rustc" + executableExtension);
        return exec.getAbsolutePath();
    }

    /**
     * Create the source code file for the given code snippet.
     *
     * @param codeSnippet The code snippet.
     * @return The file created and containing the source code.
     */
    private File createSourceCodeFile(final CodeSnippet codeSnippet) throws IOException {
        final File codeFile = new File(this.getTemporaryDirectory(), "Snippet.rs");
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
    private String buildSourceCode(final CodeSnippet codeSnippet) throws IOException {
        final StringBuilder sourceCode = new StringBuilder();

        if (hasUses(codeSnippet)) {
            sourceCode.append(getUses(codeSnippet)).append("\n\n");
        }

        if (mustBeWrappedIn(codeSnippet, WRAP_IN_MAIN_PROPERTY)) {
            sourceCode.append("fn main() {\n")
                    .append(codeSnippet.getCode())
                    .append("\n}");
        } else {
            sourceCode.append(codeSnippet.getCode());
        }

        return sourceCode.toString();
    }

    /**
     * Get the uses to be added to the source code.
     *
     * @param codeSnippet The code snippet.
     */
    protected boolean hasUses(final CodeSnippet codeSnippet) {
        final String uses = codeSnippet.getProperties().get(USES_PROPERTY);
        return uses != null && !uses.isEmpty();
    }

    /**
     * Get the uses for the code snippets. If some lines of the uses don't contain the {@code use} keyword, it
     * will be added properly.
     *
     * @param codeSnippet The code snippet.
     * @return A well formatted string containing all uses.
     */
    protected String getUses(final CodeSnippet codeSnippet) throws IOException {
        final StringJoiner uses = new StringJoiner("\n");

        try (final StringReader stringReader = new StringReader(codeSnippet.getProperties().get(USES_PROPERTY));
             final BufferedReader reader = new DefaultCharsetReader(stringReader)) {

            reader.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .forEach(line -> uses.add(formatUseLine(line)));
        }

        return uses.toString();
    }

    /**
     * Format a use line by make sure it starts with the {@code use} keyword.
     *
     * @param useLine The import line to format.
     * @return A well formatted import line.
     */
    protected String formatUseLine(final String useLine) {
        final String useLineBeginning = "use ";
        String formattedUseLine;

        if (useLine.startsWith(useLineBeginning)) {
            formattedUseLine = useLine;
        } else {
            formattedUseLine = useLineBeginning.concat(useLine);
        }

        if (!formattedUseLine.endsWith(";")) {
            formattedUseLine += ";";
        }

        return formattedUseLine;
    }

    private File determineExecutableFile(final File sourceFile) {
        var executableName = sourceFile.getName();
        executableName = executableName.substring(0, executableName.length() - 3);

        if (OSUtils.isWindows()) {
            executableName += ".exe";
        }

        return new File(sourceFile.getParentFile(), executableName);
    }
}

package com.twasyl.slideshowfx.snippet.executor.ruby;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.plugin.Plugin;
import com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import com.twasyl.slideshowfx.utils.OSUtils;
import com.twasyl.slideshowfx.utils.beans.converter.FileStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of {@link AbstractSnippetExecutor} that allows to execute
 * Ruby code snippets.
 * This implementation is identified with the code {@code RUBY}.
 *
 * @author Thierry Wasyczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX 1.0
 */
@Plugin
public class RubySnippetExecutor extends AbstractSnippetExecutor<RubySnippetExecutorOptions> {
    private static final Logger LOGGER = Logger.getLogger(RubySnippetExecutor.class.getName());

    protected static final String RUBY_HOME_PROPERTY_SUFFIX = ".home";
    protected static final String USE_RUBY_IN_PATH_PROPERTY_SUFFIX = ".useInPath";

    public RubySnippetExecutor() {
        super("RUBY", "Ruby", "language-ruby");
        this.setOptions(new RubySnippetExecutorOptions());

        this.getOptions().setUseRubyInPath(GlobalConfiguration.getBooleanProperty(this.getConfigurationBaseName().concat(USE_RUBY_IN_PATH_PROPERTY_SUFFIX), true));

        final String rubyHome = GlobalConfiguration.getProperty(this.getConfigurationBaseName().concat(RUBY_HOME_PROPERTY_SUFFIX));
        if (rubyHome != null) {
            try {
                this.getOptions().setRubyHome(new File(rubyHome));
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Can not set the RUBY_HOME", e);
            }
        }
    }

    @Override
    public Parent getUI(final CodeSnippet codeSnippet) {
        return null;
    }

    @Override
    public Node getConfigurationUI() {
        this.newOptions = new RubySnippetExecutorOptions();
        this.newOptions.setUseRubyInPath(this.getOptions().getUseRubyInPath());
        try {
            this.newOptions.setRubyHome(this.getOptions().getRubyHome());
        } catch (FileNotFoundException | NullPointerException e) {
            LOGGER.log(Level.FINE, "Can not duplicate RUBY_HOME", e);
        }


        final Label label = new Label(this.getLanguage().concat(":"));

        final TextField rubyHomeField = new TextField();
        rubyHomeField.textProperty().bindBidirectional(this.newOptions.rubyHomeProperty(), new FileStringConverter());
        rubyHomeField.setPrefColumnCount(20);

        final Button browse = new Button("...");
        browse.setOnAction(event -> {
            final DirectoryChooser chooser = new DirectoryChooser();
            final File sdkHomeDir = chooser.showDialog(null);
            if (sdkHomeDir != null) {
                rubyHomeField.setText(sdkHomeDir.getAbsolutePath());
            }

        });

        final CheckBox useRubyInPath = new CheckBox("Use ruby defined in PATH");
        useRubyInPath.selectedProperty().addListener((value, wasSelected, isSelected) -> {
            this.newOptions.setUseRubyInPath(isSelected);
            label.setDisable(isSelected);
            rubyHomeField.setDisable(isSelected);
            browse.setDisable(true);
        });
        useRubyInPath.setSelected(this.newOptions.getUseRubyInPath());

        return new VBox(5, useRubyInPath, new HBox(5, label, rubyHomeField, browse));
    }

    @Override
    public void saveNewOptions() {
        if (this.getNewOptions() != null) {
            this.setOptions(this.getNewOptions());

            GlobalConfiguration.setProperty(this.getConfigurationBaseName().concat(USE_RUBY_IN_PATH_PROPERTY_SUFFIX),
                    String.valueOf(this.getOptions().getUseRubyInPath()));

            if (this.getOptions().getRubyHome() != null) {
                GlobalConfiguration.setProperty(this.getConfigurationBaseName().concat(RUBY_HOME_PROPERTY_SUFFIX),
                        this.sanitizePath(this.getOptions().getRubyHome()));
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


            final String[] executionCommand = {determineRubyExecutable(), codeFile.getAbsolutePath()};

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

    private String determineRubyExecutable() {
        final String executableExtension = OSUtils.isWindows() ? ".exe" : "";

        if (this.getOptions().getUseRubyInPath()) {
            return "ruby" + executableExtension;
        } else {
            final File binDir = new File(this.getOptions().getRubyHome(), "bin");
            final File exec = new File(binDir, "ruby" + executableExtension);
            return exec.getAbsolutePath();
        }
    }

    /**
     * Create the source code file for the given code snippet.
     *
     * @param codeSnippet The code snippet.
     * @return The file created and containing the source code.
     */
    private File createSourceCodeFile(final CodeSnippet codeSnippet) throws IOException {
        final File codeFile = new File(this.getTemporaryDirectory(), "Snippet.rb");
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
    private String buildSourceCode(final CodeSnippet codeSnippet) {
        return codeSnippet.getCode();
    }
}

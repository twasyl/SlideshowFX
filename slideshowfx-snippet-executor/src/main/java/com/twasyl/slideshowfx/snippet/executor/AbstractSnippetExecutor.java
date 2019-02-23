package com.twasyl.slideshowfx.snippet.executor;

import com.twasyl.slideshowfx.plugin.AbstractPlugin;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Abstract implementation of a {@link com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor}. It takes care of
 * defining the {@link #getCode()}, {@link #getLanguage()}, {@link #getCssClass()}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public abstract class AbstractSnippetExecutor<T extends ISnippetExecutorOptions> extends AbstractPlugin<T> implements ISnippetExecutor<T> {
    private static final Logger LOGGER = Logger.getLogger(AbstractSnippetExecutor.class.getName());

    /*
     * Constants for stored properties
     */
    private static final String PROPERTIES_PREFIX = "snippet.executor.";
    protected T newOptions;

    private final String configurationBaseName;
    private final String code;
    private final String language;
    private final String cssClass;

    protected AbstractSnippetExecutor(final String code, final String language, final String cssClass) {
        super(code);
        this.code = code;
        this.language = language;
        this.cssClass = cssClass;

        this.configurationBaseName = PROPERTIES_PREFIX.concat(this.code);
    }

    @Override
    public String getConfigurationBaseName() {
        return this.configurationBaseName;
    }

    @Override
    public T getNewOptions() {
        return this.newOptions;
    }

    protected File getTemporaryDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getLanguage() {
        return this.language;
    }

    @Override
    public String getCssClass() {
        return this.cssClass;
    }

    /**
     * Sanitize the {@link File#getAbsolutePath() absolute path} of the given file by replacing all {@code \} by {@code /}.
     *
     * @param file The file to sanitize the path.
     * @return The sanitized path of the file.
     */
    protected String sanitizePath(final File file) {
        if (file != null) {
            return file.getAbsolutePath().replaceAll("\\\\", "/");
        }
        return null;
    }

    /**
     * Appends the exception as an error message to the given console output.
     *
     * @param consoleOutput The console output.
     * @param e             The exception to add as output.
     */
    protected void appendErrorMessageToConsole(final ObservableList<String> consoleOutput, final Exception e) {
        consoleOutput.add("ERROR: ".concat(e.getMessage()));
    }

    /**
     * Wait for the process to finish. If the process is {@code null}, nothing is performed.
     *
     * @param process The process to wait for.
     */
    protected void waitForProcess(final Process process) {
        if (process != null) {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                LOGGER.log(SEVERE, "Can not wait for process to end", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Deletes the generated file.
     *
     * @param file The file to be deleted.
     */
    protected void deleteGeneratedFile(final File file) {
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            LOGGER.log(WARNING, "Can not delete generated file " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Determine if the code snippet must be wrapped inside a block. It is determined by the presence and value of
     * the {@code propertyName} property.
     *
     * @param codeSnippet The code snippet.
     * @return {@code true} if the snippet must be wrapped in a block, {@code false} otherwise.
     */
    public boolean mustBeWrappedIn(final CodeSnippet codeSnippet, final String propertyName) {
        if (codeSnippet.getProperties().containsKey(propertyName)) {
            return Boolean.parseBoolean(codeSnippet.getProperties().get(propertyName));
        } else {
            return false;
        }
    }
}
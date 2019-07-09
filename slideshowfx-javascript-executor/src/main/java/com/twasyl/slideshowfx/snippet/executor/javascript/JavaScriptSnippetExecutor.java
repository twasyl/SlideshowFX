package com.twasyl.slideshowfx.snippet.executor.javascript;

import com.twasyl.slideshowfx.plugin.Plugin;
import com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;

/**
 * An implementation of
 * {@link com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor} that
 * allows to execute JavaScript code snippets. This implementation is identified
 * with the code {@code JAVASCRIPT}.
 *
 * @author Thierry Wasyczenko
 * @version 1.1-SNAPSHOT
 * @since SlideshowFX 1.0
 */
@Plugin
public class JavaScriptSnippetExecutor extends AbstractSnippetExecutor<JavaScriptSnippetExecutorOptions> {

    public JavaScriptSnippetExecutor() {
        super("JAVASCRIPT", "JavaScript", "language-javascript");
        this.setOptions(new JavaScriptSnippetExecutorOptions());
    }

    @Override
    public Parent getUI(final CodeSnippet codeSnippet) {
        return null;
    }

    @Override
    public Node getConfigurationUI() {
        return null;
    }

    @Override
    public void saveNewOptions() {
        // No options relative to the JavaScript executor
    }

    @Override
    public ObservableList<String> execute(final CodeSnippet codeSnippet) {
        final ObservableList<String> consoleOutput = FXCollections.observableArrayList();

        final Thread snippetThread = new Thread(() -> {
            final StringWriter writer = new StringWriter();
            final ScriptEngineManager manager = new ScriptEngineManager();

            final ScriptEngine engine = manager.getEngineByName("nashorn");
            engine.getContext().setWriter(writer);

            try {
                engine.eval(codeSnippet.getCode());
                consoleOutput.add(writer.toString());
            } catch (ScriptException ex) {
                consoleOutput.add(ex.getMessage());
            }
        });
        snippetThread.start();

        return consoleOutput;
    }
}

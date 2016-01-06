/*
 * Copyright 2016 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twasyl.slideshowfx.snippet.executor.javascript;

import com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor;
import com.twasyl.slideshowfx.snippet.executor.CodeSnippet;

import java.io.StringWriter;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.logging.Logger;
import javax.script.ScriptException;

/**
 * An implementation of
 * {@link com.twasyl.slideshowfx.snippet.executor.AbstractSnippetExecutor} that
 * allows to execute JavaScript code snippets. This implementation is identified
 * with the code {@code JAVASCRIPT}.
 *
 * @author Thierry Wasyczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class JavaScriptSnippetExecutor extends AbstractSnippetExecutor<JavaScriptSnippetExecutorOptions> {

    private static final Logger LOGGER = Logger.getLogger(JavaScriptSnippetExecutor.class.getName());

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

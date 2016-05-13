package com.twasyl.slideshowfx.snippet.executor;

import com.twasyl.slideshowfx.plugin.IConfigurable;
import com.twasyl.slideshowfx.plugin.IPlugin;
import javafx.collections.ObservableList;
import javafx.scene.Parent;

/**
 * Defines the contract to be considered as a snippet executor For SlideshowFX. A snippet executor is a feature allowing
 * to execute code snippet and display the result in the presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public interface ISnippetExecutor<T extends ISnippetExecutorOptions> extends IPlugin<T>, IConfigurable<T> {

    /**
     * Get the UI allowing to define the properties for this snippet executor. This method must take care of assuring
     * that the values entered for each properties in the UI are reflected inside the provided {@code codeSnippet}.
     * @param codeSnippet The code snippet object to be displayed in the UI.
     * @return The Node containing the whole UI to define the properties for this snippet executor.
     */
    Parent getUI(final CodeSnippet codeSnippet);

    /**
     * Get the code of this snippet executor. The code represents a unique ID between all snippet executors in order
     * to identify it.
     * @return The code of this snippet executor.
     */
    String getCode();

    /**
     * Get the name of the language this snippet executor executes.
     * @return The name of the language supported by this snippet executor.
     */
    String getLanguage();

    /**
     * Get the CSS class of the language of this snippet executor. This CSS class will be used to render the code snippet
     * in the SlideshowFX presentation.
     * @return The CSS class to highlight the code snippet in the presentation.
     */
    String getCssClass();

    /**
     * Executes the given {@code code} and return the execution console output. The console output is represented by a
     * list of String where each String is a line of the console output.
     * Note that this method is non blocking.
     * @param codeSnippet The code to execute.
     * @return The execution console output.
     */
    ObservableList<String> execute(final CodeSnippet codeSnippet);
}

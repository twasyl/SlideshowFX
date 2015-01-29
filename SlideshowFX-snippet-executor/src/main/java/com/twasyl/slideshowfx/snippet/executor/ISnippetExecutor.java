/*
 * Copyright 2015 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.snippet.executor;

import javafx.collections.ObservableList;
import javafx.scene.Parent;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Defines the contract to be considered as a snippet executor For SlideshowFX. A snippet executor is a feature allowing
 * to execute code snippet and display the result in the presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public interface ISnippetExecutor {

    /**
     * Get the UI allowing to define the properties for this snippet executor. This method must take care of assuring
     * that the values entered for each properties in the UI are reflected inside the provided {@code codeSnippet}.
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
     * Get the home of the SDK that will execute the code snippet. For instance for Java, it would be the value of the
     * JAVA_HOME variable.
     *
     * @return The home of the SDK executing the code snippet.
     */
    File getSdkHome();

    /**
     * Set the home of the SDK that will execute the code snippet.
     * @param sdkHome The home of the SDK.
     * @throws java.lang.NullPointerException If the {@code sdkHome} is {@code null}.
     * @throws java.io.FileNotFoundException If the {@code sdkHome} doesn't exist.
     * @throws java.lang.IllegalArgumentException If the {@code sdkHome} is not a directory.
     */
    void setSdkHome(File sdkHome) throws FileNotFoundException;

    /**
     * Set the home of the SDK of this executor and save it on the configuration properties. This method calls
     * {@link #setSdkHome(java.io.File)} to set the home.
     *
     * @param sdkHome The home of the SDK.
     */
    void saveSdkHome(File sdkHome) throws FileNotFoundException;

    /**
     * Executes the given {@code code} and return the execution console output. The console output is represented by a
     * list of String where each String is a line of the console output.
     * Note that this method is non blocking.
     * @param codeSnippet The code to execute.
     * @return The execution console output.
     */
    ObservableList<String> execute(final CodeSnippet codeSnippet);
}

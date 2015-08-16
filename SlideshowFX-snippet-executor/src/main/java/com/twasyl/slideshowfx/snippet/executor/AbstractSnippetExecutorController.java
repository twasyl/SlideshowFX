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

import javafx.fxml.Initializable;
import javafx.scene.Parent;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor} requires some configuration which could be defined
 * by the user in a UI. In order to provide a flexible way of define it, this class aims to provide an abstract controller
 * that could be implemented by each implementation of a ISnippetExecutor. This class provides the following useful
 * methods:
 * <ul>
 *     <li>{@link #getUI()} which is abstract and must return the UI allowing to configure the snippet executor ;</li>
 *     <li>{@link #getProperties()} which returns the properties that have been defined for the snippet executor ;</li>
 *     <li>{@link #getProperty(String)} which returns the value of a property identified by its name ;</li>
 *     <li>{@link #putProperty(String, Object)} which sets a property identified by its name and value.</li>
 * </ul>
 * 
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public abstract class AbstractSnippetExecutorController implements Initializable {
    
    protected final Map<String, Object> properties = new HashMap<>();
    
    abstract Parent getUI();

    /**
     * Returns all properties that have been defined for this controller. The result is never {@code null} but could be
     * empty if no properties have been defined.
     * @return The properties that have been defined.
     */
    public Map<String, Object> getProperties() { return this.properties; }

    /**
     * Gets a property identified by its {@code propertyName}.
     * @param propertyName The name of the property to get the value.
     * @return The value of the property or {@code null} if it hasn't been found.
     */
    public Object getProperty(final String propertyName) { return this.getProperties().get(propertyName); }

    /**
     * Define a new property identified by its {@code propertyName} and {@code value}.
     * @param propertyName The name of the property to define.
     * @param value The value of the property to define.
     * @return This controller instance in order to provide a fluent API.
     */
    public AbstractSnippetExecutorController putProperty(final String propertyName, final Object value) {
        this.properties.put(propertyName, value);
        return this;
    }
}

/*
 * Copyright 2014 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.utils;

import freemarker.template.Configuration;

/**
 * This class provides utility methods to interact with the template library, which is currently freemarker.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class TemplateProcessor {

    private static Configuration configurationSingleton;
    private static Configuration jsConfigurationSingleton;
    private static Configuration htmlConfigurationSingleton;

    /**
     * This method returns a {@link freemarker.template.Configuration} as a singleton object, as recommended in the
     * freemarker documentation.
     * @return A never {code null} Configuration object.
     */
    public static synchronized Configuration getDefaultConfiguration() {
        if(configurationSingleton == null) {
            configurationSingleton = new Configuration(Configuration.VERSION_2_3_21);
            configurationSingleton.setIncompatibleImprovements(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            configurationSingleton.setDefaultEncoding("UTF-8");
        }

        return configurationSingleton;
    }

    /**
     * This method returns a {@link freemarker.template.Configuration} as a singleton object, as recommended in the
     * freemarker documentation.
     * This configuration is initialized with the {@code /com/twasyl/slideshowfx/js/} path for
     * {@link freemarker.template.Configuration#setDirectoryForTemplateLoading(java.io.File)}
     * @return A never {code null} Configuration object.
     */
    public static synchronized Configuration getJsConfiguration() {
        if(jsConfigurationSingleton == null) {
            jsConfigurationSingleton = new Configuration(Configuration.VERSION_2_3_21);
            jsConfigurationSingleton.setIncompatibleImprovements(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            jsConfigurationSingleton.setDefaultEncoding("UTF-8");
            jsConfigurationSingleton.setClassForTemplateLoading(TemplateProcessor.class, "/com/twasyl/slideshowfx/js/");
        }

        return jsConfigurationSingleton;
    }

    /**
     * This method returns a {@link freemarker.template.Configuration} as a singleton object, as recommended in the
     * freemarker documentation.
     * This configuration is initialized with the {@code /com/twasyl/slideshowfx/html/} path for
     * {@link freemarker.template.Configuration#setDirectoryForTemplateLoading(java.io.File)}
     * @return A never {code null} Configuration object.
     */
    public static synchronized Configuration getHtmlConfiguration() {
        if(htmlConfigurationSingleton == null) {
            htmlConfigurationSingleton = new Configuration(Configuration.VERSION_2_3_21);
            htmlConfigurationSingleton.setIncompatibleImprovements(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            htmlConfigurationSingleton.setDefaultEncoding("UTF-8");
            htmlConfigurationSingleton.setClassForTemplateLoading(TemplateProcessor.class, "/com/twasyl/slideshowfx/html/");
        }

        return htmlConfigurationSingleton;
    }
}

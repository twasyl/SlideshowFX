package com.twasyl.slideshowfx.utils;

import freemarker.template.Configuration;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;

/**
 * This class provides utility methods to interact with the template library, which is currently freemarker.
 *
 * @author Thierry Wasylczenko
 * @version 1.1-SNAPSHOT
 * @since SlideshowFX 1.0
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
            configurationSingleton = new Configuration(Configuration.VERSION_2_3_23);
            configurationSingleton.setIncompatibleImprovements(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            configurationSingleton.setDefaultEncoding(getDefaultCharset().displayName());
        }

        return configurationSingleton;
    }

    /**
     * This method returns a {@link freemarker.template.Configuration} as a singleton object, as recommended in the
     * freemarker documentation.
     * This configuration is initialized with the {@code /com/twasyl/slideshowfx/server/webapp//js/} path for
     * {@link freemarker.template.Configuration#setDirectoryForTemplateLoading(java.io.File)}
     * @param classForTemplateLoading The class that will load the template.
     * @return A never {code null} Configuration object.
     */
    public static synchronized Configuration getJsConfiguration(Class classForTemplateLoading) {
        if(jsConfigurationSingleton == null) {
            jsConfigurationSingleton = new Configuration(Configuration.VERSION_2_3_23);
            jsConfigurationSingleton.setIncompatibleImprovements(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            jsConfigurationSingleton.setDefaultEncoding(getDefaultCharset().displayName());
            jsConfigurationSingleton.setClassForTemplateLoading(classForTemplateLoading, "/com/twasyl/slideshowfx/server/webapp/js/");
        }

        return jsConfigurationSingleton;
    }

    /**
     * This method returns a {@link freemarker.template.Configuration} as a singleton object, as recommended in the
     * freemarker documentation.
     * This configuration is initialized with the {@code /com/twasyl/slideshowfx/server/webapp/html/} path for
     * {@link freemarker.template.Configuration#setDirectoryForTemplateLoading(java.io.File)}
     * @param classForTemplateLoading The class that will load the template.
     * @return A never {code null} Configuration object.
     */
    public static synchronized Configuration getHtmlConfiguration(Class classForTemplateLoading) {
        if(htmlConfigurationSingleton == null) {
            htmlConfigurationSingleton = new Configuration(Configuration.VERSION_2_3_23);
            htmlConfigurationSingleton.setIncompatibleImprovements(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            htmlConfigurationSingleton.setDefaultEncoding(getDefaultCharset().displayName());
            htmlConfigurationSingleton.setClassForTemplateLoading(classForTemplateLoading, "/com/twasyl/slideshowfx/server/webapp/html/");
        }

        return htmlConfigurationSingleton;
    }
}

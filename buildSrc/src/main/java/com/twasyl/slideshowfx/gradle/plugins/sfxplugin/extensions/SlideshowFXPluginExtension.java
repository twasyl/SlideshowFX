package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.twasyl.slideshowfx.gradle.Utils.stripProjectVersion;
import static com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.PLUGIN_DEPENDENCIES_CONFIGURATION_NAME;
import static java.util.stream.Collectors.toList;

/**
 * Plugin extension for defining the properties of the plugin.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class SlideshowFXPluginExtension {
    private final Property<String> pluginName;
    private final Property<String> pluginDescription;
    private final Property<String> setupWizardIconName;
    private final Property<Boolean> contentExtension;
    private final Property<Boolean> hostingConnector;
    private final Property<Boolean> snippetExecutor;
    private final Property<Boolean> markupPlugin;
    private final DirectoryProperty sfxDir;
    private final DirectoryProperty pluginsDir;

    @Inject
    public SlideshowFXPluginExtension(final ObjectFactory objects) {
        this.pluginName = objects.property(String.class).convention("");
        this.pluginDescription = objects.property(String.class).convention("");
        this.setupWizardIconName = objects.property(String.class).convention("");
        this.contentExtension = objects.property(Boolean.class).convention(Boolean.FALSE);
        this.hostingConnector = objects.property(Boolean.class).convention(Boolean.FALSE);
        this.snippetExecutor = objects.property(Boolean.class).convention(Boolean.FALSE);
        this.markupPlugin = objects.property(Boolean.class).convention(Boolean.FALSE);

        final var sfx = new File(System.getProperty("user.home"), ".SlideshowFX");
        this.sfxDir = objects.directoryProperty().convention(objects.directoryProperty().dir(sfx.getAbsolutePath()));
        this.pluginsDir = objects.directoryProperty().convention(sfxDir.dir("plugins"));
    }

    public Property<String> getPluginName() {
        return pluginName;
    }

    public Property<String> getPluginDescription() {
        return pluginDescription;
    }

    public Property<String> getSetupWizardIconName() {
        return setupWizardIconName;
    }

    public Property<Boolean> getContentExtension() {
        return contentExtension;
    }

    public Property<Boolean> getHostingConnector() {
        return hostingConnector;
    }

    public Property<Boolean> getSnippetExecutor() {
        return snippetExecutor;
    }

    public Property<Boolean> getMarkupPlugin() {
        return markupPlugin;
    }

    public DirectoryProperty getSfxDir() {
        return sfxDir;
    }

    public DirectoryProperty getPluginsDir() {
        return pluginsDir;
    }

    public String getBundlePackageDestination() {
        String location = "plugins" + File.separator;

        if (getMarkupPlugin().get()) {
            location += "markups";
        } else if (getSnippetExecutor().get()) {
            location += "executors";
        } else if (getHostingConnector().get()) {
            location += "hostingConnectors";
        } else if (getContentExtension().get()) {
            location += "extensions";
        } else {
            return null;
        }

        return location;
    }

    public Map<String, Object> buildManifestAttributes(final Project project) {
        final Map<String, Object> manifestAttributes = new HashMap<>();

        populateManifestWithPlugin(manifestAttributes, project);
        populateManifestWithSetup(manifestAttributes);

        return manifestAttributes;
    }

    private String determineClasspath(Project project) {
        final List<String> classPath = project.getConfigurations().getByName(PLUGIN_DEPENDENCIES_CONFIGURATION_NAME)
                .resolve()
                .stream()
                .map(file -> "libs/" + file.getName())
                .collect(toList());

        if (classPath.isEmpty()) {
            return null;
        } else {
            return classPath.stream().collect(Collectors.joining(",")).concat(",.");
        }
    }

    private void populateManifestWithPlugin(Map<String, Object> manifestAttributes, Project project) {
        putValueIfValid(manifestAttributes, "Class-Path", determineClasspath(project));
        putValueIfValid(manifestAttributes, "Plugin-Name", getPluginName().get());
        putValueIfValid(manifestAttributes, "Plugin-Description", getPluginDescription().get());
        putValueIfValid(manifestAttributes, "Plugin-Version", stripProjectVersion(project));
    }

    private void populateManifestWithSetup(Map<String, Object> manifestAttributes) {
        putValueIfValid(manifestAttributes, "Setup-Wizard-Icon-Name", getSetupWizardIconName().get());
    }

    private void putValueIfValid(final Map<String, Object> manifestAttributes, final String key, final String value) {
        putValueIfValid(manifestAttributes, key, value, null);
    }

    private void putValueIfValid(final Map<String, Object> manifestAttributes, final String key, final String value, final String defaultValue) {
        if (value != null && !value.isBlank()) {
            manifestAttributes.put(key, value.trim());
        } else if (defaultValue != null) {
            manifestAttributes.put(key, defaultValue);
        }
    }
}
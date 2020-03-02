package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions;

import org.gradle.api.Project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.twasyl.slideshowfx.gradle.Utils.stripProjectVersion;
import static com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.PLUGIN_DEPENDENCIES_CONFIGURATION_NAME;
import static java.util.stream.Collectors.toList;

/**
 * Extension to define the properties of the plugin bundle.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class SlideshowFXBundleExtension {
    private String name;
    private String description;
    private String setupWizardIconName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSetupWizardIconName() {
        return setupWizardIconName;
    }

    public void setSetupWizardIconName(String setupWizardIconName) {
        this.setupWizardIconName = setupWizardIconName;
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
        putValueIfValid(manifestAttributes, "Plugin-Name", getName());
        putValueIfValid(manifestAttributes, "Plugin-Description", getDescription());
        putValueIfValid(manifestAttributes, "Plugin-Version", stripProjectVersion(project));
    }

    private void populateManifestWithSetup(Map<String, Object> manifestAttributes) {
        putValueIfValid(manifestAttributes, "Setup-Wizard-Icon-Name", getSetupWizardIconName());
    }

    private void putValueIfValid(final Map<String, Object> manifestAttributes, final String key, final String value) {
        putValueIfValid(manifestAttributes, key, value, null);
    }

    private void putValueIfValid(final Map<String, Object> manifestAttributes, final String key, final String value, final String defaultValue) {
        if (value != null && !value.trim().isEmpty()) {
            manifestAttributes.put(key, value.trim());
        } else if (defaultValue != null) {
            manifestAttributes.put(key, defaultValue);
        }
    }
}

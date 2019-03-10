package com.twasyl.slideshowfx.gradle.plugins.internal;

import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

public class SlideshowFXBundleExtension {
    private String name;
    private String symbolicName;
    private String description;
    private String activator;
    private String classpath;
    private String vendor;
    private String exportPackage;
    private String setupWizardLabel;
    private String setupWizardIconName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActivator() {
        return activator;
    }

    public void setActivator(String activator) {
        this.activator = activator;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getExportPackage() {
        return exportPackage;
    }

    public void setExportPackage(String exportPackage) {
        this.exportPackage = exportPackage;
    }

    public String getSetupWizardLabel() {
        return setupWizardLabel;
    }

    public void setSetupWizardLabel(String setupWizardLabel) {
        this.setupWizardLabel = setupWizardLabel;
    }

    public String getSetupWizardIconName() {
        return setupWizardIconName;
    }

    public void setSetupWizardIconName(String setupWizardIconName) {
        this.setupWizardIconName = setupWizardIconName;
    }

    public Map<String, Object> buildManifestAttributes(final Project project) {
        final Map<String, Object> manifestAttributes = new HashMap<>();

        manifestAttributes.put("Bundle-ManifestVersion", "2");
        manifestAttributes.put("Import-Package", "org.osgi.framework");

        putValueIfValid(manifestAttributes, "Bundle-Name", getName());
        putValueIfValid(manifestAttributes, "Bundle-SymbolicName", getSymbolicName());
        putValueIfValid(manifestAttributes, "Bundle-Version", stripProjectVersion(project));
        putValueIfValid(manifestAttributes, "Bundle-Description", getDescription());
        putValueIfValid(manifestAttributes, "Bundle-Activator", getActivator());
        putValueIfValid(manifestAttributes, "Bundle-ClassPath", getClasspath(), ".");
        putValueIfValid(manifestAttributes, "Bundle-Vendor", getVendor());
        putValueIfValid(manifestAttributes, "Export-Package", getExportPackage());
        putValueIfValid(manifestAttributes, "Setup-Wizard-Label", getSetupWizardLabel());
        putValueIfValid(manifestAttributes, "Setup-Wizard-Icon-Name", getSetupWizardIconName());

        return manifestAttributes;
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

    private String stripProjectVersion(final Project project) {
        final String version = project.getVersion().toString();
        if (version.contains("-SNAPSHOT")) {
            return version.replace("-SNAPSHOT", "");
        } else {
            return version;
        }
    }
}

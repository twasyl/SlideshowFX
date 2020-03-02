package com.twasyl.slideshowfx.engine.template.configuration;

import com.twasyl.slideshowfx.engine.IConfiguration;
import com.twasyl.slideshowfx.engine.Variable;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Represents the template found in the template configuration file.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class TemplateConfiguration implements IConfiguration {
    private String name;
    private String version;
    private File file;
    private Set<Variable> defaultVariables;
    private List<SlideTemplate> slideTemplates;
    private String contentDefinerMethod;
    private String updateCodeSnippetConsoleMethod;
    private String gotoSlideMethod;
    private String getCurrentSlideMethod;
    private String jsObject;
    private String sfxServerObject;
    private File slidesTemplateDirectory;
    private File resourcesDirectory;
    private String slideIdPrefix;
    private String slidesContainer;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Set<Variable> getDefaultVariables() {
        return defaultVariables;
    }

    public void setDefaultVariables(Set<Variable> defaultVariables) {
        this.defaultVariables = defaultVariables;
    }

    public List<SlideTemplate> getSlideTemplates() {
        return slideTemplates;
    }

    public void setSlideTemplates(List<SlideTemplate> slideTemplates) {
        this.slideTemplates = slideTemplates;
    }

    public String getContentDefinerMethod() {
        return contentDefinerMethod;
    }

    public void setContentDefinerMethod(String contentDefinerMethod) {
        this.contentDefinerMethod = contentDefinerMethod;
    }

    public String getGetCurrentSlideMethod() {
        return getCurrentSlideMethod;
    }

    public void setGetCurrentSlideMethod(String getCurrentSlideMethod) {
        this.getCurrentSlideMethod = getCurrentSlideMethod;
    }

    public String getJsObject() {
        return jsObject;
    }

    public void setJsObject(String jsObject) {
        this.jsObject = jsObject;
    }

    public String getSfxServerObject() {
        return sfxServerObject;
    }

    public void setSfxServerObject(String sfxServerObject) {
        this.sfxServerObject = sfxServerObject;
    }

    public File getSlidesTemplateDirectory() {
        return slidesTemplateDirectory;
    }

    public void setSlidesTemplateDirectory(File slidesTemplateDirectory) {
        this.slidesTemplateDirectory = slidesTemplateDirectory;
    }

    public File getResourcesDirectory() {
        return resourcesDirectory;
    }

    public void setResourcesDirectory(File resourcesDirectory) {
        this.resourcesDirectory = resourcesDirectory;
    }

    public String getSlideIdPrefix() {
        return slideIdPrefix;
    }

    public void setSlideIdPrefix(String slideIdPrefix) {
        this.slideIdPrefix = slideIdPrefix;
    }

    public String getSlidesContainer() {
        return slidesContainer;
    }

    public void setSlidesContainer(String slidesContainer) {
        this.slidesContainer = slidesContainer;
    }

    public String getUpdateCodeSnippetConsoleMethod() {
        return updateCodeSnippetConsoleMethod;
    }

    public void setUpdateCodeSnippetConsoleMethod(String updateCodeSnippetConsoleMethod) {
        this.updateCodeSnippetConsoleMethod = updateCodeSnippetConsoleMethod;
    }

    public String getGotoSlideMethod() {
        return gotoSlideMethod;
    }

    public void setGotoSlideMethod(String gotoSlideMethod) {
        this.gotoSlideMethod = gotoSlideMethod;
    }

    public SlideTemplate getSlideTemplate(int slideId) {
        return getSlideTemplates()
                .stream()
                .filter(template -> slideId == template.getId())
                .findAny()
                .orElse(null);
    }
}

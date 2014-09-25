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

package com.twasyl.slideshowfx.engine.template.configuration;

import com.twasyl.slideshowfx.engine.IConfiguration;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents the template found in the template configuration file.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class TemplateConfiguration implements IConfiguration {
    private static final Logger LOGGER = Logger.getLogger(TemplateConfiguration.class.getName());

    private String name;
    private File file;
    private List<SlideTemplateConfiguration> slideTemplateConfigurations;
    private String contentDefinerMethod;
    private String getCurrentSlideMethod;
    private String jsObject;
    private File slidesTemplateDirectory;
    private File slidesPresentationDirectory;
    private File slidesThumbnailDirectory;
    private File resourcesDirectory;
    private String slideIdPrefix;
    private String slidesContainer;

    public TemplateConfiguration() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public File getFile() { return file;  }
    public void setFile(File file) { this.file = file; }

    public List<SlideTemplateConfiguration> getSlideTemplateConfigurations() { return slideTemplateConfigurations; }
    public void setSlideTemplateConfigurations(List<SlideTemplateConfiguration> slideTemplateConfigurations) { this.slideTemplateConfigurations = slideTemplateConfigurations; }

    public String getContentDefinerMethod() { return contentDefinerMethod; }
    public void setContentDefinerMethod(String contentDefinerMethod) { this.contentDefinerMethod = contentDefinerMethod; }

    public String getGetCurrentSlideMethod() { return getCurrentSlideMethod; }
    public void setGetCurrentSlideMethod(String getCurrentSlideMethod) { this.getCurrentSlideMethod = getCurrentSlideMethod; }

    public String getJsObject() { return jsObject; }
    public void setJsObject(String jsObject) { this.jsObject = jsObject; }

    public File getSlidesTemplateDirectory() { return slidesTemplateDirectory; }
    public void setSlidesTemplateDirectory(File slidesTemplateDirectory) { this.slidesTemplateDirectory = slidesTemplateDirectory; }

    public File getSlidesPresentationDirectory() { return slidesPresentationDirectory; }
    public void setSlidesPresentationDirectory(File slidesPresentationDirectory) { this.slidesPresentationDirectory = slidesPresentationDirectory; }

    public File getResourcesDirectory() { return resourcesDirectory; }
    public void setResourcesDirectory(File resourcesDirectory) { this.resourcesDirectory = resourcesDirectory; }

    public File getSlidesThumbnailDirectory() { return slidesThumbnailDirectory; }
    public void setSlidesThumbnailDirectory(File slidesThumbnailDirectory) { this.slidesThumbnailDirectory = slidesThumbnailDirectory; }

    public String getSlideIdPrefix() { return slideIdPrefix; }
    public void setSlideIdPrefix(String slideIdPrefix) { this.slideIdPrefix = slideIdPrefix; }

    public String getSlidesContainer() { return slidesContainer; }
    public void setSlidesContainer(String slidesContainer) { this.slidesContainer = slidesContainer; }

    public SlideTemplateConfiguration getSlideTemplate(int slideId) {
        SlideTemplateConfiguration searchedTemplate = null;

        for(SlideTemplateConfiguration slideTemplateConfiguration : getSlideTemplateConfigurations()) {
            if(slideId == slideTemplateConfiguration.getId()) {
                searchedTemplate = slideTemplateConfiguration;
                break;
            }
        }

        return searchedTemplate;
    }
}

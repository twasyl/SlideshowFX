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

package com.twasyl.slideshowfx.builder.template;

import com.twasyl.slideshowfx.utils.JSONHelper;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents the template found in the template configuration file
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class Template {
    private static final Logger LOGGER = Logger.getLogger(Template.class.getName());
    protected static final String TEMPLATE_CONFIGURATION_NAME = "template-config.json";

    private File folder;
    private File configurationFile;
    private String name;
    private File file;
    private List<SlideTemplate> slideTemplates;
    private String contentDefinerMethod;
    private String getCurrentSlideMethod;
    private String jsObject;
    private File slidesTemplateDirectory;
    private File slidesPresentationDirectory;
    private File slidesThumbnailDirectory;
    private File resourcesDirectory;
    private String slideIdPrefix;
    private String slidesContainer;

    public Template() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public File getFile() { return file;  }
    public void setFile(File file) { this.file = file; }

    public List<SlideTemplate> getSlideTemplates() { return slideTemplates; }
    public void setSlideTemplates(List<SlideTemplate> slideTemplates) { this.slideTemplates = slideTemplates; }

    public File getFolder() { return folder; }
    public void setFolder(File folder) { this.folder = folder; }

    public File getConfigurationFile() { return configurationFile; }
    public void setConfigurationFile(File configurationFile) { this.configurationFile = configurationFile; }

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

    public SlideTemplate getSlideTemplate(int slideId) {
        SlideTemplate searchedTemplate = null;

        for(SlideTemplate slideTemplate : getSlideTemplates()) {
            if(slideId == slideTemplate.getId()) {
                searchedTemplate = slideTemplate;
                break;
            }
        }

        return searchedTemplate;
    }
    /**
     * Read the configuration of this template located in the <b>folder</b> attribute.
     */
    public void readFromFolder() throws IOException {

        // Set the template information
        LOGGER.fine("Starting reading template configuration");
        this.setConfigurationFile(new File(this.getFolder(), Template.TEMPLATE_CONFIGURATION_NAME));


        JsonObject configuration = JSONHelper.readFromFile(this.getConfigurationFile());

        JsonObject templateJson = configuration.getObject("template");

        this.setName(templateJson.getString("name"));
        LOGGER.fine("[Template configuration] name = " + this.getName());

        this.setFile(new File(this.getFolder(), templateJson.getString("file")));
        LOGGER.fine("[Template configuration] file = " + this.getFile().getAbsolutePath());

        this.setJsObject(templateJson.getString("js-object"));
        LOGGER.fine("[Template configuration] jsObject = " + this.getJsObject());

        this.setResourcesDirectory(new File(this.getFolder(), templateJson.getString("resources-directory")));
        LOGGER.fine("[Template configuration] resources-directory = " + this.getResourcesDirectory().getAbsolutePath());

        templateJson.getArray("methods")
                    .forEach(method -> {
                        if ("CONTENT_DEFINER".equals(((JsonObject) method).getString("type"))) {
                            this.setContentDefinerMethod(((JsonObject) method).getString("name"));
                            LOGGER.fine("[Template configuration] content definer method = " + this.getContentDefinerMethod());
                        } else if ("GET_CURRENT_SLIDE".equals(((JsonObject) method).getString("type"))) {
                            this.setGetCurrentSlideMethod(((JsonObject) method).getString("name"));
                            LOGGER.fine("[Template configuration] get current slide method = " + this.getGetCurrentSlideMethod());
                        }
                    });

        // Setting the slides
        this.setSlideTemplates(new ArrayList<SlideTemplate>());
        JsonObject slidesJson = templateJson.getObject("slides");

        if (slidesJson != null) {
            LOGGER.fine("Reading slide's configuration");
            JsonObject slidesConfigurationJson = slidesJson.getObject("configuration");

            this.setSlidesTemplateDirectory(new File(this.getFolder(), slidesConfigurationJson.getString("template-directory")));
            LOGGER.fine("[Slide's configuration] template directory = " + this.getSlidesTemplateDirectory().getAbsolutePath());

            this.setSlidesPresentationDirectory(new File(this.getFolder(), slidesConfigurationJson.getString("presentation-directory")));
            LOGGER.fine("[Slide's configuration] presentation directory = " + this.getSlidesPresentationDirectory().getAbsolutePath());

            this.setSlidesThumbnailDirectory(new File(this.getFolder(), slidesConfigurationJson.getString("thumbnail-directory")));
            LOGGER.fine("[Slide's configuration] slides thumbnail directory = " + this.getSlidesThumbnailDirectory().getAbsolutePath());

            this.setSlideIdPrefix(slidesConfigurationJson.getString("slide-id-prefix"));
            LOGGER.fine("[Template configuration] slideIdPrefix = " + this.getSlideIdPrefix());

            this.setSlidesContainer(slidesConfigurationJson.getString("slides-container"));
            LOGGER.fine("[Template configuration] slidesContainer = " + this.getSlidesContainer());

            slidesJson.getArray("slides-definition")
                    .forEach(slideJson -> {
                        Number number;

                        final SlideTemplate slideTemplate = new SlideTemplate();
                        slideTemplate.setId((number = ((JsonObject) slideJson).getNumber("id")) != null ? number.intValue() : -1);
                        LOGGER.fine("[Slide definition] id = " + slideTemplate.getId());

                        slideTemplate.setName(((JsonObject) slideJson).getString("name"));
                        LOGGER.fine("[Slide definition] name = " + slideTemplate.getName());

                        slideTemplate.setFile(new File(this.getSlidesTemplateDirectory(), ((JsonObject) slideJson).getString("file")));
                        LOGGER.fine("[Slide definition] file = " + slideTemplate.getFile().getAbsolutePath());

                        final JsonArray dynamicIdsJson = ((JsonObject) slideJson).getArray("dynamic-ids");
                        if (dynamicIdsJson != null && dynamicIdsJson.size() > 0) {
                            slideTemplate.setDynamicIds(new String[dynamicIdsJson.size()]);

                            for (int index = 0; index < dynamicIdsJson.size(); index++) {
                                slideTemplate.getDynamicIds()[index] = dynamicIdsJson.get(index);
                            }
                        }

                        final JsonArray dynamicAttributesJson = ((JsonObject) slideJson).getArray("dynamic-attributes");
                        if (dynamicAttributesJson != null && dynamicAttributesJson.size() > 0) {
                            slideTemplate.setDynamicAttributes(new DynamicAttribute[dynamicAttributesJson.size()]);
                            DynamicAttribute dynamicAttribute;
                            JsonObject dynamicAttributeJson;

                            for (int index = 0; index < dynamicAttributesJson.size(); index++) {
                                dynamicAttribute = new DynamicAttribute();
                                dynamicAttributeJson = dynamicAttributesJson.get(index);

                                dynamicAttribute.setAttribute(dynamicAttributeJson.getString("attribute"));
                                dynamicAttribute.setPromptMessage(dynamicAttributeJson.getString("prompt-message"));
                                dynamicAttribute.setTemplateExpression(dynamicAttributeJson.getString("template-expression"));

                                slideTemplate.getDynamicAttributes()[index] = dynamicAttribute;
                            }
                        }

                        this.getSlideTemplates().add(slideTemplate);
                    });
        } else {
            LOGGER.fine("No slide's configuration found");
        }
    }
}

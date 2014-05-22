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

import javax.json.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents the template found in the template configuration file
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
    public void readFromFolder() throws FileNotFoundException {

        // Set the template information
        LOGGER.fine("Starting reading template configuration");
        this.setConfigurationFile(new File(this.getFolder(), Template.TEMPLATE_CONFIGURATION_NAME));

        JsonReader configurationReader = Json.createReader(new FileInputStream(this.getConfigurationFile()));
        JsonObject templateJson = configurationReader.readObject().getJsonObject("template");

        this.setName(templateJson.getString("name"));
        LOGGER.fine("[Template configuration] name = " + this.getName());

        this.setFile(new File(this.getFolder(), templateJson.getString("file")));
        LOGGER.fine("[Template configuration] file = " + this.getFile().getAbsolutePath());

        this.setJsObject(templateJson.getString("js-object"));
        LOGGER.fine("[Template configuration] jsObject = " + this.getJsObject());

        this.setResourcesDirectory(new File(this.getFolder(), templateJson.getString("resources-directory")));
        LOGGER.fine("[Template configuration] resources-directory = " + this.getResourcesDirectory().getAbsolutePath());

        JsonArray methodsJson = templateJson.getJsonArray("methods");

        if(methodsJson != null && methodsJson.size() > 0) {
            for(JsonValue method : methodsJson) {
                if(method.getValueType().equals(JsonValue.ValueType.OBJECT)) {
                    if("CONTENT_DEFINER".equals(((JsonObject) method).getString("type"))) {
                        this.setContentDefinerMethod(((JsonObject) method).getString("name"));
                        LOGGER.fine("[Template configuration] content definer method = " + this.getContentDefinerMethod());
                    } else if("GET_CURRENT_SLIDE".equals(((JsonObject) method).getString("type"))) {
                        this.setGetCurrentSlideMethod(((JsonObject) method).getString("name"));
                        LOGGER.fine("[Template configuration] get current slide method = " + this.getGetCurrentSlideMethod());
                    }
                }
            }
        }

        // Setting the slides
        this.setSlideTemplates(new ArrayList<SlideTemplate>());
        JsonObject slidesJson = templateJson.getJsonObject("slides");

        if (slidesJson != null) {
            LOGGER.fine("Reading slide's configuration");
            JsonObject slidesConfigurationJson = slidesJson.getJsonObject("configuration");

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

            JsonArray slidesDefinition = slidesJson.getJsonArray("slides-definition");

            if(slidesDefinition != null && !slidesDefinition.isEmpty()) {
                SlideTemplate slideTemplate;
                JsonArray dynamicIdsJson;
                JsonArray dynamicAttributesJson;

                for (JsonObject slideJson : slidesDefinition.getValuesAs(JsonObject.class)) {
                    slideTemplate = new SlideTemplate();
                    slideTemplate.setId(slideJson.getInt("id"));
                    LOGGER.fine("[Slide definition] id = " + slideTemplate.getId());

                    slideTemplate.setName(slideJson.getString("name"));
                    LOGGER.fine("[Slide definition] name = " + slideTemplate.getName());

                    slideTemplate.setFile(new File(this.getSlidesTemplateDirectory(), slideJson.getString("file")));
                    LOGGER.fine("[Slide definition] file = " + slideTemplate.getFile().getAbsolutePath());

                    dynamicIdsJson = slideJson.getJsonArray("dynamic-ids");
                    if(dynamicIdsJson != null && !dynamicIdsJson.isEmpty()) {
                        slideTemplate.setDynamicIds(new String[dynamicIdsJson.size()]);

                        for(int index = 0; index < dynamicIdsJson.size(); index++) {
                            slideTemplate.getDynamicIds()[index] = dynamicIdsJson.getString(index);
                        }
                    }

                    dynamicAttributesJson = slideJson.getJsonArray("dynamic-attributes");
                    if(dynamicAttributesJson != null && !dynamicAttributesJson.isEmpty()) {
                        slideTemplate.setDynamicAttributes(new DynamicAttribute[dynamicAttributesJson.size()]);
                        DynamicAttribute dynamicAttribute;
                        JsonObject dynamicAttributeJson;

                        for(int index = 0; index < dynamicAttributesJson.size(); index++) {
                            dynamicAttribute = new DynamicAttribute();
                            dynamicAttributeJson = dynamicAttributesJson.getJsonObject(index);

                            dynamicAttribute.setAttribute(dynamicAttributeJson.getString("attribute"));
                            dynamicAttribute.setPromptMessage(dynamicAttributeJson.getString("prompt-message"));
                            dynamicAttribute.setTemplateExpression(dynamicAttributeJson.getString("template-expression"));

                            slideTemplate.getDynamicAttributes()[index] = dynamicAttribute;
                        }
                    }

                    this.getSlideTemplates().add(slideTemplate);
                }
            } else {
                LOGGER.fine("No slide's definition found");
            }
        } else {
            LOGGER.fine("No slide's configuration found");
        }
    }
}

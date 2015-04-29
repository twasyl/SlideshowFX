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

package com.twasyl.slideshowfx.engine.template;

import com.twasyl.slideshowfx.engine.AbstractEngine;
import com.twasyl.slideshowfx.engine.EngineException;
import com.twasyl.slideshowfx.engine.template.configuration.SlideElementTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration;
import com.twasyl.slideshowfx.utils.JSONHelper;
import com.twasyl.slideshowfx.utils.ZipUtils;
import com.twasyl.slideshowfx.utils.beans.Pair;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * This class is used to managed the templates provided by SlideshowFX.
 * The extension of a template is <code>sfxt</code>.
 *
 * @author Thierry Wasylczenko
 */
public class TemplateEngine extends AbstractEngine<TemplateConfiguration> {
    private static final Logger LOGGER = Logger.getLogger(TemplateEngine.class.getName());

    public TemplateEngine() {
        super("sfxt", "template-config.json");
    }

    @Override
    public boolean checkConfiguration() throws EngineException {
        return false;
    }

    @Override
    public TemplateConfiguration readConfiguration(File configurationFile) throws NullPointerException, IOException, IllegalAccessException {
        if(configurationFile == null) throw new NullPointerException("The configuration file can not be null");
        if(!configurationFile.exists()) throw new FileNotFoundException("The configuration file does not exist");
        if(!configurationFile.canRead()) throw new IllegalAccessException("The configuration file can not be read");

        TemplateConfiguration templateConfiguration = new TemplateConfiguration();

        final JsonObject configuration = JSONHelper.readFromFile(configurationFile);
        final JsonObject templateJson = configuration.getObject("template");

        templateConfiguration.setName(templateJson.getString("name"));
        LOGGER.fine("[Template configuration] name = " + templateConfiguration.getName());

        templateConfiguration.setFile(new File(getWorkingDirectory(), templateJson.getString("file")));
        LOGGER.fine("[Template configuration] file = " + templateConfiguration.getFile().getAbsolutePath());

        templateConfiguration.setJsObject(templateJson.getString("js-object"));
        LOGGER.fine("[Template configuration] jsObject = " + templateConfiguration.getJsObject());

        templateConfiguration.setResourcesDirectory(new File(getWorkingDirectory(), templateJson.getString("resources-directory")));
        LOGGER.fine("[Template configuration] resources-directory = " + templateConfiguration.getResourcesDirectory().getAbsolutePath());

        templateConfiguration.setContentDefinerMethod("slideshowFXSetField");
        LOGGER.fine("[Template configuration] content definer method = " + templateConfiguration.getContentDefinerMethod());

        templateConfiguration.setGetCurrentSlideMethod("slideshowFXGetCurrentSlide");
        LOGGER.fine("[Template configuration] content definer method = " + templateConfiguration.getGetCurrentSlideMethod());

        // Settings the default variables
        templateConfiguration.setDefaultVariables(new HashSet<>());
        JsonArray defaultVariablesJson = templateJson.getArray("default-variables");

        if(defaultVariablesJson != null) {
            LOGGER.fine("Reading default variables");

            defaultVariablesJson.forEach(variable -> {
                final JsonObject variableJson = (JsonObject) variable;
                templateConfiguration.getDefaultVariables().add(
                        new Pair<>(
                                variableJson.getString("name"),
                                new String(Base64.getDecoder().decode(variableJson.getString("value")), StandardCharsets.UTF_8)
                        ));
            });
        }

        // Setting the slides
        templateConfiguration.setSlideTemplates(new ArrayList<SlideTemplate>());
        JsonObject slidesJson = templateJson.getObject("slides");

        if (slidesJson != null) {
            LOGGER.fine("Reading slide's configuration");
            JsonObject slidesConfigurationJson = slidesJson.getObject("configuration");

            templateConfiguration.setSlidesTemplateDirectory(new File(this.getWorkingDirectory(), slidesConfigurationJson.getString("template-directory")));
            LOGGER.fine("[Slide's configuration] templateConfiguration directory = " + templateConfiguration.getSlidesTemplateDirectory().getAbsolutePath());

            templateConfiguration.setSlidesPresentationDirectory(new File(this.getWorkingDirectory(), slidesConfigurationJson.getString("presentation-directory")));
            LOGGER.fine("[Slide's configuration] presentation directory = " + templateConfiguration.getSlidesPresentationDirectory().getAbsolutePath());

            templateConfiguration.setSlidesThumbnailDirectory(new File(this.getWorkingDirectory(), slidesConfigurationJson.getString("thumbnail-directory")));
            LOGGER.fine("[Slide's configuration] slides thumbnail directory = " + templateConfiguration.getSlidesThumbnailDirectory().getAbsolutePath());

            templateConfiguration.setSlideIdPrefix(slidesConfigurationJson.getString("slide-id-prefix"));
            LOGGER.fine("[Slide's configuration] slideIdPrefix = " + templateConfiguration.getSlideIdPrefix());

            templateConfiguration.setSlidesContainer(slidesConfigurationJson.getString("slides-container"));
            LOGGER.fine("[Slide's configuration] slidesContainer = " + templateConfiguration.getSlidesContainer());

            slidesJson.getArray("slides-definition")
                    .forEach(slideJson -> {
                        Number number;

                        final SlideTemplate slideTemplate = new SlideTemplate();
                        slideTemplate.setId((number = ((JsonObject) slideJson).getNumber("id")) != null ? number.intValue() : -1);
                        LOGGER.fine("[Slide definition] id = " + slideTemplate.getId());

                        slideTemplate.setName(((JsonObject) slideJson).getString("name"));
                        LOGGER.fine("[Slide definition] name = " + slideTemplate.getName());

                        slideTemplate.setFile(new File(templateConfiguration.getSlidesTemplateDirectory(), ((JsonObject) slideJson).getString("file")));
                        LOGGER.fine("[Slide definition] file = " + slideTemplate.getFile().getAbsolutePath());

                        /* final JsonArray dynamicIdsJson = ((JsonObject) slideJson).getArray("dynamic-ids");
                        if (dynamicIdsJson != null && dynamicIdsJson.size() > 0) {
                            slideTemplate.setDynamicIds(new String[dynamicIdsJson.size()]);

                            for (int index = 0; index < dynamicIdsJson.size(); index++) {
                                slideTemplate.getDynamicIds()[index] = dynamicIdsJson.get(index);
                            }
                        }*/

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
                                dynamicAttribute.setTemplateExpression(dynamicAttributeJson.getString("templateConfiguration-expression"));

                                slideTemplate.getDynamicAttributes()[index] = dynamicAttribute;
                            }
                        }

                        final JsonArray elementsJson = ((JsonObject) slideJson).getArray("elements");
                        if(elementsJson != null && elementsJson.size() > 0) {
                            slideTemplate.setElements(new SlideElementTemplate[elementsJson.size()]);
                            SlideElementTemplate element;
                            JsonObject elementJson;

                            for (int index = 0; index < elementsJson.size(); index++) {
                                element = new SlideElementTemplate();
                                elementJson = elementsJson.get(index);

                                element.setId(elementJson.getNumber("id").intValue());
                                element.setHtmlId(elementJson.getString("html-id"));
                                element.setDefaultContent(elementJson.getString("default-content"));

                                slideTemplate.getElements()[index] = element;
                            }
                        }

                        templateConfiguration.getSlideTemplates().add(slideTemplate);
                    });
        } else {
            LOGGER.fine("No slide's configuration found");
        }

        return templateConfiguration;
    }

    @Override
    public void writeConfiguration(File configurationFile) throws NullPointerException, IOException {
        if(configurationFile == null) throw new NullPointerException("The configuration to write into can not be null");

        if (this.configuration != null) {

            final JsonObject configurationJson = new JsonObject()
                    .putObject("template", new JsonObject()
                            .putString("name", this.configuration.getName() == null ? "" : this.configuration.getName())
                            .putString("file", this.configuration.getFile() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getFile()))
                            .putString("js-object", this.configuration.getJsObject() == null ? "" : this.configuration.getJsObject())
                            .putString("resources-directory", this.configuration.getResourcesDirectory() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getResourcesDirectory()))
                            .putArray("default-variables", new JsonArray())
                            .putObject("slides", new JsonObject()
                                    .putObject("configuration", new JsonObject()
                                            .putString("slides-container", this.configuration.getSlidesContainer() == null ? "" : this.configuration.getSlidesContainer())
                                            .putString("slide-id-prefix", this.configuration.getSlideIdPrefix() == null ? "" : this.configuration.getSlideIdPrefix())
                                            .putString("template-directory", this.configuration.getSlidesTemplateDirectory() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getSlidesTemplateDirectory()))
                                            .putString("presentation-directory", this.configuration.getSlidesPresentationDirectory() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getSlidesTemplateDirectory()))
                                            .putString("thumbnail-directory", this.configuration.getSlidesThumbnailDirectory() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getSlidesThumbnailDirectory())))
                                    .putArray("slides-definition", new JsonArray())));

            final JsonArray defaultVariablesJson = configurationJson.getObject("template")
                                                                .getArray("default-variables");

            this.configuration.getDefaultVariables()
                    .forEach(variable -> {
                        defaultVariablesJson.addObject(new JsonObject()
                                .putString("name", variable.getKey())
                                .putString("value", Base64.getEncoder().encodeToString(variable.getValue().getBytes(StandardCharsets.UTF_8))));

                    });

            final JsonArray slidesDefinitionJson = configurationJson.getObject("template")
                                                                .getObject("slides")
                                                                .getArray("slides-definition");
            this.configuration.getSlideTemplates()
                    .forEach(slideTemplate -> {
                        final JsonObject jsonObject = new JsonObject()
                                .putNumber("id", slideTemplate.getId())
                                .putString("name", slideTemplate.getName() == null ? "" : slideTemplate.getName())
                                .putString("file", slideTemplate.getFile() == null ? "" : slideTemplate.getFile().getName())
                                .putArray("dynamic-ids", new JsonArray())
                                .putArray("dynamic-attributes", new JsonArray())
                                .putArray("elements", new JsonArray());

                       /* if(slideTemplate.getDynamicIds() != null && slideTemplate.getDynamicIds().length > 0) {
                            final JsonArray array = jsonObject.getArray("dynamic-ids");
                            Arrays.stream(slideTemplate.getDynamicIds())
                                    .forEach(id -> array.addString(id));
                        } */

                        if(slideTemplate.getDynamicAttributes() != null && slideTemplate.getDynamicAttributes().length > 0) {
                            final JsonArray array = jsonObject.getArray("dynamic-attributes");
                            Arrays.stream(slideTemplate.getDynamicAttributes())
                                    .forEach(attribute -> {
                                        array.addObject(new JsonObject()
                                                            .putString("attribute", attribute.getAttribute())
                                                            .putString("template-expression", attribute.getTemplateExpression())
                                                            .putString("prompt-message", attribute.getPromptMessage()));
                                    });
                        }

                        if(slideTemplate.getElements() != null && slideTemplate.getElements().length > 0) {
                            final JsonArray array = jsonObject.getArray("elements");
                            Arrays.stream(slideTemplate.getElements())
                                    .forEach(element -> {
                                        array.addObject(new JsonObject()
                                                            .putNumber("template-id", element.getId())
                                                            .putString("html-id", element.getHtmlId())
                                                            .putString("default-content", element.getDefaultContent()));
                                    });
                        }

                        slidesDefinitionJson.addObject(jsonObject);
                    });

            JSONHelper.writeObject(configurationJson, configurationFile);
        }
    }

    @Override
    public void loadArchive(File file) throws IllegalArgumentException, NullPointerException, IOException, IllegalAccessException {
        if(file == null) throw new NullPointerException("The archive file can not be null");
        if(!file.exists()) throw new FileNotFoundException("The archive file does not exist");
        if(!file.canRead()) throw new IllegalAccessException("The archive file can not be read");
        if(!file.getName().endsWith(this.getArchiveExtension())) throw new IllegalArgumentException("The extension of the archive is not valid");

        this.setArchive(file);
        this.setWorkingDirectory(this.generateWorkingDirectory());
        ZipUtils.unzip(this.getArchive(), this.getWorkingDirectory());
        final TemplateConfiguration configuration = this.readConfiguration();
        this.setConfiguration(configuration);
    }

    @Override
    public synchronized void saveArchive(File file) throws IllegalArgumentException, IOException {
        if(file == null) throw new NullPointerException("The destination archive can not be null");
        if(!file.getName().endsWith(this.getArchiveExtension())) throw new IllegalArgumentException("The file does not have the correct extension");

        this.writeConfiguration();
        ZipUtils.zip(this.getWorkingDirectory(), file);
    }
}

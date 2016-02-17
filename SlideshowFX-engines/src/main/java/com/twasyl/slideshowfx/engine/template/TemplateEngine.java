package com.twasyl.slideshowfx.engine.template;

import com.twasyl.slideshowfx.engine.AbstractEngine;
import com.twasyl.slideshowfx.engine.EngineException;
import com.twasyl.slideshowfx.engine.template.configuration.SlideElementTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration;
import com.twasyl.slideshowfx.utils.JSONHelper;
import com.twasyl.slideshowfx.utils.ZipUtils;
import com.twasyl.slideshowfx.utils.beans.Pair;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;

/**
 * This class is used to managed the templates provided by SlideshowFX.
 * The extension of a template is <code>sfxt</code>.
 *
 * @author Thierry Wasylczenko
 */
public class TemplateEngine extends AbstractEngine<TemplateConfiguration> {
    private static final Logger LOGGER = Logger.getLogger(TemplateEngine.class.getName());

    /**
     * The default extension for template archives. Value is {@value #DEFAULT_ARCHIVE_EXTENSION}.
     */
    public static final String DEFAULT_ARCHIVE_EXTENSION = "sfxt";
    /**
     * The default value, containing the dot, for presentation archives.
     */
    public static final String DEFAULT_DOTTED_ARCHIVE_EXTENSION = ".".concat(DEFAULT_ARCHIVE_EXTENSION);

    public TemplateEngine() {
        super(DEFAULT_ARCHIVE_EXTENSION, "template-config.json");
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
        final JsonObject templateJson = configuration.getJsonObject("template");

        templateConfiguration.setName(templateJson.getString("name"));
        LOGGER.fine("[Template configuration] name = " + templateConfiguration.getName());

        templateConfiguration.setFile(new File(getWorkingDirectory(), templateJson.getString("file")));
        LOGGER.fine("[Template configuration] file = " + templateConfiguration.getFile().getAbsolutePath());

        templateConfiguration.setJsObject(templateJson.getString("js-object"));
        LOGGER.fine("[Template configuration] jsObject = " + templateConfiguration.getJsObject());

        templateConfiguration.setSfxServerObject("sfxServer");
        LOGGER.fine("[Template configuration] server object = " + templateConfiguration.getSfxServerObject());

        templateConfiguration.setResourcesDirectory(new File(getWorkingDirectory(), templateJson.getString("resources-directory")));
        LOGGER.fine("[Template configuration] resources-directory = " + templateConfiguration.getResourcesDirectory().getAbsolutePath());

        templateConfiguration.setContentDefinerMethod("slideshowFXSetField");
        LOGGER.fine("[Template configuration] content definer method = " + templateConfiguration.getContentDefinerMethod());

        templateConfiguration.setGetCurrentSlideMethod("slideshowFXGetCurrentSlide");
        LOGGER.fine("[Template configuration] content definer method = " + templateConfiguration.getGetCurrentSlideMethod());

        templateConfiguration.setGotoSlideMethod("slideshowFXGotoSlide");
        LOGGER.fine("[Template configuration] goto slide method = " + templateConfiguration.getGotoSlideMethod());

        templateConfiguration.setUpdateCodeSnippetConsoleMethod("updateCodeSnippetConsole");
        LOGGER.fine("[Template configuration] update code snippet console method = " + templateConfiguration.getUpdateCodeSnippetConsoleMethod());

        templateConfiguration.setLeapMotionMethod("slideshowFXLeap");
        LOGGER.fine("[Template configuration] content definer method = " + templateConfiguration.getLeapMotionMethod());

        // Settings the default variables
        templateConfiguration.setDefaultVariables(new HashSet<>());
        JsonArray defaultVariablesJson = templateJson.getJsonArray("default-variables");

        if(defaultVariablesJson != null) {
            LOGGER.fine("Reading default variables");

            defaultVariablesJson.forEach(variable -> {
                final JsonObject variableJson = (JsonObject) variable;
                templateConfiguration.getDefaultVariables().add(
                        new Pair<>(
                                variableJson.getString("name"),
                                new String(Base64.getDecoder().decode(variableJson.getString("value")), getDefaultCharset())
                        ));
            });
        }

        // Setting the slides
        templateConfiguration.setSlideTemplates(new ArrayList<SlideTemplate>());
        JsonObject slidesJson = templateJson.getJsonObject("slides");

        if (slidesJson != null) {
            LOGGER.fine("Reading slide's configuration");
            JsonObject slidesConfigurationJson = slidesJson.getJsonObject("configuration");

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

            slidesJson.getJsonArray("slides-definition")
                    .forEach(slideJson -> {
                        Number number;

                        final SlideTemplate slideTemplate = new SlideTemplate();
                        slideTemplate.setId((number = ((JsonObject) slideJson).getInteger("id")) != null ? number.intValue() : -1);
                        LOGGER.fine("[Slide definition] id = " + slideTemplate.getId());

                        slideTemplate.setName(((JsonObject) slideJson).getString("name"));
                        LOGGER.fine("[Slide definition] name = " + slideTemplate.getName());

                        slideTemplate.setFile(new File(templateConfiguration.getSlidesTemplateDirectory(), ((JsonObject) slideJson).getString("file")));
                        LOGGER.fine("[Slide definition] file = " + slideTemplate.getFile().getAbsolutePath());

                        /* final JsonArray dynamicIdsJson = ((JsonObject) slideJson).getJsonArray("dynamic-ids");
                        if (dynamicIdsJson != null && dynamicIdsJson.size() > 0) {
                            slideTemplate.setDynamicIds(new String[dynamicIdsJson.size()]);

                            for (int index = 0; index < dynamicIdsJson.size(); index++) {
                                slideTemplate.getDynamicIds()[index] = dynamicIdsJson.get(index);
                            }
                        }*/

                        final JsonArray dynamicAttributesJson = ((JsonObject) slideJson).getJsonArray("dynamic-attributes");
                        if (dynamicAttributesJson != null && dynamicAttributesJson.size() > 0) {
                            slideTemplate.setDynamicAttributes(new DynamicAttribute[dynamicAttributesJson.size()]);
                            DynamicAttribute dynamicAttribute;
                            JsonObject dynamicAttributeJson;

                            for (int index = 0; index < dynamicAttributesJson.size(); index++) {
                                dynamicAttribute = new DynamicAttribute();
                                dynamicAttributeJson = dynamicAttributesJson.getJsonObject(index);

                                dynamicAttribute.setAttribute(dynamicAttributeJson.getString("attribute"));
                                dynamicAttribute.setPromptMessage(dynamicAttributeJson.getString("prompt-message"));
                                dynamicAttribute.setTemplateExpression(dynamicAttributeJson.getString("templateConfiguration-expression"));

                                slideTemplate.getDynamicAttributes()[index] = dynamicAttribute;
                            }
                        }

                        final JsonArray elementsJson = ((JsonObject) slideJson).getJsonArray("elements");
                        if(elementsJson != null && elementsJson.size() > 0) {
                            slideTemplate.setElements(new SlideElementTemplate[elementsJson.size()]);
                            SlideElementTemplate element;
                            JsonObject elementJson;

                            for (int index = 0; index < elementsJson.size(); index++) {
                                element = new SlideElementTemplate();
                                elementJson = elementsJson.getJsonObject(index);

                                element.setId(elementJson.getInteger("id").intValue());
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
                    .put("template", new JsonObject()
                            .put("name", this.configuration.getName() == null ? "" : this.configuration.getName())
                            .put("file", this.configuration.getFile() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getFile()))
                            .put("js-object", this.configuration.getJsObject() == null ? "" : this.configuration.getJsObject())
                            .put("resources-directory", this.configuration.getResourcesDirectory() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getResourcesDirectory()))
                            .put("default-variables", new JsonArray())
                            .put("slides", new JsonObject()
                                    .put("configuration", new JsonObject()
                                            .put("slides-container", this.configuration.getSlidesContainer() == null ? "" : this.configuration.getSlidesContainer())
                                            .put("slide-id-prefix", this.configuration.getSlideIdPrefix() == null ? "" : this.configuration.getSlideIdPrefix())
                                            .put("template-directory", this.configuration.getSlidesTemplateDirectory() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getSlidesTemplateDirectory()))
                                            .put("presentation-directory", this.configuration.getSlidesPresentationDirectory() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getSlidesTemplateDirectory()))
                                            .put("thumbnail-directory", this.configuration.getSlidesThumbnailDirectory() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getSlidesThumbnailDirectory())))
                                    .put("slides-definition", new JsonArray())));

            final JsonArray defaultVariablesJson = configurationJson.getJsonObject("template")
                                                                .getJsonArray("default-variables");

            this.configuration.getDefaultVariables()
                    .forEach(variable -> {
                        defaultVariablesJson.add(new JsonObject()
                                .put("name", variable.getKey())
                                .put("value", Base64.getEncoder().encodeToString(variable.getValue().getBytes(getDefaultCharset()))));

                    });

            final JsonArray slidesDefinitionJson = configurationJson.getJsonObject("template")
                                                                .getJsonObject("slides")
                                                                .getJsonArray("slides-definition");
            this.configuration.getSlideTemplates()
                    .forEach(slideTemplate -> {
                        final JsonObject jsonObject = new JsonObject()
                                .put("id", slideTemplate.getId())
                                .put("name", slideTemplate.getName() == null ? "" : slideTemplate.getName())
                                .put("file", slideTemplate.getFile() == null ? "" : slideTemplate.getFile().getName())
                                .put("dynamic-ids", new JsonArray())
                                .put("dynamic-attributes", new JsonArray())
                                .put("elements", new JsonArray());

                       /* if(slideTemplate.getDynamicIds() != null && slideTemplate.getDynamicIds().length > 0) {
                            final JsonArray array = jsonObject.getJsonArray("dynamic-ids");
                            Arrays.stream(slideTemplate.getDynamicIds())
                                    .forEach(id -> array.addString(id));
                        } */

                        if(slideTemplate.getDynamicAttributes() != null && slideTemplate.getDynamicAttributes().length > 0) {
                            final JsonArray array = jsonObject.getJsonArray("dynamic-attributes");
                            Arrays.stream(slideTemplate.getDynamicAttributes())
                                    .forEach(attribute -> {
                                        array.add(new JsonObject()
                                                            .put("attribute", attribute.getAttribute())
                                                            .put("template-expression", attribute.getTemplateExpression())
                                                            .put("prompt-message", attribute.getPromptMessage()));
                                    });
                        }

                        if(slideTemplate.getElements() != null && slideTemplate.getElements().length > 0) {
                            final JsonArray array = jsonObject.getJsonArray("elements");
                            Arrays.stream(slideTemplate.getElements())
                                    .forEach(element -> {
                                        array.add(new JsonObject()
                                                            .put("template-id", element.getId())
                                                            .put("html-id", element.getHtmlId())
                                                            .put("default-content", element.getDefaultContent()));
                                    });
                        }

                        slidesDefinitionJson.add(jsonObject);
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

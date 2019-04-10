package com.twasyl.slideshowfx.engine.template;

import com.twasyl.slideshowfx.engine.AbstractEngine;
import com.twasyl.slideshowfx.engine.template.configuration.SlideElementTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration;
import com.twasyl.slideshowfx.utils.JSONHelper;
import com.twasyl.slideshowfx.utils.ZipUtils;
import com.twasyl.slideshowfx.utils.beans.Pair;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.engine.template.configuration.TemplateConfigurationFields.*;
import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;

/**
 * This class is used to managed the templates provided by SlideshowFX.
 * The extension of a template is {@code sfxt}.
 *
 * @author Thierry Wasylczenko
 * @version 1.3-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class TemplateEngine extends AbstractEngine<TemplateConfiguration> {
    private static final Logger LOGGER = Logger.getLogger(TemplateEngine.class.getName());

    /**
     * The default extension for template archives. Value is {@value #DEFAULT_ARCHIVE_EXTENSION}.th
     */
    public static final String DEFAULT_ARCHIVE_EXTENSION = "sfxt";

    /**
     * The default file name of the configuration file.
     */
    public static final String DEFAULT_CONFIGURATION_FILE_NAME = "template-config.json";

    /**
     * The default value, containing the dot, for presentation archives.
     */
    public static final String DEFAULT_DOTTED_ARCHIVE_EXTENSION = ".".concat(DEFAULT_ARCHIVE_EXTENSION);

    public TemplateEngine() {
        super(DEFAULT_ARCHIVE_EXTENSION, DEFAULT_CONFIGURATION_FILE_NAME);
    }

    @Override
    public TemplateConfiguration readConfiguration(Reader reader) throws IOException {
        if (reader == null) throw new NullPointerException("The configuration reader can not be null");

        TemplateConfiguration templateConfiguration = new TemplateConfiguration();

        final JsonObject configuration = JSONHelper.readFromReader(reader);
        final JsonObject templateJson = configuration.getJsonObject(TEMPLATE.getFieldName());

        templateConfiguration.setName(templateJson.getString(TEMPLATE_NAME.getFieldName()));
        LOGGER.fine("[Template configuration] name = " + templateConfiguration.getName());

        templateConfiguration.setVersion(templateJson.getString(TEMPLATE_VERSION.getFieldName(), null));
        LOGGER.fine("[Template configuration] version = " + templateConfiguration.getVersion());

        templateConfiguration.setFile(new File(getWorkingDirectory(), templateJson.getString(TEMPLATE_FILE.getFieldName())));
        LOGGER.fine("[Template configuration] file = " + templateConfiguration.getFile().getAbsolutePath());

        templateConfiguration.setJsObject(templateJson.getString(JS_OBJECT.getFieldName()));
        LOGGER.fine("[Template configuration] jsObject = " + templateConfiguration.getJsObject());

        templateConfiguration.setSfxServerObject("sfxServer");
        LOGGER.fine("[Template configuration] server object = " + templateConfiguration.getSfxServerObject());

        templateConfiguration.setResourcesDirectory(new File(getWorkingDirectory(), templateJson.getString(TEMPLATE_RESOURCES_DIRECTORY.getFieldName())));
        LOGGER.fine("[Template configuration] resources-directory = " + templateConfiguration.getResourcesDirectory().getAbsolutePath());

        templateConfiguration.setContentDefinerMethod("slideshowFXSetField");
        LOGGER.fine("[Template configuration] content definer method = " + templateConfiguration.getContentDefinerMethod());

        templateConfiguration.setGetCurrentSlideMethod("slideshowFXGetCurrentSlide");
        LOGGER.fine("[Template configuration] content definer method = " + templateConfiguration.getGetCurrentSlideMethod());

        templateConfiguration.setGotoSlideMethod("slideshowFXGotoSlide");
        LOGGER.fine("[Template configuration] goto slide method = " + templateConfiguration.getGotoSlideMethod());

        templateConfiguration.setUpdateCodeSnippetConsoleMethod("updateCodeSnippetConsole");
        LOGGER.fine("[Template configuration] update code snippet console method = " + templateConfiguration.getUpdateCodeSnippetConsoleMethod());

        // Settings the default variables
        templateConfiguration.setDefaultVariables(new HashSet<>());
        JsonArray defaultVariablesJson = templateJson.getJsonArray(TEMPLATE_DEFAULT_VARIABLES.getFieldName());

        if (defaultVariablesJson != null) {
            LOGGER.fine("Reading default variables");

            defaultVariablesJson.forEach(variable -> {
                final JsonObject variableJson = (JsonObject) variable;
                templateConfiguration.getDefaultVariables().add(
                        new Pair<>(
                                variableJson.getString(TEMPLATE_DEFAULT_VARIABLE_NAME.getFieldName()),
                                new String(Base64.getDecoder().decode(variableJson.getString(TEMPLATE_DEFAULT_VARIABLE_VALUE.getFieldName())), getDefaultCharset())
                        ));
            });
        }

        // Setting the slides
        templateConfiguration.setSlideTemplates(new ArrayList<>());
        JsonObject slidesJson = templateJson.getJsonObject(SLIDES.getFieldName());

        if (slidesJson != null) {
            LOGGER.fine("Reading slide's configuration");
            JsonObject slidesConfigurationJson = slidesJson.getJsonObject(SLIDES_CONFIGURATION.getFieldName());

            templateConfiguration.setSlidesTemplateDirectory(new File(this.getWorkingDirectory(), slidesConfigurationJson.getString(SLIDES_TEMPLATE_DIRECTORY.getFieldName())));
            LOGGER.fine("[Slide's configuration] templateConfiguration directory = " + templateConfiguration.getSlidesTemplateDirectory().getAbsolutePath());

            templateConfiguration.setSlidesPresentationDirectory(new File(this.getWorkingDirectory(), slidesConfigurationJson.getString(SLIDES_PRESENTATION_DIRECTORY.getFieldName())));
            LOGGER.fine("[Slide's configuration] presentation directory = " + templateConfiguration.getSlidesPresentationDirectory().getAbsolutePath());

            templateConfiguration.setSlideIdPrefix(slidesConfigurationJson.getString(SLIDE_ID_PREFIX.getFieldName()));
            LOGGER.fine("[Slide's configuration] slideIdPrefix = " + templateConfiguration.getSlideIdPrefix());

            templateConfiguration.setSlidesContainer(slidesConfigurationJson.getString(SLIDES_CONTAINER.getFieldName()));
            LOGGER.fine("[Slide's configuration] slidesContainer = " + templateConfiguration.getSlidesContainer());

            slidesJson.getJsonArray(SLIDES_DEFINITION.getFieldName())
                    .forEach(slideJson -> {
                        Number number;

                        final SlideTemplate slideTemplate = new SlideTemplate();
                        slideTemplate.setId(((number = ((JsonObject) slideJson).getInteger(SLIDE_ID.getFieldName())) != null) ? number.intValue() : -1);
                        LOGGER.fine("[Slide definition] id = " + slideTemplate.getId());

                        slideTemplate.setName(((JsonObject) slideJson).getString(SLIDE_NAME.getFieldName()));
                        LOGGER.fine("[Slide definition] name = " + slideTemplate.getName());

                        slideTemplate.setFile(new File(templateConfiguration.getSlidesTemplateDirectory(), ((JsonObject) slideJson).getString(SLIDE_FILE.getFieldName())));
                        LOGGER.fine("[Slide definition] file = " + slideTemplate.getFile().getAbsolutePath());

                        /* final JsonArray dynamicIdsJson = ((JsonObject) slideJson).getJsonArray("dynamic-ids");
                        if (dynamicIdsJson != null && dynamicIdsJson.size() > 0) {
                            slideTemplate.setDynamicIds(new String[dynamicIdsJson.size()]);

                            for (int index = 0; index < dynamicIdsJson.size(); index++) {
                                slideTemplate.getDynamicIds()[index] = dynamicIdsJson.get(index);
                            }
                        }*/

                        final JsonArray dynamicAttributesJson = ((JsonObject) slideJson).getJsonArray(SLIDE_DYNAMIC_ATTRIBUTES.getFieldName());
                        if (dynamicAttributesJson != null && dynamicAttributesJson.size() > 0) {
                            slideTemplate.setDynamicAttributes(new DynamicAttribute[dynamicAttributesJson.size()]);
                            DynamicAttribute dynamicAttribute;
                            JsonObject dynamicAttributeJson;

                            for (int index = 0; index < dynamicAttributesJson.size(); index++) {
                                dynamicAttribute = new DynamicAttribute();
                                dynamicAttributeJson = dynamicAttributesJson.getJsonObject(index);

                                dynamicAttribute.setAttribute(dynamicAttributeJson.getString(DYNAMIC_ATTRIBUTE.getFieldName()));
                                dynamicAttribute.setPromptMessage(dynamicAttributeJson.getString(DYNAMIC_ATTRIBUTE_PROMPT_MESSAGE.getFieldName()));
                                dynamicAttribute.setTemplateExpression(dynamicAttributeJson.getString(DYNAMIC_ATTRIBUTE_TEMPLATE_EXPRESSION.getFieldName()));

                                slideTemplate.getDynamicAttributes()[index] = dynamicAttribute;
                            }
                        }

                        final JsonArray elementsJson = ((JsonObject) slideJson).getJsonArray(SLIDE_ELEMENTS.getFieldName());
                        if (elementsJson != null && elementsJson.size() > 0) {
                            slideTemplate.setElements(new SlideElementTemplate[elementsJson.size()]);
                            SlideElementTemplate element;
                            JsonObject elementJson;

                            for (int index = 0; index < elementsJson.size(); index++) {
                                element = new SlideElementTemplate();
                                elementJson = elementsJson.getJsonObject(index);

                                element.setId(elementJson.getInteger(SLIDE_ELEMENT_ID.getFieldName()).intValue());
                                element.setHtmlId(elementJson.getString(SLIDE_ELEMENT_HTML_ID.getFieldName()));
                                element.setDefaultContent(elementJson.getString(SLIDE_ELEMENT_DEFAULT_CONTENT.getFieldName()));

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
    public void writeConfiguration(final Writer writer) throws IOException {
        if (writer == null) throw new NullPointerException("The configuration to write into can not be null");

        if (this.configuration != null) {

            final JsonObject configurationJson = new JsonObject()
                    .put(TEMPLATE.getFieldName(), new JsonObject()
                            .put(TEMPLATE_NAME.getFieldName(), this.configuration.getName() == null ? "" : this.configuration.getName())
                            .put(TEMPLATE_FILE.getFieldName(), this.configuration.getFile() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getFile()))
                            .put(JS_OBJECT.getFieldName(), this.configuration.getJsObject() == null ? "" : this.configuration.getJsObject())
                            .put(TEMPLATE_RESOURCES_DIRECTORY.getFieldName(), this.configuration.getResourcesDirectory() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getResourcesDirectory()))
                            .put(TEMPLATE_DEFAULT_VARIABLES.getFieldName(), new JsonArray())
                            .put(SLIDES.getFieldName(), new JsonObject()
                                    .put(SLIDES_CONFIGURATION.getFieldName(), new JsonObject()
                                            .put(SLIDES_CONTAINER.getFieldName(), this.configuration.getSlidesContainer() == null ? "" : this.configuration.getSlidesContainer())
                                            .put(SLIDE_ID_PREFIX.getFieldName(), this.configuration.getSlideIdPrefix() == null ? "" : this.configuration.getSlideIdPrefix())
                                            .put(SLIDES_TEMPLATE_DIRECTORY.getFieldName(), this.configuration.getSlidesTemplateDirectory() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getSlidesTemplateDirectory()))
                                            .put(SLIDES_PRESENTATION_DIRECTORY.getFieldName(), this.configuration.getSlidesPresentationDirectory() == null ? "" : this.relativizeFromWorkingDirectory(this.configuration.getSlidesTemplateDirectory())))
                                    .put(SLIDES_DEFINITION.getFieldName(), new JsonArray())));

            final JsonArray defaultVariablesJson = configurationJson.getJsonObject(TEMPLATE.getFieldName())
                    .getJsonArray(TEMPLATE_DEFAULT_VARIABLES.getFieldName());

            this.configuration.getDefaultVariables()
                    .forEach(variable -> defaultVariablesJson.add(new JsonObject()
                            .put(TEMPLATE_DEFAULT_VARIABLE_NAME.getFieldName(), variable.getKey())
                            .put(TEMPLATE_DEFAULT_VARIABLE_VALUE.getFieldName(), Base64.getEncoder().encodeToString(variable.getValue().getBytes(getDefaultCharset())))));

            final JsonArray slidesDefinitionJson = configurationJson.getJsonObject(TEMPLATE.getFieldName())
                    .getJsonObject(SLIDES.getFieldName())
                    .getJsonArray(SLIDES_DEFINITION.getFieldName());
            this.configuration.getSlideTemplates()
                    .forEach(slideTemplate -> {
                        final JsonObject jsonObject = new JsonObject()
                                .put(SLIDE_ID.getFieldName(), slideTemplate.getId())
                                .put(SLIDE_NAME.getFieldName(), slideTemplate.getName() == null ? "" : slideTemplate.getName())
                                .put(SLIDE_FILE.getFieldName(), slideTemplate.getFile() == null ? "" : slideTemplate.getFile().getName())
                                .put(SLIDE_DYNAMIC_IDS.getFieldName(), new JsonArray())
                                .put(SLIDE_DYNAMIC_ATTRIBUTES.getFieldName(), new JsonArray())
                                .put(SLIDE_ELEMENTS.getFieldName(), new JsonArray());

                       /* if(slideTemplate.getDynamicIds() != null && slideTemplate.getDynamicIds().length > 0) {
                            final JsonArray array = jsonObject.getJsonArray("dynamic-ids");
                            Arrays.stream(slideTemplate.getDynamicIds())
                                    .forEach(id -> array.addString(id));
                        } */

                        if (slideTemplate.getDynamicAttributes() != null && slideTemplate.getDynamicAttributes().length > 0) {
                            final JsonArray array = jsonObject.getJsonArray(SLIDE_DYNAMIC_ATTRIBUTES.getFieldName());
                            Arrays.stream(slideTemplate.getDynamicAttributes())
                                    .forEach(attribute -> array.add(new JsonObject()
                                            .put(DYNAMIC_ATTRIBUTE.getFieldName(), attribute.getAttribute())
                                            .put(DYNAMIC_ATTRIBUTE_TEMPLATE_EXPRESSION.getFieldName(), attribute.getTemplateExpression())
                                            .put(DYNAMIC_ATTRIBUTE_PROMPT_MESSAGE.getFieldName(), attribute.getPromptMessage())));
                        }

                        if (slideTemplate.getElements() != null && slideTemplate.getElements().length > 0) {
                            final JsonArray array = jsonObject.getJsonArray(SLIDE_ELEMENTS.getFieldName());
                            Arrays.stream(slideTemplate.getElements())
                                    .forEach(element -> array.add(new JsonObject()
                                            .put(SLIDE_ELEMENT_ID.getFieldName(), element.getId())
                                            .put(SLIDE_ELEMENT_HTML_ID.getFieldName(), element.getHtmlId())
                                            .put(SLIDE_ELEMENT_DEFAULT_CONTENT.getFieldName(), element.getDefaultContent())));
                        }

                        slidesDefinitionJson.add(jsonObject);
                    });

            JSONHelper.writeObject(configurationJson, writer);
        }
    }

    @Override
    public void loadArchive(File file) throws IOException, IllegalAccessException {
        if (file == null) throw new NullPointerException("The archive file can not be null");
        if (!file.exists()) throw new FileNotFoundException("The archive file does not exist");
        if (!file.canRead()) throw new IllegalAccessException("The archive file can not be read");
        if (!file.getName().endsWith(this.getArchiveExtension()))
            throw new IllegalArgumentException("The extension of the archive is not valid");

        this.setArchive(file);
        this.setWorkingDirectory(this.generateWorkingDirectory());
        ZipUtils.unzip(this.getArchive(), this.getWorkingDirectory());
        final TemplateConfiguration configuration = this.readConfiguration();
        this.setConfiguration(configuration);
    }

    @Override
    public synchronized void saveArchive(File file) throws IOException {
        if (file == null) throw new NullPointerException("The destination archive can not be null");
        if (!file.getName().endsWith(this.getArchiveExtension()))
            throw new IllegalArgumentException("The file does not have the correct extension");

        ZipUtils.zip(this.getWorkingDirectory(), file);
    }
}

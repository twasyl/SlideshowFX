package com.twasyl.slideshowfx.engine.presentation;

import com.twasyl.slideshowfx.content.extension.Resource;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.engine.AbstractEngine;
import com.twasyl.slideshowfx.engine.presentation.configuration.PresentationConfiguration;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import com.twasyl.slideshowfx.engine.presentation.configuration.SlideElement;
import com.twasyl.slideshowfx.engine.template.DynamicAttribute;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.utils.*;
import com.twasyl.slideshowfx.utils.beans.Pair;
import com.twasyl.slideshowfx.utils.io.IOUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.twasyl.slideshowfx.engine.presentation.configuration.PresentationConfigurationFields.*;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * This class manages presentations operation done with SlideshowFX. It is used to open them as well as add, update an
 * delete slides.
 * The extension of a presentation is {@code sfx}.
 *
 * @author Thierry Wasylczenko
 * @version 1.5-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class PresentationEngine extends AbstractEngine<PresentationConfiguration> {

    private static final Logger LOGGER = Logger.getLogger(PresentationEngine.class.getName());

    /**
     * The default extension for presentation archives. Value is {@value #DEFAULT_ARCHIVE_EXTENSION}.
     */
    public static final String DEFAULT_ARCHIVE_EXTENSION = "sfx";

    /**
     * The default value, containing the dot, for presentation archives.
     */
    public static final String DEFAULT_DOTTED_ARCHIVE_EXTENSION = ".".concat(DEFAULT_ARCHIVE_EXTENSION);

    /**
     * The default file name of the configuration file.
     */
    public static final String DEFAULT_CONFIGURATION_FILE_NAME = "presentation-config.json";

    private static final String TEMPLATE_SLIDE_NUMBER_TOKEN = "slideNumber";
    private static final String TEMPLATE_SFX_JAVASCRIPT_RESOURCES_TOKEN = "sfxJavascriptResources";
    private static final String TEMPLATE_SFX_CALLBACK_TOKEN = "sfxCallback";
    private static final String TEMPLATE_SLIDE_ID_PREFIX_TOKEN = "slideIdPrefix";

    private static final String TEMPLATE_SFX_CONTENT_DEFINER_SCRIPT = "/com/twasyl/slideshowfx/engine/js/setField.js";
    private static final String TEMPLATE_SFX_SNIPPET_EXECUTOR_SCRIPT = "/com/twasyl/slideshowfx/engine/js/snippetExecutor.js";
    private static final String TEMPLATE_SFX_CALLBACK_SCRIPT = "/com/twasyl/slideshowfx/engine/js/sendInformationToSlideshowFX.js";
    private static final String TEMPLATE_SFX_QUIZ_CALLER_SCRIPT = "/com/twasyl/slideshowfx/engine/js/quizCaller.js";

    private static final String TEMPLATE_SFX_CALLBACK_CALL = "sendInformationToSlideshowFX(this);";

    private TemplateEngine templateEngine;

    private boolean modifiedSinceLatestSave;

    public PresentationEngine() {
        super(DEFAULT_ARCHIVE_EXTENSION, DEFAULT_CONFIGURATION_FILE_NAME);
        this.templateEngine = new TemplateEngine();

        Presentations.register(this);
    }

    @Override
    public PresentationConfiguration readConfiguration(Reader reader) throws IOException {
        if (reader == null) throw new NullPointerException("The configuration reader can not be null");

        final PresentationConfiguration presentationConfiguration = new PresentationConfiguration();
        presentationConfiguration.setPresentationFile(new File(this.getWorkingDirectory(), DEFAULT_PRESENTATION_FILENAME.getFieldName()));

        JsonObject configurationJson = JSONHelper.readFromReader(reader);
        JsonObject presentationJson = configurationJson.getJsonObject(PRESENTATION.getFieldName());

        presentationConfiguration.setId(presentationJson.getLong(PRESENTATION_ID.getFieldName(), System.currentTimeMillis()));

        if (presentationJson.getJsonArray(PRESENTATION_CUSTOM_RESOURCES.getFieldName()) != null) {
            presentationJson.getJsonArray(PRESENTATION_CUSTOM_RESOURCES.getFieldName())
                    .stream()
                    .map(resource -> (JsonObject) resource)
                    .forEach(customResource -> {
                        final Resource resource = new Resource(
                                ResourceType.valueOf(customResource.getString(CUSTOM_RESOURCE_TYPE.getFieldName())),
                                new String(Base64.getDecoder().decode(customResource.getString(CUSTOM_RESOURCE_CONTENT.getFieldName())))
                        );

                        presentationConfiguration.getCustomResources().add(resource);
                    });
        }

        if (presentationJson.getJsonArray(PRESENTATION_VARIABLES.getFieldName()) != null) {
            presentationJson.getJsonArray(PRESENTATION_VARIABLES.getFieldName())
                    .stream()
                    .map(variable -> (JsonObject) variable)
                    .forEach(variableJson -> {
                        final Pair<String, String> variable = new Pair<>();
                        variable.setKey(variableJson.getString(VARIABLE_NAME.getFieldName()));
                        variable.setValue(new String(Base64.getDecoder().decode(variableJson.getString(VARIABLE_VALUE.getFieldName()))));
                        presentationConfiguration.getVariables().add(variable);
                    });
        }

        presentationJson.getJsonArray(SLIDES.getFieldName())
                .stream()
                .map(slide -> (JsonObject) slide)
                .forEach(slideJson -> {
                    final Slide slide = new Slide();

                    slide.setId(slideJson.getString(SLIDE_ID.getFieldName()));
                    slide.setSlideNumber(slideJson.getString(SLIDE_NUMBER.getFieldName()));
                    slide.setTemplate(this.templateEngine.getConfiguration().getSlideTemplate(slideJson.getInteger(
                            SLIDE_TEMPLATE_ID.getFieldName())));

                    final String speakerNotes = slideJson.getString(SLIDE_SPEAKER_NOTES.getFieldName());
                    if (speakerNotes != null) {
                        slide.setSpeakerNotesAsBase64(speakerNotes);
                    }

                    slideJson.getJsonArray(SLIDE_ELEMENTS.getFieldName())
                            .stream()
                            .map(element -> (JsonObject) element)
                            .forEach(slideElementJson -> {
                                final SlideElement slideElement = new SlideElement();
                                slideElement.setTemplate(slide.getTemplate().getSlideElementTemplate(slideElementJson.getInteger(
                                        SLIDE_ELEMENT_TEMPLATE_ID.getFieldName())));
                                slideElement.setId(slideElementJson.getString(SLIDE_ELEMENT_ELEMENT_ID.getFieldName()));
                                slideElement.setOriginalContentCode(slideElementJson.getString(SLIDE_ELEMENT_ORIGINAL_CONTENT_CODE.getFieldName()));
                                slideElement.setOriginalContentAsBase64(slideElementJson.getString(SLIDE_ELEMENT_ORIGINAL_CONTENT.getFieldName()));
                                slideElement.setHtmlContentAsBase64(slideElementJson.getString(SLIDE_ELEMENT_HTML_CONTENT.getFieldName()));

                                slide.getElements().add(slideElement);
                            });

                    presentationConfiguration.getSlides().add(slide);
                });

        return presentationConfiguration;
    }

    @Override
    public void writeConfiguration(Writer writer) throws IOException {
        if (writer == null) throw new NullPointerException("The configuration to write into can not be null");

        if (this.configuration != null) {
            final JsonObject presentationJson = new JsonObject();
            final JsonArray slidesJson = new JsonArray();
            final JsonArray customResourcesJson = new JsonArray();
            final JsonArray variablesJson = new JsonArray();

            presentationJson.put(PRESENTATION_ID.getFieldName(), this.configuration.getId());

            this.configuration.getCustomResources()
                    .forEach(resource -> {
                        final JsonObject resourceJson = new JsonObject()
                                .put(CUSTOM_RESOURCE_TYPE.getFieldName(), resource.getType().name())
                                .put(CUSTOM_RESOURCE_CONTENT.getFieldName(), Base64.getEncoder().encodeToString(resource.getContent().getBytes()));

                        customResourcesJson.add(resourceJson);
                    });

            this.configuration.getVariables()
                    .forEach(variable -> {
                        final JsonObject variableJson = new JsonObject()
                                .put(VARIABLE_NAME.getFieldName(), variable.getKey())
                                .put(VARIABLE_VALUE.getFieldName(), Base64.getEncoder().encodeToString(variable.getValue().getBytes()));

                        variablesJson.add(variableJson);
                    });

            this.configuration.getSlides()
                .forEach(slide -> {
                    final JsonArray elementsJson = new JsonArray();
                    final JsonObject slideJson = new JsonObject();

                    slideJson.put(SLIDE_TEMPLATE_ID.getFieldName(), slide.getTemplate().getId())
                            .put(SLIDE_ID.getFieldName(), slide.getId())
                            .put(SLIDE_NUMBER.getFieldName(), slide.getSlideNumber());

                    if (slide.hasSpeakerNotes()) {
                        slideJson.put(SLIDE_SPEAKER_NOTES.getFieldName(), slide.getSpeakerNotesAsBase64());
                    }

                    slide.getElements()
                        .forEach(slideElement -> {
                            final JsonObject elementJson = new JsonObject();
                            elementJson.put(SLIDE_ELEMENT_TEMPLATE_ID.getFieldName(), slideElement.getTemplate().getId())
                                    .put(SLIDE_ELEMENT_ELEMENT_ID.getFieldName(), slideElement.getId())
                                    .put(SLIDE_ELEMENT_ORIGINAL_CONTENT_CODE.getFieldName(), slideElement.getOriginalContentCode())
                                    .put(SLIDE_ELEMENT_ORIGINAL_CONTENT.getFieldName(), slideElement.getOriginalContentAsBase64())
                                    .put(SLIDE_ELEMENT_HTML_CONTENT.getFieldName(), slideElement.getHtmlContentAsBase64());

                            elementsJson.add(elementJson);
                        });

                    slideJson.put(SLIDE_ELEMENTS.getFieldName(), elementsJson);
                    slidesJson.add(slideJson);
                });

            presentationJson.put(PRESENTATION_CUSTOM_RESOURCES.getFieldName(), customResourcesJson);
            presentationJson.put(PRESENTATION_VARIABLES.getFieldName(), variablesJson);
            presentationJson.put(SLIDES.getFieldName(), slidesJson);

            final JsonObject finalObject = new JsonObject();
            finalObject.put(PRESENTATION.getFieldName(), presentationJson);

            JSONHelper.writeObject(finalObject, writer);
        }
    }

    @Override
    public void loadArchive(File file) throws IOException, IllegalAccessException {
        if (file == null) throw new NullPointerException("The archive file can not be null");
        if (!file.exists()) throw new FileNotFoundException("The archive file does not exist");
        if (!file.canRead()) throw new IllegalAccessException("The archive file can not be read");
        if (!file.getName().endsWith(this.getArchiveExtension()))
            throw new IllegalArgumentException("The extension of the archive is not valid");

        this.setModifiedSinceLatestSave(false);

        this.setArchive(file);
        this.setWorkingDirectory(this.generateWorkingDirectory());
        ZipUtils.unzip(this.getArchive(), this.getWorkingDirectory());

        // The template configuration has to be read and set
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setWorkingDirectory(this.getWorkingDirectory());
        this.templateEngine.setConfiguration(this.templateEngine.readConfiguration());

        // Configure the PresentationConfiguration
        final PresentationConfiguration configuration = this.readConfiguration();
        configuration.getVariables().addAll(this.getTemplateConfiguration().getDefaultVariables()
                .stream()
                .filter(defVariable -> !configuration.getVariables().contains(defVariable))
                .collect(Collectors.toList()));

        this.setConfiguration(configuration);

        final Configuration templateConfiguration = TemplateProcessor.getDefaultConfiguration();
        templateConfiguration.setDirectoryForTemplateLoading(this.templateEngine.getConfiguration().getFile().getParentFile());

        final Map tokens = new HashMap<>();
        tokens.put(TEMPLATE_SFX_JAVASCRIPT_RESOURCES_TOKEN, this.buildJavaScriptResourcesToInclude());

        // Replacing the template tokens
        try (final StringWriter writer = new StringWriter()) {

            final Template documentTemplate = templateConfiguration.getTemplate(this.templateEngine.getConfiguration().getFile().getName());
            documentTemplate.process(tokens, writer);
            writer.flush();

            this.configuration.setDocument(Jsoup.parse(writer.toString()));

            this.savePresentationFile();
        } catch (TemplateException e) {
            LOGGER.log(SEVERE, "Can not parse template", e);
        }

        LOGGER.fine("Building presentation file");
        // Append the custom resources
        this.configuration.getCustomResources()
                .stream()
                .forEach(this::addCustomResource);

        // Append the slides' content to the presentation
        tokens.clear();
        tokens.put(TEMPLATE_SFX_CALLBACK_TOKEN, TEMPLATE_SFX_CALLBACK_CALL);
        tokens.put(TEMPLATE_SLIDE_ID_PREFIX_TOKEN, this.templateEngine.getConfiguration().getSlideIdPrefix());
        tokens.putAll(this.configuration.getVariables().stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue)));

        for (Slide s : this.configuration.getSlides()) {
            templateConfiguration.setDirectoryForTemplateLoading(s.getTemplate().getFile().getParentFile());

            try (final StringWriter writer = new StringWriter()) {
                tokens.put(TEMPLATE_SLIDE_NUMBER_TOKEN, s.getSlideNumber());

                final Template slideTemplate = templateConfiguration.getTemplate(s.getTemplate().getFile().getName());
                slideTemplate.process(tokens, writer);
                writer.flush();

                this.configuration.getDocument()
                        .getElementById(this.templateEngine.getConfiguration().getSlidesContainer())
                        .append(writer.toString());

                s.getElements()
                        .stream()
                        .forEach(element -> this.configuration.getDocument()
                                .getElementById(element.getId())
                                .html(element.getClearedHtmlContent(this.getConfiguration().getVariables())));
            } catch (IOException | TemplateException e) {
                LOGGER.log(SEVERE, "Can not read slide's template", e);
            }
        }

        this.savePresentationFile();
    }

    @Override
    public synchronized void saveArchive(File file) throws IOException {

        this.writeConfiguration();

        ZipUtils.zip(this.getWorkingDirectory(), file);

        this.setModifiedSinceLatestSave(false);
        LOGGER.fine("Presentation saved");
    }

    /**
     * Indicates if the presentation has already been saved by testing if the {@link #getArchive()}
     * method returns {@code null} or not.
     *
     * @return {@code true} if {@link #getArchive()} is not {@code null}, {@code false} otherwise.
     */
    public boolean isPresentationAlreadySaved() {
        return this.getArchive() != null;
    }

    /**
     * Indicates if the presentation has been modified since the latest save. If the presentation has never been saved,
     * then the presentation is considered modified.
     *
     * @return {@code true} if the presentation has been modified since the latest save, {@code false} otherwise.
     */
    public boolean isModifiedSinceLatestSave() {
        return modifiedSinceLatestSave;
    }

    /**
     * Set if the presentation has been modified since its latest save.
     *
     * @param modifiedSinceLatestSave {@code true} to indicate a modification, {@code false} otherwise.
     */
    public void setModifiedSinceLatestSave(boolean modifiedSinceLatestSave) {
        boolean oldValue = this.modifiedSinceLatestSave;
        this.modifiedSinceLatestSave = modifiedSinceLatestSave;
        PlatformHelper.run(() -> this.propertyChangeSupport.firePropertyChange("modifiedSinceLatestSave", oldValue, modifiedSinceLatestSave));
    }

    /**
     * This method creates a presentation from the given template archive. It prepares all resources
     * in order this engine to be used to create the new presentation.
     *
     * @param templateArchive The template archive file to create the presentation from.
     * @throws IOException            If an error occurred when processing the archive.
     * @throws IllegalAccessException If an error occurred when processing the archive.
     */
    public void createFromTemplate(File templateArchive) throws IOException, IllegalAccessException {
        this.setArchive(null);

        this.setModifiedSinceLatestSave(true);

        this.templateEngine = new TemplateEngine();
        this.templateEngine.loadArchive(templateArchive);

        this.setWorkingDirectory(this.templateEngine.getWorkingDirectory());

        this.configuration = new PresentationConfiguration();
        this.configuration.setPresentationFile(new File(this.getWorkingDirectory(), DEFAULT_PRESENTATION_FILENAME.getFieldName()));
        this.configuration.getVariables().addAll(this.templateEngine.getConfiguration().getDefaultVariables());

        final Configuration templateConfiguration = TemplateProcessor.getDefaultConfiguration();
        templateConfiguration.setDirectoryForTemplateLoading(this.templateEngine.getConfiguration().getFile().getParentFile());

        final Map tokens = new HashMap<>();
        tokens.put(TEMPLATE_SFX_JAVASCRIPT_RESOURCES_TOKEN, this.buildJavaScriptResourcesToInclude());

        try (final StringWriter writer = new StringWriter()) {

            final Template documentTemplate = templateConfiguration.getTemplate(this.templateEngine.getConfiguration().getFile().getName());
            documentTemplate.process(tokens, writer);
            writer.flush();

            this.configuration.setDocument(Jsoup.parse(writer.toString()));

            this.savePresentationFile();
        } catch (TemplateException e) {
            LOGGER.log(SEVERE, "Can not parse the template", e);
        }
    }

    /**
     * Get the configuration of the template stored in the presentation.
     *
     * @return The configuration of the template.
     */
    public TemplateConfiguration getTemplateConfiguration() {
        return this.templateEngine.getConfiguration();
    }

    /**
     * Add a slide to the presentation and save the presentation. If {@code afterSlideNumber} is {@code null} or not
     * found, the slide is added at the end of the presentation, otherwise it is added after the given slide number.
     *
     * @param template         The template of slide to add.
     * @param afterSlideNumber The slide number to insert the new slide after.
     * @return The new added slide.
     * @throws IOException If an error occurred when saving the presentation.
     */
    public Slide addSlide(SlideTemplate template, String afterSlideNumber) throws IOException {
        if (template == null)
            throw new IllegalArgumentException("The templateConfiguration for creating a slide can not be null");

        this.setModifiedSinceLatestSave(true);
        final Pair<Slide, Element> createdSlide = this.createSlide(template);

        if (afterSlideNumber == null) {
            this.configuration.getSlides().add(createdSlide.getKey());
        } else {
            ListIterator<Slide> slidesIterator = this.configuration.getSlides().listIterator();

            this.configuration.getSlideByNumber(afterSlideNumber);
            int index = -1;
            while (slidesIterator.hasNext()) {
                if (slidesIterator.next().getSlideNumber().equals(afterSlideNumber)) {
                    index = slidesIterator.nextIndex();
                    break;
                }
            }

            if (index > -1) {
                this.configuration.getSlides().add(index, createdSlide.getKey());
            } else {
                this.configuration.getSlides().add(createdSlide.getKey());
            }
        }

        if (afterSlideNumber == null || afterSlideNumber.isEmpty()) {
            this.configuration.getDocument()
                    .getElementById(this.templateEngine.getConfiguration().getSlidesContainer())
                    .append(createdSlide.getValue().outerHtml());
        } else {
            this.configuration.getDocument()
                    .getElementById(this.configuration.getSlideByNumber(afterSlideNumber).getId())
                    .after(createdSlide.getValue().outerHtml());
        }

        this.savePresentationFile();

        return createdSlide.getKey();
    }

    /**
     * Delete the slide with the slideNumber and save the presentation.
     *
     * @param slideNumber The slide number to delete.
     */
    public void deleteSlide(String slideNumber) {
        if (slideNumber == null) throw new IllegalArgumentException("Slide number can not be null");

        this.setModifiedSinceLatestSave(true);

        Slide slideToRemove = this.configuration.getSlideByNumber(slideNumber);
        if (slideToRemove != null) {
            this.configuration.getSlides().remove(slideToRemove);
            this.configuration.getDocument()
                    .getElementById(slideToRemove.getId()).remove();
        }

        this.savePresentationFile();
    }

    /**
     * Duplicates the given slide and add it to the presentation. The presentation is temporary saved.
     *
     * @param slide The slide to duplicate.
     * @return The duplicated slide.
     */
    public Slide duplicateSlide(Slide slide) throws IOException {
        if (slide == null) throw new IllegalArgumentException("The slide to duplicate can not be null");

        this.setModifiedSinceLatestSave(true);
        final Pair<Slide, Element> duplicatedSlide = this.createSlide(slide.getTemplate());

        // Add the slide to the presentation's slides
        int index = this.configuration.getSlides().indexOf(slide);
        if (index != -1) {
            if (index == this.configuration.getSlides().size() - 1) {
                this.configuration.getSlides().add(duplicatedSlide.getKey());
            } else {
                this.configuration.getSlides().add(index + 1, duplicatedSlide.getKey());
            }
        }

        // Update the slide elements
        duplicatedSlide.getKey().getElements().forEach(copiedElement -> {
            Optional<SlideElement> optional = slide.getElements().stream()
                    .filter(originalElement -> copiedElement.getTemplate().getId() == originalElement.getTemplate().getId())
                    .findFirst();

            // Update the copy
            if (optional.isPresent()) {
                copiedElement.setOriginalContentCode(optional.get().getOriginalContentCode());
                copiedElement.setOriginalContent(optional.get().getOriginalContent());
                copiedElement.setHtmlContent(optional.get().getHtmlContent());
            }
        });

        // Update the document
        this.getConfiguration().getDocument().getElementById(slide.getId()).after(duplicatedSlide.getValue().outerHtml());
        this.getConfiguration().updateSlideInDocument(duplicatedSlide.getKey());

        this.savePresentationFile();

        return duplicatedSlide.getKey();
    }

    /**
     * Move a slide and update the presentation's document. If <code>beforeSlide</code> is null,
     * the slide is moved at the end of the presentation. If <code>slideToMove</code> is equal to <code>beforeSlide</code>
     * nothing is done.
     * If an operation has been performed, the presentation is temporary saved.
     *
     * @param slideToMove The slide to move
     * @param beforeSlide The slide before <code>slideToMove</code> is moved
     * @throws IllegalArgumentException if the slideToMove is null
     */
    public void moveSlide(Slide slideToMove, Slide beforeSlide) {
        if (slideToMove == null) throw new IllegalArgumentException("The slideToMove to move can not be null");

        if (!slideToMove.equals(beforeSlide)) {
            this.setModifiedSinceLatestSave(true);

            this.configuration.getSlides().remove(slideToMove);

            final String slideHtml = this.configuration.getDocument()
                    .getElementById(slideToMove.getId()).outerHtml();

            this.configuration.getDocument()
                    .getElementById(slideToMove.getId())
                    .remove();

            if (beforeSlide == null) {
                this.configuration.getSlides().add(slideToMove);
                this.configuration.getDocument()
                        .getElementById(this.templateEngine.getConfiguration().getSlidesContainer())
                        .append(slideHtml);
            } else {
                int index = this.configuration.getSlides().indexOf(beforeSlide);
                this.configuration.getSlides().add(index, slideToMove);

                this.configuration.getDocument()
                        .getElementById(beforeSlide.getId())
                        .before(slideHtml);
            }

            this.savePresentationFile();
        }
    }

    /**
     * This method adds the given resource to the collection of resources present in {@link #getConfiguration()} as well
     * as in the presentation's document.
     *
     * @param resource The resource to add in the collection and the document.
     */
    public void addCustomResource(Resource resource) {
        if (resource != null
                && resource.getContent() != null
                && !resource.getContent().trim().isEmpty()) {

            this.setModifiedSinceLatestSave(true);
            this.configuration.getCustomResources().add(resource);

            /*
             * All of this ensure formatting using the HTML manipulation library.
             */
            final String location = this.relativizeFromWorkingDirectory(this.getTemplateConfiguration().getResourcesDirectory());
            final String htmlString = resource.buildHTMLString(location);
            final String resourceHtml = Jsoup.parseBodyFragment(htmlString).body().html();

            if (!this.configuration.getDocument().head().html().contains(resourceHtml)) {
                this.configuration.getDocument().head().append(htmlString);
            }
        }
    }

    public void savePresentationFile() {
        try (final FileOutputStream fileOutputStream = new FileOutputStream(this.configuration.getPresentationFile());
             final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, GlobalConfiguration.getDefaultCharset());
             final Writer writer = new BufferedWriter(outputStreamWriter)) {
            writer.write(this.configuration.getDocument().html());
            writer.flush();
        } catch (IOException e) {
            LOGGER.log(SEVERE, "Can not save presentation file", e);
        }
    }

    /**
     * This method loads all JavaScript resources that should be inserted in a template and return them in a String.
     *
     * @return The String containing the content of all JavaScript resources needed for a template
     */
    private String buildJavaScriptResourcesToInclude() {
        final StringBuilder builder = new StringBuilder();

        builder.append(IOUtils.read(PresentationEngine.class.getResourceAsStream(TEMPLATE_SFX_CONTENT_DEFINER_SCRIPT))).append("\n\n")
                .append(IOUtils.read(PresentationEngine.class.getResourceAsStream(TEMPLATE_SFX_SNIPPET_EXECUTOR_SCRIPT))).append("\n\n")
                .append(IOUtils.read(PresentationEngine.class.getResourceAsStream(TEMPLATE_SFX_CALLBACK_SCRIPT))).append("\n\n")
                .append(IOUtils.read(PresentationEngine.class.getResourceAsStream(TEMPLATE_SFX_QUIZ_CALLER_SCRIPT))).append("\n\n");

        return builder.toString();
    }

    /**
     * Create a {@link Slide slide} from the given {@link SlideTemplate template}.
     *
     * @param template The template to create the slide from.
     * @return A {@link Pair} where the key is the created {@link Slide} object and the value the HTML code get from the
     * parsed template.
     * @throws IOException          If an error occurs when parsing the template.
     * @throws NullPointerException If the given {@code template} is {@code null}.
     */
    private Pair<Slide, Element> createSlide(final SlideTemplate template) throws IOException {
        if (template == null) throw new NullPointerException("The template can not be null");

        this.setModifiedSinceLatestSave(true);
        final Pair<Slide, Element> result = new Pair<>();
        result.setKey(new Slide(template, System.currentTimeMillis() + ""));

        final Map tokens = new HashMap<>();
        tokens.put(TEMPLATE_SLIDE_ID_PREFIX_TOKEN, this.templateEngine.getConfiguration().getSlideIdPrefix());
        tokens.put(TEMPLATE_SLIDE_NUMBER_TOKEN, result.getKey().getSlideNumber());

        // Process the SlideElements by replacing their ID and setting their content
        final Configuration defaultConfiguration = TemplateProcessor.getDefaultConfiguration();
        if (template.getElements() != null) {
            Arrays.stream(template.getElements())
                    .forEach(element -> {
                        try (final StringWriter writer = new StringWriter();
                             final StringReader reader = new StringReader(element.getHtmlId())) {

                            final Template elementTemplate = new Template("element template", reader, defaultConfiguration);
                            elementTemplate.process(tokens, writer);
                            writer.flush();

                            result.getKey().updateElement(writer.toString(), "HTML", element.getDefaultContent(), element.getDefaultContent())
                                    .setTemplate(element);
                        } catch (IOException | TemplateException e) {
                            LOGGER.log(WARNING, "Can not parse element", e);
                        }
                    });
        }

        // Add dynamic attributes to the tokens by asking their values to the user
        // TODO INCUBATING
        tokens.clear();
        if (result.getKey().getTemplate().getDynamicAttributes() != null && result.getKey().getTemplate().getDynamicAttributes().length > 0) {
            Scanner scanner = new Scanner(System.in);
            String value;

            for (DynamicAttribute attribute : result.getKey().getTemplate().getDynamicAttributes()) {
                System.out.print(attribute.getPromptMessage() + " ");
                value = scanner.nextLine();

                if (value == null || value.trim().isEmpty()) {
                    tokens.put(attribute.getTemplateExpression(), "");
                } else {
                    tokens.put(attribute.getTemplateExpression(), String.format("%1$s=\"%2$s\"", attribute.getAttribute(), value.trim()));
                }
            }
        }

        // Parsing the slide's template file
        defaultConfiguration.setDirectoryForTemplateLoading(result.getKey().getTemplate().getFile().getParentFile());
        tokens.put(TEMPLATE_SLIDE_ID_PREFIX_TOKEN, this.templateEngine.getConfiguration().getSlideIdPrefix());
        tokens.put(TEMPLATE_SLIDE_NUMBER_TOKEN, result.getKey().getSlideNumber());
        tokens.put(TEMPLATE_SFX_CALLBACK_TOKEN, TEMPLATE_SFX_CALLBACK_CALL);
        tokens.putAll(this.configuration.getVariables().stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue)));

        try (final StringWriter writer = new StringWriter()) {
            final Template slideTemplate = defaultConfiguration.getTemplate(template.getFile().getName());
            slideTemplate.process(tokens, writer);
            writer.flush();

            result.setValue(DOMUtils.convertToNode(writer.toString()));
            result.getKey().setId(result.getValue().id());
        } catch (TemplateException e) {
            LOGGER.log(WARNING, "Error when parsing the slide's template", e);
        }

        return result;
    }
}

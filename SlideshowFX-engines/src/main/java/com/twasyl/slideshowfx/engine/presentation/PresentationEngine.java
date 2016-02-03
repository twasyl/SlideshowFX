/*
 * Copyright 2016 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.engine.presentation;

import com.twasyl.slideshowfx.content.extension.Resource;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.engine.AbstractEngine;
import com.twasyl.slideshowfx.engine.EngineException;
import com.twasyl.slideshowfx.engine.presentation.configuration.PresentationConfiguration;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import com.twasyl.slideshowfx.engine.presentation.configuration.SlideElement;
import com.twasyl.slideshowfx.engine.template.DynamicAttribute;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration;
import com.twasyl.slideshowfx.utils.*;
import com.twasyl.slideshowfx.utils.beans.Pair;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import javafx.embed.swing.SwingFXUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class manages presentations operation done with SlideshowFX. It is used to open them as well as add, update an
 * delete slides.
 * The extension of a presentation is {@code sfx}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
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

    private static final String TEMPLATE_SLIDE_NUMBER_TOKEN = "slideNumber";
    private static final String TEMPLATE_SFX_JAVASCRIPT_RESOURCES_TOKEN = "sfxJavascriptResources";
    private static final String TEMPLATE_SFX_CALLBACK_TOKEN = "sfxCallback";
    private static final String TEMPLATE_SLIDE_ID_PREFIX_TOKEN = "slideIdPrefix";

    private static final String TEMPLATE_SFX_CONTENT_DEFINER_SCRIPT = "/com/twasyl/slideshowfx/js/setField.js";
    private static final String TEMPLATE_SFX_SNIPPET_EXECUTOR_SCRIPT = "/com/twasyl/slideshowfx/js/snippetExecutor.js";
    private static final String TEMPLATE_SFX_CALLBACK_SCRIPT = "/com/twasyl/slideshowfx/js/sendInformationToSlideshowFX.js";
    private static final String TEMPLATE_SFX_QUIZ_CALLER_SCRIPT = "/com/twasyl/slideshowfx/js/quizCaller.js";

    private static final String TEMPLATE_SFX_CALLBACK_CALL = "sendInformationToSlideshowFX(this);";

    private TemplateEngine templateEngine;

    private boolean modifiedSinceLatestSave;

    public PresentationEngine() {
        super(DEFAULT_ARCHIVE_EXTENSION, "presentation-config.json");
        this.templateEngine = new TemplateEngine();

        Presentations.register(this);
    }

    @Override
    public boolean checkConfiguration() throws EngineException {
        return false;
    }

    @Override
    public PresentationConfiguration readConfiguration(File configurationFile) throws NullPointerException, IllegalArgumentException, IOException, IllegalAccessException {
        if(configurationFile == null) throw new NullPointerException("The configuration file can not be null");
        if(!configurationFile.exists()) throw new FileNotFoundException("The configuration file does not exist");
        if(!configurationFile.canRead()) throw new IllegalAccessException("The configuration file can not be read");

        final PresentationConfiguration presentationConfiguration = new PresentationConfiguration();
        presentationConfiguration.setPresentationFile(new File(this.getWorkingDirectory(), PresentationConfiguration.DEFAULT_PRESENTATION_FILENAME));

        JsonObject configurationJson = JSONHelper.readFromFile(configurationFile);
        JsonObject presentationJson = configurationJson.getJsonObject("presentation");

        presentationConfiguration.setId(presentationJson.getLong("id", System.currentTimeMillis()));

        if(presentationJson.getJsonArray("custom-resources") != null) {
            presentationJson.getJsonArray("custom-resources")
                    .forEach(customResource -> {
                        final Resource resource = new Resource(
                                ResourceType.valueOf(((JsonObject) customResource).getString("type")),
                                new String(Base64.getDecoder().decode(((JsonObject) customResource).getString("content")))
                        );

                        presentationConfiguration.getCustomResources().add(resource);
                    });
        }

        if(presentationJson.getJsonArray("variables") != null) {
            presentationJson.getJsonArray("variables")
                    .forEach(variableJson -> {
                        final Pair<String, String> variable = new Pair<>();
                        variable.setKey(((JsonObject) variableJson).getString("name"));
                        variable.setValue(new String(Base64.getDecoder().decode(((JsonObject) variableJson).getString("value"))));
                        presentationConfiguration.getVariables().add(variable);
                    });
        }

        presentationJson.getJsonArray("slides")
                .forEach(slideJson -> {
                    final Slide slide = new Slide();

                    slide.setId(((JsonObject) slideJson).getString("id"));
                    slide.setSlideNumber(((JsonObject) slideJson).getString("number"));
                    slide.setTemplate(this.templateEngine.getConfiguration().getSlideTemplate(((JsonObject) slideJson).getInteger(
                        "template-id")));

                    try {
                        slide.setThumbnail(SwingFXUtils.toFXImage(ImageIO.read(new File(this.templateEngine.getConfiguration().getSlidesThumbnailDirectory(), slide.getSlideNumber().concat(".png"))), null));
                    } catch (IOException e) {
                        LOGGER.log(Level.INFO, "Error setting the thumbnail", e);
                    }

                    ((JsonObject) slideJson).getJsonArray("elements")
                            .forEach(slideElementJson -> {
                                final SlideElement slideElement = new SlideElement();
                                slideElement.setTemplate(slide.getTemplate().getSlideElementTemplate(((JsonObject) slideElementJson).getInteger(
                                    "template-id")));
                                slideElement.setId(((JsonObject) slideElementJson).getString("element-id"));
                                slideElement.setOriginalContentCode(((JsonObject) slideElementJson).getString("original-content-code"));
                                slideElement.setOriginalContentAsBase64(((JsonObject) slideElementJson).getString("original-content"));
                                slideElement.setHtmlContentAsBase64(((JsonObject) slideElementJson).getString("html-content"));

                                slide.getElements().add(slideElement);
                            });

                    presentationConfiguration.getSlides().add(slide);
                });

        return presentationConfiguration;
    }

    @Override
    public void writeConfiguration(File configurationFile) throws NullPointerException, IOException {
        if(configurationFile == null) throw new NullPointerException("The configuration to write into can not be null");

        if(this.configuration != null) {
            final JsonObject presentationJson = new JsonObject();
            final JsonArray slidesJson = new JsonArray();
            final JsonArray customResourcesJson = new JsonArray();
            final JsonArray variablesJson = new JsonArray();

            presentationJson.put("id", this.configuration.getId());

            this.configuration.getCustomResources()
                    .stream()
                    .forEach(resource -> {
                        final JsonObject resourceJson = new JsonObject()
                                .put("type", resource.getType().name())
                                .put("content", Base64.getEncoder().encodeToString(resource.getContent().getBytes()));

                        customResourcesJson.add(resourceJson);
                    });

            this.configuration.getVariables()
                    .forEach(variable -> {
                        final JsonObject variableJson = new JsonObject()
                                .put("name", variable.getKey())
                                .put("value", Base64.getEncoder().encodeToString(variable.getValue().getBytes()));

                        variablesJson.add(variableJson);
                    });

            this.configuration.getSlides()
                    .stream()
                    .forEach(slide -> {
                        final JsonArray elementsJson = new JsonArray();
                        final JsonObject slideJson = new JsonObject();

                        slideJson.put("template-id", slide.getTemplate().getId())
                                .put("id", slide.getId())
                                .put("number", slide.getSlideNumber());

                        slide.getElements()
                                .stream()
                                .forEach(slideElement -> {
                                    final JsonObject elementJson = new JsonObject();
                                    elementJson.put("template-id", slideElement.getTemplate().getId())
                                            .put("element-id", slideElement.getId())
                                            .put("original-content-code", slideElement.getOriginalContentCode())
                                            .put("original-content", slideElement.getOriginalContentAsBase64())
                                            .put("html-content", slideElement.getHtmlContentAsBase64());

                                    elementsJson.add(elementJson);
                                });

                        slideJson.put("elements", elementsJson);
                        slidesJson.add(slideJson);
                    });

            presentationJson.put("custom-resources", customResourcesJson);
            presentationJson.put("variables", variablesJson);
            presentationJson.put("slides", slidesJson);

            final JsonObject finalObject = new JsonObject();
            finalObject.put("presentation", presentationJson);

            JSONHelper.writeObject(finalObject, configurationFile);
        }
    }

    @Override
    public void loadArchive(File file) throws IllegalArgumentException, NullPointerException, IOException, IllegalAccessException {
        if(file == null) throw new NullPointerException("The archive file can not be null");
        if(!file.exists()) throw new FileNotFoundException("The archive file does not exist");
        if(!file.canRead()) throw new IllegalAccessException("The archive file can not be read");
        if(!file.getName().endsWith(this.getArchiveExtension())) throw new IllegalArgumentException("The extension of the archive is not valid");

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
        try(final StringWriter writer = new StringWriter()) {

            final Template documentTemplate = templateConfiguration.getTemplate(this.templateEngine.getConfiguration().getFile().getName());
            documentTemplate.process(tokens, writer);
            writer.flush();

            this.configuration.setDocument(Jsoup.parse(writer.toString()));

            this.savePresentationFile();
        } catch (TemplateException e) {
            LOGGER.log(Level.SEVERE, "Can not parse template", e);
        }

        LOGGER.fine("Building presentation file");
        // Append the custom resources
        this.configuration.getCustomResources()
                .stream()
                .forEach(resource -> this.addCustomResource(resource));

        // Append the slides' content to the presentation
        tokens.clear();
        tokens.put(TEMPLATE_SFX_CALLBACK_TOKEN, TEMPLATE_SFX_CALLBACK_CALL);
        tokens.put(TEMPLATE_SLIDE_ID_PREFIX_TOKEN, this.templateEngine.getConfiguration().getSlideIdPrefix());
        tokens.putAll(this.configuration.getVariables().stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue)));

        for(Slide s : this.configuration.getSlides()) {
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
                LOGGER.log(Level.SEVERE, "Can not read slide's template", e);
            }
        }

        this.savePresentationFile();
    }

    @Override
    public synchronized void saveArchive(File file) throws IllegalArgumentException, IOException {

        this.writeConfiguration();

        LOGGER.fine("Create slides thumbnails");
        if(!this.templateEngine.getConfiguration().getSlidesThumbnailDirectory().exists()) this.templateEngine.getConfiguration().getSlidesThumbnailDirectory().mkdirs();
        else {
            Arrays.stream(this.templateEngine.getConfiguration().getSlidesThumbnailDirectory()
                    .listFiles(f -> f.isFile()))
                    .forEach(slideFile -> slideFile.delete());
        }

        this.configuration.getSlides()
                .stream()
                .filter(slide -> slide != null && slide.getThumbnail() != null)
                .forEach(slide -> {
                    LOGGER.fine("Creating thumbnail file: " + this.templateEngine.getConfiguration().getSlidesThumbnailDirectory().getAbsolutePath() + File.separator + slide.getSlideNumber() + ".png");
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(slide.getThumbnail(), null), "png", new File(this.templateEngine.getConfiguration().getSlidesThumbnailDirectory(), slide.getSlideNumber().concat(".png")));
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING,
                                String.format("Can not create thumbnail for slide number %1$s", slide.getSlideNumber()),
                                e);
                    }
                });

        ZipUtils.zip(this.getWorkingDirectory(), file);

        this.setModifiedSinceLatestSave(false);
        LOGGER.fine("Presentation saved");
    }

    /**
     * Indicates if the presentation has already been saved by testing if the {@link #getArchive()}
     * method returns {@code null} or not.
     * @return {@code true} if {@link #getArchive()} is not {@code null}, {@code false} otherwise.
     */
    public boolean isPresentationAlreadySaved() {
        return this.getArchive() != null;
    }

    /**
     * Indicates if the presentation has been modified since the latest save. If the presentation has never been saved,
     * then the presentation is considered modified.
     * @return {@code true} if the presentation has been modified since the latest save, {@code false} otherwise.
     */
    public boolean isModifiedSinceLatestSave() {
        return modifiedSinceLatestSave;
    }

    /**
     * Set if the presentation has been modified since its latest save.
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
     * @throws java.io.IOException If an error occurred when processing the archive.
     * @throws java.lang.IllegalAccessException If an error occurred when processing the archive.
     */
    public void createFromTemplate(File templateArchive) throws IOException, IllegalAccessException {
        this.setArchive(null);

        this.setModifiedSinceLatestSave(true);

        this.templateEngine = new TemplateEngine();
        this.templateEngine.loadArchive(templateArchive);

        this.setWorkingDirectory(this.templateEngine.getWorkingDirectory());

        this.configuration = new PresentationConfiguration();
        this.configuration.setPresentationFile(new File(this.getWorkingDirectory(), PresentationConfiguration.DEFAULT_PRESENTATION_FILENAME));
        this.configuration.getVariables().addAll(this.templateEngine.getConfiguration().getDefaultVariables());

        final Configuration templateConfiguration = TemplateProcessor.getDefaultConfiguration();
        templateConfiguration.setDirectoryForTemplateLoading(this.templateEngine.getConfiguration().getFile().getParentFile());

        final Map tokens = new HashMap<>();
        tokens.put(TEMPLATE_SFX_JAVASCRIPT_RESOURCES_TOKEN, this.buildJavaScriptResourcesToInclude());

        try(final StringWriter writer = new StringWriter()) {

            final Template documentTemplate = templateConfiguration.getTemplate(this.templateEngine.getConfiguration().getFile().getName());
            documentTemplate.process(tokens, writer);
            writer.flush();

            this.configuration.setDocument(Jsoup.parse(writer.toString()));

            this.savePresentationFile();
        } catch (TemplateException e) {
            LOGGER.log(Level.SEVERE, "Can not parse the template", e);
        }
    }

    /**
     * Get the configuration of the template stored in the presentation.
     *
     * @return The configuration of the template.
     */
    public TemplateConfiguration getTemplateConfiguration() { return this.templateEngine.getConfiguration(); }

    /**
     * Add a slide to the presentation and save the presentation. If {@code afterSlideNumber} is {@code null} or not
     * found, the slide is added at the end of the presentation, otherwise it is added after the given slide number.
     * @param template The template of slide to add.
     * @param afterSlideNumber The slide number to insert the new slide after.
     * @return The new added slide.
     * @throws IOException If an error occurred when saving the presentation.
     */
    public Slide addSlide(SlideTemplate template, String afterSlideNumber) throws IOException {
        if(template == null) throw new IllegalArgumentException("The templateConfiguration for creating a slide can not be null");

        this.setModifiedSinceLatestSave(true);
        final Pair<Slide, Element> createdSlide = this.createSlide(template);

        if(afterSlideNumber == null) {
            this.configuration.getSlides().add(createdSlide.getKey());
        } else {
            ListIterator<Slide> slidesIterator = this.configuration.getSlides().listIterator();

            this.configuration.getSlideByNumber(afterSlideNumber);
            int index = -1;
            while(slidesIterator.hasNext()) {
                if(slidesIterator.next().getSlideNumber().equals(afterSlideNumber)) {
                    index = slidesIterator.nextIndex();
                    break;
                }
            }

            if(index > -1) {
                this.configuration.getSlides().add(index, createdSlide.getKey());
            } else {
                this.configuration.getSlides().add(createdSlide.getKey());
            }
        }

        if(afterSlideNumber == null || afterSlideNumber.isEmpty()) {
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
     * @param slideNumber The slide number to delete.
     */
    public void deleteSlide(String slideNumber) {
        if(slideNumber == null) throw new IllegalArgumentException("Slide number can not be null");

        this.setModifiedSinceLatestSave(true);

        Slide slideToRemove = this.configuration.getSlideByNumber(slideNumber);
        if(slideToRemove != null) {
            this.configuration.getSlides().remove(slideToRemove);
            this.configuration.getDocument()
                    .getElementById(slideToRemove.getId()).remove();
        }

        this.savePresentationFile();
    }

    /**
     * Duplicates the given slide and add it to the presentation. The presentation is temporary saved.
     * @param slide The slide to duplicate.
     * @return The duplicated slide.
     */
    public Slide duplicateSlide(Slide slide) throws IOException {
        if(slide == null) throw new IllegalArgumentException("The slide to duplicate can not be null");

        this.setModifiedSinceLatestSave(true);
        final Pair<Slide, Element> duplicatedSlide = this.createSlide(slide.getTemplate());

        // Add the slide to the presentation's slides
        int index = this.configuration.getSlides().indexOf(slide);
        if(index != -1) {
            if(index == this.configuration.getSlides().size() - 1) {
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
     * @param slideToMove The slide to move
     * @param beforeSlide The slide before <code>slideToMove</code> is moved
     * @throws java.lang.IllegalArgumentException if the slideToMove is null
     */
    public void moveSlide(Slide slideToMove, Slide beforeSlide) {
        if(slideToMove == null) throw new IllegalArgumentException("The slideToMove to move can not be null");

        if(!slideToMove.equals(beforeSlide)) {
            this.setModifiedSinceLatestSave(true);

            this.configuration.getSlides().remove(slideToMove);

            final String slideHtml = this.configuration.getDocument()
                    .getElementById(slideToMove.getId()).outerHtml();

            this.configuration.getDocument()
                    .getElementById(slideToMove.getId())
                    .remove();

            if(beforeSlide == null) {
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
     * @param resource The resource to add in the collection and the document.
     */
    public void addCustomResource(Resource resource) {
        if(resource != null
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

            if(!this.configuration.getDocument().head().html().contains(resourceHtml)) {
                this.configuration.getDocument().head().append(htmlString);
            }
        }
    }

    public void savePresentationFile() {
        try(final Writer writer = new FileWriter(this.configuration.getPresentationFile())) {
            writer.write(this.configuration.getDocument().html());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method loads all JavaScript resources that should be inserted in a template and return them in a String.
     * @return The String containing the content of all JavaScript resources needed for a template
     */
    private String buildJavaScriptResourcesToInclude() {
        final StringBuilder builder = new StringBuilder();

        builder.append(ResourceHelper.readResource(TEMPLATE_SFX_CONTENT_DEFINER_SCRIPT)).append("\n\n")
                .append(ResourceHelper.readResource(TEMPLATE_SFX_SNIPPET_EXECUTOR_SCRIPT)).append("\n\n")
                .append(ResourceHelper.readResource(TEMPLATE_SFX_CALLBACK_SCRIPT)).append("\n\n")
                .append(ResourceHelper.readResource(TEMPLATE_SFX_QUIZ_CALLER_SCRIPT)).append("\n\n");

        return builder.toString();
    }

    /**
     * Create a {@link Slide slide} from the given {@link SlideTemplate template}.
     * @param template The template to create the slide from.
     * @return A {@link Pair} where the key is the created {@link Slide} object and the value the HTML code get from the
     * parsed template.
     * @throws IOException If an error occurs when parsing the template.
     * @throws NullPointerException If the given {@code template} is {@code null}.
     */
    private Pair<Slide, Element> createSlide(final SlideTemplate template) throws NullPointerException, IOException {
        if(template == null) throw new NullPointerException("The template can not be null");

        this.setModifiedSinceLatestSave(true);
        final Pair<Slide, Element> result = new Pair<>();
        result.setKey(new Slide(template, System.currentTimeMillis() + ""));

        final Map tokens = new HashMap<>();
        tokens.put(TEMPLATE_SLIDE_ID_PREFIX_TOKEN, this.templateEngine.getConfiguration().getSlideIdPrefix());
        tokens.put(TEMPLATE_SLIDE_NUMBER_TOKEN, result.getKey().getSlideNumber());

        // Process the SlideElements by replacing their ID and setting their content
        final Configuration defaultConfiguration = TemplateProcessor.getDefaultConfiguration();
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
                    LOGGER.log(Level.WARNING, "Can not parse element", e);
                }
            });


        // Add dynamic attributes to the tokens by asking their values to the user
        // INCUBATING
        tokens.clear();
        if(result.getKey().getTemplate().getDynamicAttributes() != null && result.getKey().getTemplate().getDynamicAttributes().length > 0) {
            Scanner scanner = new Scanner(System.in);
            String value;

            for(DynamicAttribute attribute : result.getKey().getTemplate().getDynamicAttributes()) {
                System.out.print(attribute.getPromptMessage() + " ");
                value = scanner.nextLine();

                if(value == null || value.trim().isEmpty()) {
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

        try(final StringWriter writer = new StringWriter()) {
            final Template slideTemplate = defaultConfiguration.getTemplate(template.getFile().getName());
            slideTemplate.process(tokens, writer);
            writer.flush();

            result.setValue(DOMUtils.convertToNode(writer.toString()));
            result.getKey().setId(result.getValue().id());
        } catch (TemplateException e) {
            LOGGER.log(Level.WARNING, "Error when parsing the slide's template", e);
        }

        return result;
    }
}

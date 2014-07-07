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

package com.twasyl.slideshowfx.engine.presentation;

import com.twasyl.slideshowfx.engine.AbstractEngine;
import com.twasyl.slideshowfx.engine.EngineException;
import com.twasyl.slideshowfx.engine.presentation.configuration.PresentationConfiguration;
import com.twasyl.slideshowfx.engine.presentation.configuration.SlideElementConfiguration;
import com.twasyl.slideshowfx.engine.presentation.configuration.SlidePresentationConfiguration;
import com.twasyl.slideshowfx.engine.template.*;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplateConfiguration;
import com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration;
import com.twasyl.slideshowfx.utils.DOMUtils;
import com.twasyl.slideshowfx.utils.JSONHelper;
import com.twasyl.slideshowfx.utils.ZipUtils;
import javafx.embed.swing.SwingFXUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages presentations operation done with SlideshowFX. It is used to open them as well as add, update an
 * delete slides.
 * The extension of a presentation is <code>sfx</code>.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class PresentationEngine extends AbstractEngine<PresentationConfiguration> {

    private static final Logger LOGGER = Logger.getLogger(PresentationEngine.class.getName());
    private static final String VELOCITY_SLIDE_NUMBER_TOKEN = "slideNumber";
    private static final String VELOCITY_SFX_CALLBACK_TOKEN = "sfxCallback";
    private static final String VELOCITY_SFX_CONTENT_DEFINER_TOKEN = "sfxContentDefiner";
    private static final String VELOCITY_SLIDE_ID_PREFIX_TOKEN = "slideIdPrefix";

    private static final String VELOCITY_SFX_CONTENT_DEFINER_SCRIPT = "function setField(slide, what, value) {\n" +
            "\telement = document.getElementById(slide + \"-\" + what);\n" +
            "\telement.innerHTML = decodeURIComponent(escape(window.atob(value)));\n" +
            "}";
    private static final String VELOCITY_SFX_CALLBACK_SCRIPT = "function sendInformationToSlideshowFX(source) {\n" +
            "\tdashIndex = source.id.indexOf(\"-\");\n" +
            "\tslideNumber = source.id.substring(0, dashIndex);\n" +
            "\tfieldName = source.id.substring(dashIndex+1);\n" +
            "\n" +
            "\tsfx.prefillContentDefinition(slideNumber, fieldName, window.btoa(unescape(encodeURIComponent(source.innerHTML))));\n" +
            "}";

    private static final String VELOCITY_SFX_CALLBACK_CALL = "sendInformationToSlideshowFX(this);";

    private TemplateEngine templateEngine;

    public PresentationEngine() {
        super("sfx", "presentation-config.json");
        this.templateEngine = new TemplateEngine();
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
        JsonObject presentationJson = configurationJson.getObject("presentation");

        presentationJson.getArray("slides")
                .forEach(slideJson -> {
                    final SlidePresentationConfiguration slide = new SlidePresentationConfiguration();

                    slide.setId(((JsonObject) slideJson).getString("id"));
                    slide.setSlideNumber(((JsonObject) slideJson).getString("number"));
                    slide.setTemplate(this.templateEngine.getConfiguration().getSlideTemplate(((JsonObject) slideJson).getNumber("template-id").intValue()));

                    try {
                        slide.setThumbnail(SwingFXUtils.toFXImage(ImageIO.read(new File(this.templateEngine.getConfiguration().getSlidesThumbnailDirectory(), slide.getSlideNumber().concat(".png"))), null));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ((JsonObject) slideJson).getArray("elements")
                            .forEach(slideElementJson -> {
                                final SlideElementConfiguration slideElement = new SlideElementConfiguration();
                                slideElement.setId(((JsonObject) slideElementJson).getString("element-id"));
                                slideElement.setOriginalContentCode(((JsonObject) slideElementJson).getString("original-content-code"));
                                slideElement.setOriginalContentAsBase64(((JsonObject) slideElementJson).getString("original-content"));
                                slideElement.setHtmlContentAsBase64(((JsonObject) slideElementJson).getString("html-content"));

                                slide.getElements().put(slideElement.getId(), slideElement);
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

            this.configuration.getSlides()
                    .stream()
                    .forEach(slide -> {
                        final JsonArray elementsJson = new JsonArray();
                        final JsonObject slideJson = new JsonObject();

                        slideJson.putNumber("template-id", slide.getTemplate().getId())
                                .putString("id", slide.getId())
                                .putString("number", slide.getSlideNumber());

                        slide.getElements().values()
                                .stream()
                                .forEach(slideElement -> {
                                    final JsonObject elementJson = new JsonObject();
                                    elementJson.putString("element-id", slideElement.getId())
                                            .putString("original-content-code", slideElement.getOriginalContentCode())
                                            .putString("original-content", slideElement.getOriginalContentAsBase64())
                                            .putString("html-content", slideElement.getHtmlContentAsBase64());

                                    elementsJson.addObject(elementJson);
                                });

                        slideJson.putArray("elements", elementsJson);
                        slidesJson.addObject(slideJson);
                    });

            presentationJson.putArray("slides", slidesJson);

            final JsonObject finalObject = new JsonObject();
            finalObject.putObject("presentation", presentationJson);

            JSONHelper.writeObject(finalObject, configurationFile);
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

        // The template configuration has to be read and set
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setWorkingDirectory(this.getWorkingDirectory());
        this.templateEngine.setConfiguration(this.templateEngine.readConfiguration());

        final PresentationConfiguration configuration = this.readConfiguration();
        this.setConfiguration(configuration);

        Velocity.init();
        VelocityContext context = new VelocityContext();

        // Replacing the velocity tokens
        try(final InputStream inputStream = new FileInputStream(this.templateEngine.getConfiguration().getFile());
            final Reader reader = new InputStreamReader(inputStream);
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final Writer writer = new OutputStreamWriter(outputStream)) {

            context.put(VELOCITY_SFX_CONTENT_DEFINER_TOKEN, VELOCITY_SFX_CONTENT_DEFINER_SCRIPT);
            context.put(VELOCITY_SFX_CALLBACK_TOKEN, VELOCITY_SFX_CALLBACK_SCRIPT);

            Velocity.evaluate(context, writer, "", reader);

            writer.flush();
            outputStream.flush();

            this.configuration.setDocument(Jsoup.parse(outputStream.toString("UTF8")));

            this.savePresentationFile();
        }

        // Append the slides' content to the presentation
        LOGGER.fine("Building presentation file");
        context = new VelocityContext();
        context.put(VELOCITY_SFX_CALLBACK_TOKEN, VELOCITY_SFX_CALLBACK_CALL);
        context.put(VELOCITY_SLIDE_ID_PREFIX_TOKEN, this.templateEngine.getConfiguration().getSlideIdPrefix());

        for(SlidePresentationConfiguration s : this.configuration.getSlides()) {
            try (final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                 final Writer outputWriter = new BufferedWriter(new OutputStreamWriter(arrayOutputStream));
                 final FileReader slideReader = new FileReader(s.getTemplate().getFile())) {

                context.put(VELOCITY_SLIDE_NUMBER_TOKEN, s.getSlideNumber());
                Velocity.evaluate(context, outputWriter, "", slideReader);

                outputWriter.flush();
                arrayOutputStream.flush();

                this.configuration.getDocument()
                        .getElementById(this.templateEngine.getConfiguration().getSlidesContainer())
                        .append(arrayOutputStream.toString("UTF8"));

                s.getElements().values()
                        .stream()
                        .forEach(element -> this.configuration.getDocument()
                                .getElementById(element.getId())
                                .html(element.getHtmlContent()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.savePresentationFile();
    }

    @Override
    public void saveArchive(File file) throws IllegalArgumentException, IOException {

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
        LOGGER.fine("Presentation saved");
    }

    /**
     * This method creates a presentation from the given template archive. It prepares all resources
     * in order this engine to be used to create the new presentation.
     *
     * @param templateArchive The template archive file to create the presentation from.
     */
    public void createFromTemplate(File templateArchive) throws IOException, IllegalAccessException {
        this.templateEngine = new TemplateEngine();
        this.templateEngine.loadArchive(templateArchive);

        this.setWorkingDirectory(this.templateEngine.getWorkingDirectory());

        this.configuration = new PresentationConfiguration();
        this.configuration.setPresentationFile(new File(this.getWorkingDirectory(), PresentationConfiguration.DEFAULT_PRESENTATION_FILENAME));

        Velocity.init();
        VelocityContext context = new VelocityContext();

        // Replacing the velocity tokens
        try(final InputStream inputStream = new FileInputStream(this.templateEngine.getConfiguration().getFile());
            final Reader reader = new InputStreamReader(inputStream);
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final Writer writer = new OutputStreamWriter(outputStream)) {

            context.put(VELOCITY_SFX_CONTENT_DEFINER_TOKEN, VELOCITY_SFX_CONTENT_DEFINER_SCRIPT);
            context.put(VELOCITY_SFX_CALLBACK_TOKEN, VELOCITY_SFX_CALLBACK_SCRIPT);

            Velocity.evaluate(context, writer, "", reader);

            writer.flush();
            outputStream.flush();

            this.configuration.setDocument(Jsoup.parse(outputStream.toString("UTF8")));

            this.savePresentationFile();
        }
    }

    /**
     * Get the configuration of the template stored in the presentation.
     *
     * @return The configuration of the template.
     */
    public TemplateConfiguration getTemplateConfiguration() { return this.templateEngine.getConfiguration(); }

    /**
     * Add a slide to the presentation and save the presentation
     * @param template
     * @throws IOException
     */
    public SlidePresentationConfiguration addSlide(SlideTemplateConfiguration template, String afterSlideNumber) throws IOException {
        if(template == null) throw new IllegalArgumentException("The templateConfiguration for creating a slide can not be null");
        Velocity.init();

        final SlidePresentationConfiguration slide = new SlidePresentationConfiguration(template, System.currentTimeMillis() + "");

        if(afterSlideNumber == null) {
            this.configuration.getSlides().add(slide);
        } else {
            ListIterator<SlidePresentationConfiguration> slidesIterator = this.configuration.getSlides().listIterator();

            this.configuration.getSlideByNumber(afterSlideNumber);
            int index = -1;
            while(slidesIterator.hasNext()) {
                if(slidesIterator.next().getSlideNumber().equals(afterSlideNumber)) {
                    index = slidesIterator.nextIndex();
                    break;
                }
            }

            if(index > -1) {
                this.configuration.getSlides().add(index, slide);
            } else {
                this.configuration.getSlides().add(slide);
            }
        }


        final Reader slideFileReader = new FileReader(template.getFile());
        final ByteArrayOutputStream slideContentByte = new ByteArrayOutputStream();
        final Writer slideContentWriter = new OutputStreamWriter(slideContentByte);

        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_SLIDE_ID_PREFIX_TOKEN, this.templateEngine.getConfiguration().getSlideIdPrefix());
        context.put(VELOCITY_SLIDE_NUMBER_TOKEN, slide.getSlideNumber());
        context.put(VELOCITY_SFX_CALLBACK_TOKEN, VELOCITY_SFX_CALLBACK_CALL);

        if(template.getDynamicAttributes() != null && template.getDynamicAttributes().length > 0) {
            Scanner scanner = new Scanner(System.in);
            String value;

            for(DynamicAttribute attribute : template.getDynamicAttributes()) {
                System.out.print(attribute.getPromptMessage() + " ");
                value = scanner.nextLine();

                if(value == null || value.trim().isEmpty()) {
                    context.put(attribute.getTemplateExpression(), "");
                } else {
                    context.put(attribute.getTemplateExpression(), String.format("%1$s=\"%2$s\"", attribute.getAttribute(), value.trim()));
                }
            }
        }

        Velocity.evaluate(context, slideContentWriter, "", slideFileReader);
        slideContentWriter.flush();
        slideContentWriter.close();

        Element htmlSlide = DOMUtils.convertToNode(slideContentByte.toString("UTF8"));
        slide.setId(htmlSlide.id());

        if(afterSlideNumber == null || afterSlideNumber.isEmpty()) {
            this.configuration.getDocument()
                    .getElementById(this.templateEngine.getConfiguration().getSlidesContainer())
                    .append(htmlSlide.outerHtml());
        } else {
            this.configuration.getDocument()
                    .getElementById(this.configuration.getSlideByNumber(afterSlideNumber).getId())
                    .after(htmlSlide.outerHtml());
        }

        this.savePresentationFile();

        return slide;
    }

    /**
     * Delete the slide with the slideNumber and save the presentation
     * @param slideNumber
     */
    public void deleteSlide(String slideNumber) throws ParserConfigurationException {
        if(slideNumber == null) throw new IllegalArgumentException("Slide number can not be null");

        SlidePresentationConfiguration slideToRemove = this.configuration.getSlideByNumber(slideNumber);
        if(slideToRemove != null) {
            this.configuration.getSlides().remove(slideToRemove);
            this.configuration.getDocument()
                    .getElementById(slideToRemove.getId()).remove();
        }

        this.savePresentationFile();
    }

    /**
     * Duplicates the given slide and add it to the presentation. The presentation is temporary saved.
     */
    public SlidePresentationConfiguration duplicateSlide(SlidePresentationConfiguration slide) {
        if(slide == null) throw new IllegalArgumentException("The slide to duplicate can not be null");

        final SlidePresentationConfiguration copy = new SlidePresentationConfiguration(slide.getTemplate(), System.currentTimeMillis() + "");
        copy.setThumbnail(slide.getThumbnail());
        copy.setId(slide.getId());

        // Copy the elements. Keep original IDs for now
        SlideElementConfiguration copySlideElement;
        for(SlideElementConfiguration slideElement : slide.getElements().values()) {
            copySlideElement = new SlideElementConfiguration();
            copySlideElement.setId(slideElement.getId());
            copySlideElement.setOriginalContentCode(slideElement.getOriginalContentCode());
            copySlideElement.setOriginalContent(slideElement.getOriginalContent());
            copySlideElement.setHtmlContent(slideElement.getHtmlContent());

            copy.getElements().put(copySlideElement.getId(), copySlideElement);
        }

        // Apply the templateConfiguration engine for replacing dynamic elements
        final VelocityContext originalContext = new VelocityContext();
        originalContext.put(VELOCITY_SLIDE_ID_PREFIX_TOKEN, this.templateEngine.getConfiguration().getSlideIdPrefix());
        originalContext.put(VELOCITY_SLIDE_NUMBER_TOKEN, slide.getSlideNumber());

        final VelocityContext copyContext = new VelocityContext();
        copyContext.put(VELOCITY_SLIDE_ID_PREFIX_TOKEN, this.templateEngine.getConfiguration().getSlideIdPrefix());
        copyContext.put(VELOCITY_SLIDE_NUMBER_TOKEN, copy.getSlideNumber());

        try (ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
             Writer writer = new OutputStreamWriter(byteOutput)) {

            String oldId, newId;

            /**
             * For each ID:
             * 1- Look for the original ID ; ie with the original slide number
             * 2- Replace each original ID by the ID of the new slide
             * 3- Store the new elements in a list
             * 4- Clear the current elements
             * 5- Create the new map of elements
             */
            List<SlideElementConfiguration> copySlideElements = new ArrayList<>();
            for(String dynamicId : slide.getTemplate().getDynamicIds()) {
                Velocity.evaluate(originalContext, writer, "", dynamicId);
                writer.flush();

                oldId = new String(byteOutput.toByteArray());
                byteOutput.reset();

                /**
                 * Manage slide elements IDs
                 */
                if(copy.getElements().containsKey(oldId)) {
                    Velocity.evaluate(copyContext, writer, "", dynamicId);
                    writer.flush();

                    newId = new String(byteOutput.toByteArray());
                    byteOutput.reset();

                    // Change IDs
                    copySlideElement = copy.getElements().get(oldId);
                    copySlideElement.setId(newId);

                    copySlideElements.add(copySlideElement);
                }

                /**
                 * Manage slide ID
                 */
                if(copy.getId().equals(oldId)) {
                    Velocity.evaluate(copyContext, writer, "", dynamicId);
                    writer.flush();

                    newId = new String(byteOutput.toByteArray());
                    byteOutput.reset();

                    copy.setId(newId);
                }
            }

            copy.getElements().clear();
            for(SlideElementConfiguration copySE : copySlideElements) {
                copy.getElements().put(copySE.getId(), copySE);
            }

            copySlideElements = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * Add the slide to the document
         */
        try(final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final Writer writer = new OutputStreamWriter(output);
            final FileReader input = new FileReader(copy.getTemplate().getFile())) {

            copyContext.put(VELOCITY_SFX_CALLBACK_TOKEN, VELOCITY_SFX_CALLBACK_CALL);

            Velocity.evaluate(copyContext, writer, "", input);

            writer.flush();
            output.flush();

            this.configuration.getDocument()
                    .getElementById(slide.getId())
                    .after(output.toString("UTF8"));

            /**
             * Insert the content
             */
            copy.getElements().values()
                    .stream()
                    .forEach(element -> this.configuration.getDocument()
                                                            .getElementById(element.getId())
                                                            .html(element.getHtmlContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * Add the slide to the presentation's slides
         */
        int index = this.configuration.getSlides().indexOf(slide);
        if(index != -1) {
            if(index == this.configuration.getSlides().size() - 1) {
                this.configuration.getSlides().add(copy);
            } else {
                this.configuration.getSlides().add(index + 1, copy);
            }
        }

        this.savePresentationFile();

        return copy;
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
    public void moveSlide(SlidePresentationConfiguration slideToMove, SlidePresentationConfiguration beforeSlide) {
        if(slideToMove == null) throw new IllegalArgumentException("The slideToMove to move can not be null");

        if(!slideToMove.equals(beforeSlide)) {
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

    public void savePresentationFile() {
        try(final Writer writer = new FileWriter(this.configuration.getPresentationFile())) {
            writer.write(this.configuration.getDocument().html());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

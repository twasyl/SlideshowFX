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

package com.twasyl.slideshowfx.builder;

import com.oracle.javafx.jmx.json.JSONDocument;
import com.oracle.javafx.jmx.json.JSONFactory;
import com.oracle.javafx.jmx.json.JSONWriter;
import com.twasyl.slideshowfx.builder.template.DynamicAttribute;
import com.twasyl.slideshowfx.builder.template.SlideTemplate;
import com.twasyl.slideshowfx.builder.template.Template;
import com.twasyl.slideshowfx.exceptions.InvalidPresentationConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateException;
import com.twasyl.slideshowfx.exceptions.PresentationException;
import com.twasyl.slideshowfx.utils.DOMUtils;
import com.twasyl.slideshowfx.utils.JSONHelper;
import com.twasyl.slideshowfx.utils.ZipUtils;
import javafx.embed.swing.SwingFXUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages all operations for templates and presentations. It is used to open them as well as add, update an
 * delete slides.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class PresentationBuilder {

    private static final Logger LOGGER = Logger.getLogger(PresentationBuilder.class.getName());
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

    private Template template;
    private Presentation presentation;
    private File templateArchiveFile;
    private File presentationArchiveFile;

    public PresentationBuilder() {
    }

    public PresentationBuilder(File template) {
        this.templateArchiveFile = template;
    }

    public File getTemplateArchiveFile() { return this.templateArchiveFile; }
    public void setTemplateArchiveFile(File template) { this.templateArchiveFile = template; }

    public File getPresentationArchiveFile() { return presentationArchiveFile; }
    public void setPresentationArchiveFile(File presentationArchiveFile) { this.presentationArchiveFile = presentationArchiveFile; }

    public Presentation getPresentation() { return this.presentation; }
    public void setPresentation(Presentation presentationFile) { this.presentation = presentationFile; }

    public Template getTemplate() { return template; }
    public void setTemplate(Template template) { this.template = template; }

    /**
     * Prepare the resources:
     * <ul>
     *     <li>Create an instance of Template</li>
     *     <li>Create the temporary folder</li>
     *     <li>Extract the data</li>
     *     <li>Load the template data</li>
     * </ul>
     */
    private void prepareResources(File dataArchive) throws InvalidTemplateException, InvalidTemplateConfigurationException {
        if(dataArchive == null) throw new IllegalArgumentException("Can not prepare the resources: the dataArchive is null");
        if(!dataArchive.exists()) throw new IllegalArgumentException("Can not prepare the resources: dataArchive does not exist");

        this.template = new Template();
        this.template.setContentDefinerMethod("setField");
        this.template.setFolder(new File(System.getProperty("java.io.tmpdir") + File.separator + "sfx-" + System.currentTimeMillis()));

        LOGGER.fine("The temporaryTemplateFolder is " + this.template.getFolder().getAbsolutePath());

        this.template.getFolder().deleteOnExit();

        // Unzip the template into a temporary folder
        LOGGER.fine("Extracting the template ...");

        try {
            ZipUtils.unzip(dataArchive, this.template.getFolder());
        } catch (IOException e) {
            throw new InvalidTemplateException("Error while trying to unzip the template", e);
        }

        // Read the configuration
        try {
            this.template.readFromFolder();
        } catch (IOException e) {
            throw new InvalidTemplateConfigurationException("Can not read template's configuration");
        }
    }

    /**
     * Load the current template defined by the templateArchiveFile attribute.
     * This creates a temporary file.
     */
    private void loadTemplate() throws InvalidTemplateException, InvalidTemplateConfigurationException {
        this.prepareResources(this.templateArchiveFile);

        // Copy the template to the presentation file
        LOGGER.fine("Creating presentation file");
        this.presentation = new Presentation();
        this.presentation.setSlides(new ArrayList<>());
        this.presentation.setPresentationFile(new File(this.template.getFolder(), Presentation.PRESENTATION_FILE_NAME));

        // Replacing the velocity tokens
        try(final InputStream inputStream = new FileInputStream(this.template.getFile());
            final Reader reader = new InputStreamReader(inputStream);
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final Writer writer = new OutputStreamWriter(outputStream)) {

            Velocity.init();
            VelocityContext context = new VelocityContext();
            context.put(VELOCITY_SFX_CONTENT_DEFINER_TOKEN, VELOCITY_SFX_CONTENT_DEFINER_SCRIPT);
            context.put(VELOCITY_SFX_CALLBACK_TOKEN, VELOCITY_SFX_CALLBACK_SCRIPT);

            Velocity.evaluate(context, writer, "", reader);

            writer.flush();
            outputStream.flush();

            this.presentation.setDocument(Jsoup.parse(outputStream.toString("UTF8")));

            this.saveTemporaryPresentation();

        } catch (FileNotFoundException e) {
            throw new InvalidTemplateException("The template file is not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the given template
     * @param template
     * @throws IOException
     */
    public void loadTemplate(File template) throws InvalidTemplateException, InvalidTemplateConfigurationException, PresentationException {
        setTemplateArchiveFile(template);
        this.loadTemplate();
    }

    /**
     * Open a saved presentation
     */
    private void openPresentation() throws InvalidPresentationConfigurationException {
        // Reading the slides' configuration
        LOGGER.fine("Parsing presentation configuration");

        JSONDocument presentationJson;
        try {
            presentationJson = JSONHelper.readFromFile(new File(this.template.getFolder(), Presentation.PRESENTATION_CONFIGURATION_NAME));
            presentationJson = presentationJson.get("presentation");
        } catch (IOException e) {
            throw new InvalidPresentationConfigurationException("Can not read presentation configuration file", e);
        }


        LOGGER.fine("Reading slides configuration");
        presentationJson.getList("slides")
                .stream()
                .map(slideJson -> (JSONDocument) slideJson)
                .forEach( slideJson -> {
                    final Slide slide = new Slide();

                    slide.setId(slideJson.getString("id"));
                    slide.setSlideNumber(slideJson.getString("number"));
                    slide.setTemplate(this.template.getSlideTemplate(slideJson.getNumber("template-id").intValue()));

                    try {
                        slide.setThumbnail(SwingFXUtils.toFXImage(ImageIO.read(new File(this.template.getSlidesThumbnailDirectory(), slide.getSlideNumber().concat(".png"))), null));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    slideJson.getList("elements")
                            .stream()
                            .map(slideElementJson -> (JSONDocument) slideElementJson)
                            .forEach(slideElementJson -> {
                                final SlideElement slideElement = new SlideElement();
                                slideElement.setId(slideElementJson.getString("element-id"));
                                slideElement.setOriginalContentCode(slideElementJson.getString("original-content-code"));
                                slideElement.setOriginalContentAsBase64(slideElementJson.getString("original-content"));
                                slideElement.setHtmlContentAsBase64(slideElementJson.getString("html-content"));

                                slide.getElements().put(slideElement.getId(), slideElement);
                            });

                    this.presentation.getSlides().add(slide);
        });

        // Append the slides' content to the presentation
        LOGGER.fine("Building presentation file");
        Velocity.init();
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_SFX_CALLBACK_TOKEN, VELOCITY_SFX_CALLBACK_CALL);
        context.put(VELOCITY_SLIDE_ID_PREFIX_TOKEN, this.template.getSlideIdPrefix());

        for(Slide s : this.presentation.getSlides()) {
            try (final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                 final Writer outputWriter = new BufferedWriter(new OutputStreamWriter(arrayOutputStream));
                 final FileReader slideReader = new FileReader(s.getTemplate().getFile())) {

                context.put(VELOCITY_SLIDE_NUMBER_TOKEN, s.getSlideNumber());
                Velocity.evaluate(context, outputWriter, "", slideReader);

                outputWriter.flush();
                arrayOutputStream.flush();

                this.presentation.getDocument()
                        .getElementById(this.template.getSlidesContainer())
                        .append(arrayOutputStream.toString("UTF8"));

                s.getElements().values()
                        .stream()
                        .forEach(element -> presentation.getDocument()
                                                        .getElementById(element.getId())
                                                        .html(element.getHtmlContent()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.saveTemporaryPresentation();

        LOGGER.fine("Presentation file created at " + this.presentation.getPresentationFile().getAbsolutePath());
    }

    /**
     * Open the given presentation. This methods first load the template and then the content of the presentation
     * @param presentation
     * @throws InvalidTemplateConfigurationException
     * @throws InvalidTemplateException
     * @throws PresentationException
     * @throws InvalidPresentationConfigurationException
     */
    public void openPresentation(File presentation) throws InvalidTemplateConfigurationException, InvalidTemplateException, PresentationException, InvalidPresentationConfigurationException {
        setTemplateArchiveFile(presentation);
        setPresentationArchiveFile(presentation);
        loadTemplate();
        openPresentation();
    }

    /**
     * Add a slide to the presentation and save the presentation
     * @param template
     * @throws IOException
     */
    public Slide addSlide(SlideTemplate template, String afterSlideNumber) throws IOException {
        if(template == null) throw new IllegalArgumentException("The template for creating a slide can not be null");
        Velocity.init();

        final Slide slide = new Slide(template, System.currentTimeMillis() + "");

        if(afterSlideNumber == null) {
            this.presentation.getSlides().add(slide);
        } else {
            ListIterator<Slide> slidesIterator = this.getPresentation().getSlides().listIterator();

            this.presentation.getSlideByNumber(afterSlideNumber);
            int index = -1;
            while(slidesIterator.hasNext()) {
                if(slidesIterator.next().getSlideNumber().equals(afterSlideNumber)) {
                    index = slidesIterator.nextIndex();
                    break;
                }
            }

            if(index > -1) {
                this.presentation.getSlides().add(index, slide);
            } else {
                this.presentation.getSlides().add(slide);
            }
        }


        final Reader slideFileReader = new FileReader(template.getFile());
        final ByteArrayOutputStream slideContentByte = new ByteArrayOutputStream();
        final Writer slideContentWriter = new OutputStreamWriter(slideContentByte);

        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_SLIDE_ID_PREFIX_TOKEN, this.template.getSlideIdPrefix());
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
            this.presentation.getDocument()
                    .getElementById(this.template.getSlidesContainer())
                    .append(htmlSlide.outerHtml());
        } else {
            this.presentation.getDocument()
                    .getElementById(this.presentation.getSlideByNumber(afterSlideNumber).getId())
                    .after(htmlSlide.outerHtml());
        }

        this.saveTemporaryPresentation();

        return slide;
    }

    /**
     * Delete the slide with the slideNumber and save the presentation
     * @param slideNumber
     */
    public void deleteSlide(String slideNumber) throws ParserConfigurationException {
        if(slideNumber == null) throw new IllegalArgumentException("Slide number can not be null");

        Slide slideToRemove = this.presentation.getSlideByNumber(slideNumber);
        if(slideToRemove != null) {
            this.presentation.getSlides().remove(slideToRemove);
            this.presentation.getDocument()
                    .getElementById(slideToRemove.getId()).remove();
        }

        this.saveTemporaryPresentation();
    }

    /**
     * Duplicates the given slide and add it to the presentation. The presentation is temporary saved.
     */
    public Slide duplicateSlide(Slide slide) {
        if(slide == null) throw new IllegalArgumentException("The slide to duplicate can not be null");

        final Slide copy = new Slide(slide.getTemplate(), System.currentTimeMillis() + "");
        copy.setThumbnail(slide.getThumbnail());
        copy.setId(slide.getId());

        // Copy the elements. Keep original IDs for now
        SlideElement copySlideElement;
        for(SlideElement slideElement : slide.getElements().values()) {
            copySlideElement = new SlideElement();
            copySlideElement.setId(slideElement.getId());
            copySlideElement.setOriginalContentCode(slideElement.getOriginalContentCode());
            copySlideElement.setOriginalContent(slideElement.getOriginalContent());
            copySlideElement.setHtmlContent(slideElement.getHtmlContent());

            copy.getElements().put(copySlideElement.getId(), copySlideElement);
        }

        // Apply the template engine for replacing dynamic elements
        final VelocityContext originalContext = new VelocityContext();
        originalContext.put(VELOCITY_SLIDE_ID_PREFIX_TOKEN, this.template.getSlideIdPrefix());
        originalContext.put(VELOCITY_SLIDE_NUMBER_TOKEN, slide.getSlideNumber());

        final VelocityContext copyContext = new VelocityContext();
        copyContext.put(VELOCITY_SLIDE_ID_PREFIX_TOKEN, this.template.getSlideIdPrefix());
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
            List<SlideElement> copySlideElements = new ArrayList<>();
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
            for(SlideElement copySE : copySlideElements) {
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

            this.presentation.getDocument()
                    .getElementById(slide.getId())
                    .after(output.toString("UTF8"));

            /**
             * Insert the content
             */
            copy.getElements().values()
                    .stream()
                    .forEach(element -> this.presentation.getDocument()
                                                            .getElementById(element.getId())
                                                            .html(element.getHtmlContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * Add the slide to the presentation's slides
         */
        int index = this.presentation.getSlides().indexOf(slide);
        if(index != -1) {
            if(index == this.presentation.getSlides().size() - 1) {
                this.presentation.getSlides().add(copy);
            } else {
                this.presentation.getSlides().add(index + 1, copy);
            }
        }

        this.saveTemporaryPresentation();

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
    public void moveSlide(Slide slideToMove, Slide beforeSlide) {
        if(slideToMove == null) throw new IllegalArgumentException("The slideToMove to move can not be null");

        if(!slideToMove.equals(beforeSlide)) {
            this.presentation.getSlides().remove(slideToMove);

            final String slideHtml = this.presentation.getDocument()
                    .getElementById(slideToMove.getId()).outerHtml();

            this.presentation.getDocument()
                    .getElementById(slideToMove.getId())
                    .remove();

            if(beforeSlide == null) {
                this.presentation.getSlides().add(slideToMove);
                this.presentation.getDocument()
                        .getElementById(this.template.getSlidesContainer())
                        .append(slideHtml);
            } else {
                int index = this.presentation.getSlides().indexOf(beforeSlide);
                this.presentation.getSlides().add(index, slideToMove);

                this.presentation.getDocument()
                        .getElementById(beforeSlide.getId())
                        .before(slideHtml);
            }

            this.saveTemporaryPresentation();
        }
    }

    public void saveTemporaryPresentation() {
        try(final Writer writer = new FileWriter(this.presentation.getPresentationFile())) {
            writer.write(this.presentation.getDocument().html());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build the final presentation file by writing the complete package
     */
    public void savePresentation(File presentationArchive) throws IOException {
        if(presentationArchive == null) throw new IllegalArgumentException("The presentation archive can not be null");

        LOGGER.fine("Creating the presentation configuration file");
        JSONWriter writer = JSONFactory.instance().makeWriter(new PrintWriter(new File(this.template.getFolder(), Presentation.PRESENTATION_CONFIGURATION_NAME)));

        writer.startObject()
              .startObject("presentation")
              .startArray("slides");

        this.presentation.getSlides()
                .stream()
                .forEach(slide -> {
                    try {
                        writer.startObject()
                              .objectValue("template-id", slide.getTemplate().getId())
                              .objectValue("id", slide.getId())
                              .objectValue("number", slide.getSlideNumber())
                              .startArray("elements");

                        slide.getElements().values()
                                .stream()
                                .forEach(slideElement -> {
                                    try {
                                        writer.startObject()
                                                .objectValue("element-id", slideElement.getId())
                                                .objectValue("original-content-code", slideElement.getOriginalContentCode())
                                                .objectValue("original-content", slideElement.getOriginalContentAsBase64())
                                                .objectValue("html-content", slideElement.getHtmlContentAsBase64())
                                                .endObject();
                                    } catch(IOException e) {
                                        LOGGER.log(Level.WARNING, "Can not create slide element", e);
                                    }
                                });
                        writer.endArray()
                              .endObject();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Can not add slide object to the configuration file", e);
                    }
                });


        writer.endArray() // Ends the slides array
              .endObject() // End the presentation document
              .endObject() // End the document
              .flush()
              .close();

        LOGGER.fine("Presentation configuration file created");

        LOGGER.fine("Create slides thumbnails");
        if(!this.template.getSlidesThumbnailDirectory().exists()) this.template.getSlidesThumbnailDirectory().mkdirs();
        else {
            Arrays.stream(this.template.getSlidesThumbnailDirectory()
                    .listFiles(file -> file.isFile()))
                    .forEach(slideFile -> slideFile.delete());
        }

        this.presentation.getSlides()
                .stream()
                .filter(slide -> slide != null)
                .forEach(slide -> {
                    LOGGER.fine("Creating thumbnail file: " + this.template.getSlidesThumbnailDirectory().getAbsolutePath() + File.separator + slide.getSlideNumber() + ".png");
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(slide.getThumbnail(), null), "png", new File(this.getTemplate().getSlidesThumbnailDirectory(), slide.getSlideNumber().concat(".png")));
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING,
                                String.format("Can not create thumbnail for slide number %1$s", slide.getSlideNumber()),
                                e);
                    }
                });

        LOGGER.fine("Compressing temporary file");
        ZipUtils.zip(this.template.getFolder(), presentationArchive);
        LOGGER.fine("Presentation saved");
    }
}

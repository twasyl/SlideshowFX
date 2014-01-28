package com.twasyl.slideshowfx.builder;

import com.twasyl.slideshowfx.builder.template.DynamicAttribute;
import com.twasyl.slideshowfx.builder.template.SlideTemplate;
import com.twasyl.slideshowfx.builder.template.Template;
import com.twasyl.slideshowfx.exceptions.InvalidPresentationConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateException;
import com.twasyl.slideshowfx.exceptions.PresentationException;
import com.twasyl.slideshowfx.utils.ZipUtils;
import javafx.embed.swing.SwingFXUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.json.*;
import javax.json.stream.JsonGenerator;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PresentationBuilder {

    private static final Logger LOGGER = Logger.getLogger(PresentationBuilder.class.getName());
    private static final String VELOCITY_SLIDE_NUMBER_TOKEN = "slideNumber";
    private static final String VELOCITY_SLIDES_TOKEN = "slides";
    private static final String VELOCITY_SFX_CALLBACK_TOKEN = "sfxCallback";
    private static final String VELOCITY_SFX_CONTENT_DEFINER_TOKEN = "sfxContentDefiner";
    private static final String VELOCITY_SLIDE_ID_PREFIX_TOKEN = "slideIdPrefix";

    private static final String VELOCITY_SFX_CONTENT_DEFINER_SCRIPT = "function setField(slide, what, value) {\n" +
            "\telement = document.getElementById(slide + \"-\" + what);\n" +
            "\telement.innerHTML = value;\n" +
            "}";
    private static final String VELOCITY_SFX_CALLBACK_SCRIPT = "function sendInformationToSlideshowFX(source) {\n" +
            "\tdashIndex = source.id.indexOf(\"-\");\n" +
            "\tslideNumber = source.id.substring(0, dashIndex);\n" +
            "\tfieldName = source.id.substring(dashIndex+1);\n" +
            "\n" +
            "\tsfx.prefillContentDefinition(slideNumber, fieldName, source.innerHTML);\n" +
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
        } catch (FileNotFoundException e) {
            throw new InvalidTemplateConfigurationException("Can not read template's configuration");
        }
    }

    /**
     * Load the current template defined by the templateArchiveFile attribute.
     * This creates a temporary file.
     */
    public void loadTemplate() throws InvalidTemplateException, InvalidTemplateConfigurationException, PresentationException {
        this.prepareResources(this.templateArchiveFile);

        // Copy the template to the presentation file
        LOGGER.fine("Creating presentation file");
        this.presentation = new Presentation();
        this.presentation.setSlides(new ArrayList<Slide>());
        this.presentation.setPresentationFile(new File(this.template.getFolder(), Presentation.PRESENTATION_FILE_NAME));

        // Replacing the velocity tokens
        final Reader templateReader;
        try {
            templateReader = new FileReader(this.template.getFile());
        } catch (FileNotFoundException e) {
            throw new InvalidTemplateException("The template file is not found");
        }

        final Writer presentationWriter;
        try {
            presentationWriter = new FileWriter(this.presentation.getPresentationFile());
        } catch (IOException e) {
            throw new PresentationException("Can not create the presentation temporary file", e);
        }

        Velocity.init();
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_SFX_CONTENT_DEFINER_TOKEN, VELOCITY_SFX_CONTENT_DEFINER_SCRIPT);
        context.put(VELOCITY_SFX_CALLBACK_TOKEN, VELOCITY_SFX_CALLBACK_SCRIPT);
        context.put(VELOCITY_SLIDES_TOKEN, "");

        Velocity.evaluate(context, presentationWriter, "", templateReader);

        try {
            presentationWriter.flush();
            presentationWriter.close();
        } catch (IOException e) {
            throw new PresentationException("Can not create the presentation temporary file", e);
        }

        try {
            templateReader.close();
        } catch (IOException e) {
            LOGGER.warning("Error while closing the template stream");
        }

        LOGGER.fine("Presentation file created at " + this.presentation.getPresentationFile().getAbsolutePath());
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
    public void openPresentation() throws InvalidTemplateException, InvalidTemplateConfigurationException, InvalidPresentationConfigurationException, PresentationException {
        this.prepareResources(this.templateArchiveFile);
        this.presentationArchiveFile = this.templateArchiveFile;

        // Copy the template to the presentation file
        LOGGER.fine("Creating presentation file");
        this.presentation = new Presentation();
        this.presentation.setSlides(new ArrayList<Slide>());
        this.presentation.setPresentationFile(new File(this.template.getFolder(), Presentation.PRESENTATION_FILE_NAME));

        // Reading the slides' configuration
        LOGGER.fine("Parsing presentation configuration");

        JsonReader reader = null;
        try {
            reader = Json.createReader(new FileInputStream(new File(this.template.getFolder(), Presentation.PRESENTATION_CONFIGURATION_NAME)));
        } catch (FileNotFoundException e) {
            throw new InvalidPresentationConfigurationException("Can not read presentation configuration file", e);
        }
        JsonObject presentationJson = reader.readObject().getJsonObject("presentation");
        JsonArray slidesJson = presentationJson.getJsonArray("slides");
        JsonObject slideJson;
        Slide slide;

        LOGGER.fine("Reading slides configuration");
        for(int index = 0; index < slidesJson.size(); index++)  {
            slide = new Slide();

            slideJson = slidesJson.getJsonObject(index);
            slide.setSlideNumber(slideJson.getString("number"));
            slide.setTemplate(this.template.getSlideTemplate(slideJson.getInt("template-id")));

            try {
                slide.setThumbnail(SwingFXUtils.toFXImage(ImageIO.read(new File(this.template.getSlidesThumbnailDirectory(), slide.getSlideNumber().concat(".png"))), null));
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.presentation.getSlides().add(slide);
        }

        reader.close();

        // Fill the slides' content
        LOGGER.fine("Reading slides files");
        FileInputStream slideInput = null;
        ByteArrayOutputStream slideContent = null;
        byte[] buffer = new byte[1024];
        int length;
        StringBuffer slidesBuffer = new StringBuffer();
        File slideFile;

        for(Slide s : this.presentation.getSlides()) {
            slideFile = new File(this.template.getSlidesPresentationDirectory(), s.getSlideNumber().concat(".html"));
            LOGGER.fine("Reading slide file: " + slideFile.getAbsolutePath());
            try {
                slideInput = new FileInputStream(slideFile);
                slideContent = new ByteArrayOutputStream();

                while((length = slideInput.read(buffer)) > 0) {
                    slideContent.write(buffer, 0, length);
                }
            } catch(IOException ex) {
                LOGGER.log(Level.WARNING, "Error while reading slide content", ex);
            } finally {
                if(slideContent != null) {
                    s.setText(new String(slideContent.toByteArray(), Charset.forName("UTF-8")));
                    try {
                        Slide.buildContent(slidesBuffer, s);
                    } catch (IOException | SAXException | ParserConfigurationException e) {
                        LOGGER.log(Level.WARNING, "Can not set slide's text", e);
                    }

                    try {
                        slideContent.close();
                    }
                    catch(IOException ex) {
                        LOGGER.log(Level.WARNING, "Can not close slide content stream");
                    }
                }
                if(slideInput != null) {
                    try {
                        slideInput.close();
                    } catch(IOException ex) {
                        LOGGER.log(Level.WARNING, "Can not close slide content file");
                    }
                }
            }

        }

        // Replacing the velocity tokens
        LOGGER.fine("Building presentation file");
        Reader templateReader = null;
        try {
            templateReader = new FileReader(this.template.getFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final Writer presentationWriter;
        try {
            presentationWriter = new FileWriter(this.presentation.getPresentationFile());
        } catch (IOException e) {
            throw new PresentationException("Can not create presentation temporary file", e);
        }

        Velocity.init();
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_SFX_CONTENT_DEFINER_TOKEN, VELOCITY_SFX_CONTENT_DEFINER_SCRIPT);
        context.put(VELOCITY_SFX_CALLBACK_TOKEN, VELOCITY_SFX_CALLBACK_SCRIPT);
        context.put(VELOCITY_SLIDES_TOKEN, slidesBuffer.toString());

        Velocity.evaluate(context, presentationWriter, "", templateReader);

        try {
            presentationWriter.flush();
            presentationWriter.close();
        } catch (IOException e) {
            throw new PresentationException("Can not create presentation temporary file", e);
        }

        try {
            templateReader.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error while closing template stream", e);
        }

        LOGGER.fine("Presentation file created at " + this.presentation.getPresentationFile().getAbsolutePath());
    }

    public void openPresentation(File presentation) throws InvalidTemplateConfigurationException, InvalidTemplateException, PresentationException, InvalidPresentationConfigurationException {
        setTemplateArchiveFile(presentation);
        openPresentation();
    }

    /**
     * Add a slide to the presentation and save the presentation
     * @param template
     * @throws IOException
     */
    public Slide addSlide(SlideTemplate template, String afterSlideNumber) throws IOException, ParserConfigurationException, SAXException {
        if(template == null) throw new IllegalArgumentException("The template for creating a slide can not be null");
        Velocity.init();

        final Slide slide = new Slide(template, System.currentTimeMillis() + "");

        if(afterSlideNumber == null) {
            this.presentation.getSlides().add(slide);
        } else {
            ListIterator<Slide> slidesIterator = this.getPresentation().getSlides().listIterator();

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

        if(!template.getDynamicAttributes().isEmpty()) {
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

        slide.setText(new String(slideContentByte.toByteArray()));

        this.saveTemporaryPresentation();

        return slide;
    }

    /**
     * Delete the slide with the slideNumber and save the presentation
     * @param slideNumber
     */
    public void deleteSlide(String slideNumber) throws IOException, SAXException, ParserConfigurationException {
        if(slideNumber == null) throw new IllegalArgumentException("Slide number can not be null");

        ListIterator<Slide> slidesIterator = this.presentation.getSlides().listIterator();

        while(slidesIterator.hasNext()) {
            if(slidesIterator.next().getSlideNumber().equals(slideNumber)) {
                slidesIterator.remove();
                break;
            }
        }

        this.saveTemporaryPresentation();
    }

    /**
     * Duplicates the given slide
     */
    public Slide duplicateSlide(Slide slide) {
        if(slide == null) throw new IllegalArgumentException("The slide to duplicate can not be null");

        final Slide copy = new Slide(slide.getTemplate(), System.currentTimeMillis() + "");
        copy.setText(slide.getText());
        copy.setThumbnail(slide.getThumbnail());

        // Apply the template engine for replacing dynamic elements
        final VelocityContext originalContext = new VelocityContext();
        originalContext.put(VELOCITY_SLIDE_ID_PREFIX_TOKEN, this.template.getSlideIdPrefix());
        originalContext.put(VELOCITY_SLIDE_NUMBER_TOKEN, slide.getSlideNumber());

        final VelocityContext copyContext = new VelocityContext();
        copyContext.put(VELOCITY_SLIDE_ID_PREFIX_TOKEN, this.template.getSlideIdPrefix());
        copyContext.put(VELOCITY_SLIDE_NUMBER_TOKEN, copy.getSlideNumber());

        ByteArrayOutputStream byteOutput = null;
        Writer writer = null;
        try {
            byteOutput = new ByteArrayOutputStream();
            writer = new OutputStreamWriter(byteOutput);

            String oldId, newId;

            /**
             * For each ID:
             * 1- Look for the original ID ; ie with the original slide number
             * 2- Replace each original ID by the ID of the new slide
             */
            for(String dynamicId : slide.getTemplate().getDynamicIds()) {
                Velocity.evaluate(originalContext, writer, "", dynamicId);
                writer.flush();

                oldId = new String(byteOutput.toByteArray());
                byteOutput.reset();

                if(copy.getText().contains(String.format("id=\"%1$s\"", oldId))) {
                    Velocity.evaluate(copyContext, writer, "", dynamicId);
                    writer.flush();

                    newId = new String(byteOutput.toByteArray());
                    byteOutput.reset();

                    copy.setText(copy.getText().replaceAll(String.format("id=\"%1$s\"", oldId), String.format("id=\"%1$s\"", newId)));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

        return copy;
    }

    public void saveTemporaryPresentation() throws ParserConfigurationException, SAXException, IOException {
        Velocity.init();

        // Saving the presentation file
        // Step 1: build the slides
        final StringBuffer slidesBuffer = new StringBuffer();
        for(Slide s : this.presentation.getSlides()) {
            Slide.buildContent(slidesBuffer, s);
        }

        // Step 2: rewrite from the template
        final Reader templateReader = new FileReader(this.template.getFile());
        final Writer presentationWriter = new FileWriter(this.presentation.getPresentationFile());

        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_SFX_CONTENT_DEFINER_TOKEN, VELOCITY_SFX_CONTENT_DEFINER_SCRIPT);
        context.put(VELOCITY_SFX_CALLBACK_TOKEN, VELOCITY_SFX_CALLBACK_SCRIPT);
        context.put(VELOCITY_SLIDES_TOKEN, slidesBuffer.toString());

        Velocity.evaluate(context, presentationWriter, "", templateReader);

        templateReader.close();
        presentationWriter.flush();
        presentationWriter.close();
    }

    /**
     * Build the final presentation file by writing the complete package
     */
    public void savePresentation(File presentationArchive) throws IOException {
        if(presentationArchive == null) throw new IllegalArgumentException("The presentation archive can not be null");

        LOGGER.fine("Creating the presentation configuration file");
        JsonArrayBuilder slidesJsonArray = Json.createArrayBuilder();
        JsonObject slideJson;

        for(Slide slide : this.presentation.getSlides()) {
            slidesJsonArray.add(
                    Json.createObjectBuilder()
                        .add("template-id", slide.getTemplate().getId())
                        .add("number", slide.getSlideNumber())
                        .build()
            );
        }

        JsonObject configuration = Json.createObjectBuilder()
                .add("presentation",
                        Json.createObjectBuilder()
                                .add("slides", slidesJsonArray.build()))
                .build();

        HashMap<String, Object> writerConfiguration = new HashMap<>();
        writerConfiguration.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriter writer = Json.createWriterFactory(writerConfiguration).createWriter(new FileOutputStream(new File(this.template.getFolder(), Presentation.PRESENTATION_CONFIGURATION_NAME)));
        writer.writeObject(configuration);

        LOGGER.fine("Presentation configuration file created");

        LOGGER.fine("Creating slides files");
        if(!this.template.getSlidesPresentationDirectory().exists()) this.template.getSlidesPresentationDirectory().mkdirs();
        else {
            for(File slideFile : this.template.getSlidesPresentationDirectory().listFiles()) {
                if(slideFile.isFile()) {
                    slideFile.delete();
                }
            }
        }

        PrintWriter slideWriter = null;
        for(Slide slide : this.presentation.getSlides()) {
            LOGGER.fine("Creating file: " + this.template.getSlidesPresentationDirectory().getAbsolutePath() + File.separator + slide.getSlideNumber() + ".html");
            slideWriter = new PrintWriter(new File(this.template.getSlidesPresentationDirectory(), slide.getSlideNumber() + ".html"));
            slideWriter.print(slide.getText());
            slideWriter.flush();
            slideWriter.close();
        }

        LOGGER.fine("Slides files created");

        LOGGER.fine("Create slides thumbnails");
        if(!this.template.getSlidesThumbnailDirectory().exists()) this.template.getSlidesThumbnailDirectory().mkdirs();
        else {
            for(File slideFile : this.template.getSlidesThumbnailDirectory().listFiles()) {
                if(slideFile.isFile()) {
                    slideFile.delete();
                }
            }
        }

        for(Slide slide : this.presentation.getSlides()) {
            LOGGER.fine("Creating thumbnail file: " + this.template.getSlidesThumbnailDirectory().getAbsolutePath() + File.separator + slide.getSlideNumber() + ".png");

            if(slide.getThumbnail() != null)
                ImageIO.write(SwingFXUtils.fromFXImage(slide.getThumbnail(), null), "png", new File(this.getTemplate().getSlidesThumbnailDirectory(), slide.getSlideNumber().concat(".png")));
        }

        LOGGER.fine("Compressing temporary file");
        ZipUtils.zip(this.template.getFolder(), presentationArchive);
        LOGGER.fine("Presentation saved");
    }
}

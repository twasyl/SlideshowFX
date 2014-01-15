package com.twasyl.slideshowfx.utils;

import com.twasyl.slideshowfx.chat.Chat;
import com.twasyl.slideshowfx.exceptions.InvalidPresentationConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateException;
import com.twasyl.slideshowfx.exceptions.PresentationException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.json.*;
import javax.json.stream.JsonGenerator;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PresentationBuilder {

    /**
     * Represents a slide defined by a template
     */
    public static class Slide {
        private int id;
        private String slideNumber;
        private String name;
        private File file;
        private String text;
        private Image thumbnail;
        private String[] dynamicIds;

        public Slide() {
        }

        public Slide(int id, String name, File file) {
            this.id = id;
            this.name = name;
            this.file = file;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public File getFile() { return file; }
        public void setFile(File file) { this.file = file; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getSlideNumber() { return slideNumber; }
        public void setSlideNumber(String slideNumber) { this.slideNumber = slideNumber; }

        public Image getThumbnail() { return thumbnail; }
        public void setThumbnail(Image thumbnail) { this.thumbnail = thumbnail; }

        public String[] getDynamicIds() { return dynamicIds; }
        public void setDynamicIds(String[] dynamicIds) { this.dynamicIds = dynamicIds; }

        public static void buildContent(StringBuffer buffer, Slide slide) throws IOException, SAXException, ParserConfigurationException {
            buffer.append(slide.getText());
        }
    }

    /**
     * Represents the template found in the template configuration file
     */
    public static class Template {
        protected static final String TEMPLATE_CONFIGURATION_NAME = "template-config.json";

        private File folder;
        private File configurationFile;
        private String name;
        private File file;
        private List<Slide> slides;
        private String contentDefinerMethod;
        private String getCurrentSlideMethod;
        private String jsObject;
        private File slidesTemplateDirectory;
        private File slidesPresentationDirectory;
        private File slidesThumbnailDirectory;
        private File resourcesDirectory;
        private String slideIdPrefix;

        public Template() {
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public File getFile() { return file;  }
        public void setFile(File file) { this.file = file; }

        public List<Slide> getSlides() { return slides; }
        public void setSlides(List<Slide> slides) { this.slides = slides; }

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
            this.setSlides(new ArrayList<Slide>());
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

                JsonArray slidesDefinition = slidesJson.getJsonArray("slides-definition");

                if(slidesDefinition != null && !slidesDefinition.isEmpty()) {
                    Slide slide;
                    JsonArray dynamicIdsJson;

                    for (JsonObject slideJson : slidesDefinition.getValuesAs(JsonObject.class)) {
                        slide = new Slide();
                        slide.setId(slideJson.getInt("id"));
                        LOGGER.fine("[Slide definition] id = " + slide.getId());

                        slide.setName(slideJson.getString("name"));
                        LOGGER.fine("[Slide definition] name = " + slide.getName());

                        slide.setFile(new File(this.getSlidesTemplateDirectory(), slideJson.getString("file")));
                        LOGGER.fine("[Slide definition] file = " + slide.getFile().getAbsolutePath());

                        dynamicIdsJson = slideJson.getJsonArray("dynamic-ids");
                        if(dynamicIdsJson != null && !dynamicIdsJson.isEmpty()) {
                            slide.setDynamicIds(new String[dynamicIdsJson.size()]);

                            for(int index = 0; index < dynamicIdsJson.size(); index++) {
                                slide.getDynamicIds()[index] = dynamicIdsJson.getString(index);
                            }
                        }

                        this.getSlides().add(slide);
                    }
                } else {
                    LOGGER.fine("No slide's definition found");
                }
            } else {
                LOGGER.fine("No slide's configuration found");
            }
        }
    }

    /**
     * Represents a presentation
     */
    public static class Presentation {
        protected static final String PRESENTATION_CONFIGURATION_NAME = "presentation-config.json";
        protected static final String PRESENTATION_FILE_NAME = "presentation.html";

        private File presentationFile;
        private List<Slide> slides;

        public File getPresentationFile() { return presentationFile; }
        public void setPresentationFile(File presentationFile) { this.presentationFile = presentationFile; }

        public List<Slide> getSlides() { return slides; }
        public void setSlides(List<Slide> slides) { this.slides = slides; }

        public void updateSlideText(String slideNumber, String content) {
            if(slideNumber == null) throw new IllegalArgumentException("The slide number can not be null");

            Slide slideToUpdate = null;
            for (Slide s : getSlides()) {
                if (slideNumber.equals(s.getSlideNumber())) {
                    s.setText(content);
                    LOGGER.finest("Slide's text updated");
                    break;
                }
            }
        }

        public void updateSlideThumbnail(String slideNumber, Image image) {
            if(slideNumber == null) throw new IllegalArgumentException("The slide number can not be null");

            Slide slideToUpdate = null;
            for (Slide s : getSlides()) {
                if (slideNumber.equals(s.getSlideNumber())) {
                    s.setThumbnail(image);
                    LOGGER.finest("Slide's thumbnail updated");
                    break;
                }
            }
        }

        public Slide getSlide(String slideNumber) {
            Slide slide = null;

            for(Slide s : slides) {
                if(s.getSlideNumber().equals(slideNumber)) {
                    slide = s;
                    break;
                }
            }

            return slide;
        }
    }

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
            slide.setId(slideJson.getInt("template-id"));
            slide.setFile(new File(this.template.getSlidesPresentationDirectory(), slideJson.getString("file")));

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

        for(Slide s : this.presentation.getSlides()) {
            LOGGER.fine("Reading slide file: " + s.getFile().getAbsolutePath());
            try {
                slideInput = new FileInputStream(s.getFile());
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
    public Slide addSlide(Slide template, String afterSlideNumber) throws IOException, ParserConfigurationException, SAXException {
        if(template == null) throw new IllegalArgumentException("The template for creating a slide can not be null");
        Velocity.init();

        final Slide slide = new Slide(template.getId(), template.getName(), template.getFile());
        slide.setSlideNumber(template.getSlideNumber());

        slide.setSlideNumber(System.currentTimeMillis() + "");

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


        final Reader slideFileReader = new FileReader(slide.getFile());
        final ByteArrayOutputStream slideContentByte = new ByteArrayOutputStream();
        final Writer slideContentWriter = new OutputStreamWriter(slideContentByte);

        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_SLIDE_ID_PREFIX_TOKEN, this.template.getSlideIdPrefix());
        context.put(VELOCITY_SLIDE_NUMBER_TOKEN, slide.getSlideNumber());
        context.put(VELOCITY_SFX_CALLBACK_TOKEN, VELOCITY_SFX_CALLBACK_CALL);

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

        Slide copy =  new Slide();
        copy.setName(slide.getName());
        copy.setId(slide.getId());
        copy.setSlideNumber(System.currentTimeMillis() + "");
        copy.setText(slide.getText());
        copy.setFile(slide.getFile());
        copy.setThumbnail(slide.getThumbnail());

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
                        .add("template-id", slide.getId())
                        .add("number", slide.getSlideNumber())
                        .add("file", slide.getSlideNumber() + ".html")
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

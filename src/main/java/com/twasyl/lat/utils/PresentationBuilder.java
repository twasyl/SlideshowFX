package com.twasyl.lat.utils;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PresentationBuilder {

    /**
     * Represents a slide defined by a template
     */
    public static class Slide {
        private int id;
        private String name;
        private File file;
        private String content;

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

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    /**
     * Represents the template found in the template configuration file
     */
    public static class Template {
        protected static final String TEMPLATE_CONFIGURATION_NAME = "template-config.xml";

        private File folder;
        private String name;
        private File file;
        private List<Slide> slides;

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
    }

    /**
     * Represents a presentation
     */
    public static class Presentation {
        protected static final String PRESENTATION_FILE_NAME = "presentation.html";

        private File presentationFile;
        private List<Slide> slides;

        public File getPresentationFile() { return presentationFile; }
        public void setPresentationFile(File presentationFile) { this.presentationFile = presentationFile; }

        public List<Slide> getSlides() { return slides; }
        public void setSlides(List<Slide> slides) { this.slides = slides; }
    }

    private static final Logger LOGGER = Logger.getLogger(PresentationBuilder.class.getName());
    private static final String VELOCITY_SLIDE_NUMBER_TOKEN = "${slideNumber}";
    private static final String VELOCITY_SLIDES_TOKEN = "${slides}";

    private Template template;
    private Presentation presentation;
    private File templateArchiveFile;


    public PresentationBuilder() {
        Velocity.init();
    }

    public PresentationBuilder(File template) {
        this();
        this.templateArchiveFile = template;
    }

    public File getTemplateArchiveFile() { return this.templateArchiveFile; }
    public void setTemplateArchiveFile(File template) { this.templateArchiveFile = template; }

    public Presentation getPresentation() { return this.presentation; }
    public void setPresentation(Presentation presentationFile) { this.presentation = presentationFile; }

    public Template getTemplate() { return template; }
    public void setTemplate(Template template) { this.template = template; }

    /**
     * Load the current template defined by the templateArchiveFile attribute.
     * This creates a temporary file.
     */
    public void loadTemplate() throws IOException {
        if(this.templateArchiveFile == null) throw new IllegalArgumentException("Can not load the template: file is null");
        if(!this.templateArchiveFile.exists()) throw new IllegalArgumentException("Can not load the template: file does not exist");

        this.template = new Template();

        ZipInputStream zipReader = new ZipInputStream(new FileInputStream(this.templateArchiveFile));
        ZipEntry zipEntry;
        File extractedFile;
        FileOutputStream extractedFileOutputStream;

        this.template.setFolder(new File(System.getProperty("java.io.tmpdir") + File.separator + "ifx-" + System.currentTimeMillis()));
        LOGGER.fine("The temporaryTemplateFolder is " + this.template.getFolder().getAbsolutePath());

        this.template.getFolder().mkdir();
        this.template.getFolder().deleteOnExit();

        // Unzip the template into a temporary folder
        LOGGER.fine("The temporaryTemplateFolder has been created: " + this.template.getFolder().exists());
        LOGGER.fine("Extracting the template ...");

        while((zipEntry = zipReader.getNextEntry()) != null) {
            extractedFile = new File(this.template.getFolder(), zipEntry.getName());

            LOGGER.fine("Extracting file: " + extractedFile.getAbsolutePath());

            if(zipEntry.isDirectory()) extractedFile.mkdirs();
            else {
                // Ensure to create the parents directories
                extractedFile.getParentFile().mkdirs();

                int length;
                byte[] buffer = new byte[1024];

                extractedFileOutputStream = new FileOutputStream(extractedFile);

                while((length = zipReader.read(buffer)) > 0) {
                    extractedFileOutputStream.write(buffer, 0, length);
                }

                extractedFileOutputStream.flush();
                extractedFileOutputStream.close();
            }
        }

        LOGGER.fine("Extraction done");

        zipReader.closeEntry();
        zipReader.close();

        // Read the configuration
        // Set the template information
        LOGGER.fine("Starting reading template configuration");
        this.templateConfigurationFile = new File(this.template.getFolder(), Template.TEMPLATE_CONFIGURATION_NAME);

        XPath xpath = XPathFactory.newInstance().newXPath();
        InputSource configurationFileInput = new InputSource(this.templateConfigurationFile.getAbsolutePath());
        String expression = "/impress-fx/template/name";

        try {
            this.template.setName(xpath.evaluate(expression, configurationFileInput));
            LOGGER.fine("[Template configuration] name = " + this.template.getName());

            expression = "/impress-fx/template/file";
            this.template.setFile(new File(this.template.getFolder(), xpath.evaluate(expression, configurationFileInput)));
            LOGGER.fine("[Template configuration] file = " + this.template.getFile().getAbsolutePath());

            // Setting the slides
            this.template.setSlides(new ArrayList<Slide>());

            LOGGER.fine("Reading slide's configuration");

            expression = "/impress-fx/slides/directory";
            File slidesDirectory = new File(this.template.getFolder(), xpath.evaluate(expression, configurationFileInput));
            LOGGER.fine("[Slide's configuration] directory = " + slidesDirectory.getAbsolutePath());

            expression = "/impress-fx/slides/slide()";
            NodeList slidesXpath = null;

            slidesXpath = (NodeList) xpath.evaluate(expression, configurationFileInput, XPathConstants.NODESET);

            if(slidesXpath != null && slidesXpath.getLength() > 0) {
                Node slideNode;
                Slide slide;

                for(int index = 0; index < slidesXpath.getLength(); index++) {
                    slideNode = slidesXpath.item(index);
                    slide = new Slide();

                    expression = "id";
                    slide.setId(((Number) xpath.evaluate(expression, slideNode, XPathConstants.NUMBER)).intValue());
                    LOGGER.fine("[Slide configuration] id = " + slide.getId());

                    expression = "name";
                    slide.setName(xpath.evaluate(expression, slideNode));
                    LOGGER.fine("[Slide configuration] name = " + slide.getName());

                    expression = "file";
                    slide.setFile(new File(slidesDirectory, xpath.evaluate(expression, slideNode)));
                    LOGGER.fine("[Slide configuration] file = " + slide.getFile().getAbsolutePath());

                    this.template.getSlides().add(slide);
                }
            } else {
                LOGGER.fine("No slide's configurationfound");
            }
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.WARNING, "Error parsing the template configuration", e);
            e.printStackTrace();
        }

        // Copy the template to the presentation file
        LOGGER.fine("Creating presentation file");
        this.presentation = new Presentation();
        this.presentation.setPresentationFile(new File(this.template.getFolder(), Presentation.PRESENTATION_FILE_NAME));

        FileOutputStream presentationFileOutput = new FileOutputStream(this.presentation.getPresentationFile());
        FileInputStream templateFileInput = new FileInputStream(this.template.getFile());
        byte[] buffer = new byte[1024];
        int length;

        while((length = templateFileInput.read(buffer)) > 0) {
            presentationFileOutput.write(buffer, 0, length);
        }

        presentationFileOutput.flush();
        presentationFileOutput.close();

        templateFileInput.close();

        LOGGER.fine("Presentation file created at " + this.presentation.getPresentationFile().getAbsolutePath());
    }

    public void loadTemplate(File template) throws IOException {
        setTemplateArchiveFile(template);
        this.loadTemplate();
    }

    /**
     * Add a slide to the presentation and save the presentation
     * @param template
     * @throws IOException
     */
    public void addSlide(Slide template) throws IOException {
        if(template == null) throw new IllegalArgumentException("The template for creating a slide can not be null");

        Slide slide = new Slide(template.getId(), template.getName(), template.getFile());

        Reader slideFileReader = new FileReader(slide.getFile());
        ByteArrayOutputStream slideContentByte = new ByteArrayOutputStream();
        Writer slideContentWriter = new OutputStreamWriter(slideContentByte);

        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_SLIDE_NUMBER_TOKEN, this.presentation.getSlides().size() + 1);

        Velocity.evaluate(context, slideContentWriter, "", slideFileReader);
        slideContentWriter.flush();

        slide.setContent(new String(slideContentByte.toByteArray()));

        this.presentation.getSlides().add(slide);

        // Saving the presentation file
        // Step 1: build the slides
        StringBuffer slidesBuffer = new StringBuffer();
        for(Slide s : this.presentation.getSlides()) {
            slidesBuffer.append(s.getContent());
        }

        // Step 2: rewrite from the template
    }

    /**
     * Build the final presentation file by writing the complete package
     */
    public void buildPresentation() {
       // if(this.presentationFile == null) throw new IllegalArgumentException("The presentation file can not be null");
    }
}

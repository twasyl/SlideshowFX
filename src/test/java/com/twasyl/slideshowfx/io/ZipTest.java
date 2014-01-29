package com.twasyl.slideshowfx.io;

import com.twasyl.slideshowfx.exceptions.InvalidTemplateConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateException;
import com.twasyl.slideshowfx.exceptions.PresentationException;
import com.twasyl.slideshowfx.builder.PresentationBuilder;
import com.twasyl.slideshowfx.utils.ZipUtils;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZipTest {

    private static final Logger LOGGER = Logger.getLogger(ZipTest.class.getName());

    private static File revealjsTemplateFile;
    private static File reveljsTemplateFolder;

    private static File impressjsTemplateFile;
    private static File impressjsTemplateFolder;

    private static File bespokejsTemplateFile;
    private static File bespokejsTemplateFolder;

    private static File presentationExampleFolder;
    private static File presentationExampleFile;

    private static PresentationBuilder builder;

    @BeforeClass
    public static void beforeClass() {

        reveljsTemplateFolder = new File("./src/test/resources/revealjs-template");
        revealjsTemplateFile = new File("./src/test/resources/revealjs-template.sfxt");

        impressjsTemplateFolder = new File("./src/test/resources/impress.js-template");
        impressjsTemplateFile = new File("./src/test/resources/impress.js-template.sfxt");

        bespokejsTemplateFolder = new File("./src/test/resources/bespoke.js-template");
        bespokejsTemplateFile = new File("./src/test/resources/bespoke.js-template.sfxt");

        presentationExampleFolder = new File("./src/test/resources/iCode");
        presentationExampleFile = new File("./src/test/resources/iCode.sfx");

        LOGGER.fine("Template folder: " + reveljsTemplateFolder.getAbsolutePath());
        LOGGER.fine("Template file: " + revealjsTemplateFile.getAbsolutePath());

        builder = new PresentationBuilder();
    }

    @AfterClass
    public static void afterClass() {

    }

    @Test
    public void unzipRevealjsTemplate() throws IOException {
        try {
            builder.loadTemplate(revealjsTemplateFile);
        } catch (InvalidTemplateException | InvalidTemplateConfigurationException | PresentationException e) {
           LOGGER.log(Level.SEVERE, "Error while unzipping template", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void zipRevealjsTemplateFolder() throws IOException {
        ZipUtils.zip(reveljsTemplateFolder, revealjsTemplateFile);
    }

    @Test
    public void unzipImpressjsTemplate() throws IOException {
        try {
            builder.loadTemplate(impressjsTemplateFile);
        } catch (InvalidTemplateException | InvalidTemplateConfigurationException | PresentationException e) {
            LOGGER.log(Level.SEVERE, "Error while unzipping template", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void zipImpressjsTemplateFolder() throws IOException {
        ZipUtils.zip(impressjsTemplateFolder, impressjsTemplateFile);
    }

    @Test
    public void unzipBespokejsTemplate() throws IOException {
        try {
            builder.loadTemplate(bespokejsTemplateFile);
        } catch (InvalidTemplateException | InvalidTemplateConfigurationException | PresentationException e) {
            LOGGER.log(Level.SEVERE, "Error while unzipping template", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void zipBespokejsTemplateFolder() throws IOException {
        ZipUtils.zip(bespokejsTemplateFolder, bespokejsTemplateFile);
    }

    @Test
    public void zipPresentationExample() throws IOException {
        ZipUtils.zip(presentationExampleFolder, presentationExampleFile);
    }
}

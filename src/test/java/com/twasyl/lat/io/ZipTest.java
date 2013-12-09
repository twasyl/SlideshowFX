package com.twasyl.lat.io;

import com.twasyl.lat.utils.PresentationBuilder;
import com.twasyl.lat.utils.ZipUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ZipTest {

    private static final Logger LOGGER = Logger.getLogger(ZipTest.class.getName());

    private static File templateFile;
    private static File templateFolder;
    private static PresentationBuilder builder;

    @BeforeClass
    public static void beforeClass() {

        templateFolder = new File("./src/test/resources/template");
        templateFile = new File("./src/test/resources/template.sfxt");

        LOGGER.fine("Template folder: " + templateFolder.getAbsolutePath());
        LOGGER.fine("Template file: " + templateFile.getAbsolutePath());

       // Assert.assertTrue(templateFile.exists());

        builder = new PresentationBuilder();
    }

    @AfterClass
    public static void afterClass() {

    }

    @Test
    public void unzipTemplate() throws IOException {
        builder.loadTemplate(templateFile);
    }

    @Test
    public void zipTemplateFolder() throws IOException {
        ZipUtils.zip(templateFolder, templateFile);
    }
}

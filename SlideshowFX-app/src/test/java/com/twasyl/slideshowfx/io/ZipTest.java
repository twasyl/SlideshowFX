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

package com.twasyl.slideshowfx.io;

import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.utils.ZipUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

    private static TemplateEngine templateEngine;

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

        LOGGER.fine("TemplateConfiguration folder: " + reveljsTemplateFolder.getAbsolutePath());
        LOGGER.fine("TemplateConfiguration file: " + revealjsTemplateFile.getAbsolutePath());

        templateEngine = new TemplateEngine();
    }

    @AfterClass
    public static void afterClass() {

    }

    @Test(enabled = false)
    public void unzipRevealjsTemplate() throws IOException {
        try {
            templateEngine.loadArchive(revealjsTemplateFile);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Error while unzipping template", e);
            Assert.fail();
        }
    }

    @Test(enabled = false)
    public void zipRevealjsTemplateFolder() throws IOException {
        ZipUtils.zip(reveljsTemplateFolder, revealjsTemplateFile);
    }

    @Test(enabled = false)
    public void unzipImpressjsTemplate() throws IOException {
        try {
            templateEngine.loadArchive(impressjsTemplateFile);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Error while unzipping template", e);
            Assert.fail();
        }
    }

    @Test(enabled = false)
    public void zipImpressjsTemplateFolder() throws IOException {
        ZipUtils.zip(impressjsTemplateFolder, impressjsTemplateFile);
    }

    @Test(enabled = false)
    public void unzipBespokejsTemplate() throws IOException {
        try {
            templateEngine.loadArchive(bespokejsTemplateFile);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Error while unzipping template", e);
            Assert.fail();
        }
    }

    @Test(enabled = false)
    public void zipBespokejsTemplateFolder() throws IOException {
        ZipUtils.zip(bespokejsTemplateFolder, bespokejsTemplateFile);
    }

    @Test(enabled = false)
    public void zipPresentationExample() throws IOException {
        ZipUtils.zip(presentationExampleFolder, presentationExampleFile);
    }
}

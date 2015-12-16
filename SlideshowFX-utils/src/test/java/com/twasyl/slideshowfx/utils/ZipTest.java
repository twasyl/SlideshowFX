/*
 * Copyright 2015 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.utils;

import com.twasyl.slideshowfx.utils.io.DeleteFileVisitor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.*;

public class ZipTest {

    private static final Logger LOGGER = Logger.getLogger(ZipTest.class.getName());

    private static File testResultsDir;
    private static File resourcesDir;

    @BeforeClass
    public static void beforeClass() {

        testResultsDir = new File(System.getProperty("testResultsDir", "build"));
        resourcesDir = new File("src/test/resources/com/twasyl/slideshowfx/utils/zip");

        assertTrue(resourcesDir.exists());
    }

    @Test
    public void zip() throws IOException {
        final File zip = new File(testResultsDir, "zipped.zip");
        ZipUtils.zip(resourcesDir, zip);

        // Verify all content of the zip
        try(final FileInputStream fileInput = new FileInputStream(zip);
            final ZipInputStream input = new ZipInputStream(fileInput)) {

            final int expectedNumberOfEntries = 5;
            int numberOfEntries = 0;

            ZipEntry entry;

            while((entry = input.getNextEntry()) != null) {
                numberOfEntries++;

                switch(entry.getName()) {
                    case "dir/otherDir/otherDirTest.html":
                        assertFalse("otherDirTest.html is a directory", entry.isDirectory());
                        break;
                    case "dir/dirTest.txt":
                        assertFalse("dirTest.txt is a directory", entry.isDirectory());
                        break;
                    case "archive.zip":
                        assertFalse("archive.zip is a directory", entry.isDirectory());
                        break;
                    case "test.html":
                        assertFalse("test.html is a directory", entry.isDirectory());
                        break;
                    case "test.txt":
                        assertFalse("test.txt is a directory", entry.isDirectory());
                        break;
                    default:
                        fail("Unknown entry name: [" + entry.getName() + "]");
                }
            }

            assertEquals(expectedNumberOfEntries, numberOfEntries);
        }

        zip.delete();
    }

    @Test public void unzip() throws IOException {
        final File zip = new File(resourcesDir, "archive.zip");
        final File unzippedFolder = new File(testResultsDir, "unzipped").toPath().toAbsolutePath().toFile();

        if(!unzippedFolder.exists()) unzippedFolder.mkdir();

        ZipUtils.unzip(zip, unzippedFolder);

        File unzippedFile = Paths.get(unzippedFolder.getAbsolutePath(), "dir").toFile();
        assertTrue(unzippedFile.exists());
        assertTrue(unzippedFile.isDirectory());

        unzippedFile = Paths.get(unzippedFolder.getAbsolutePath(), "dir", "otherDir").toFile();
        assertTrue(unzippedFile.exists());
        assertTrue(unzippedFile.isDirectory());

        unzippedFile = Paths.get(unzippedFolder.getAbsolutePath(), "dir", "otherDir", "otherDirTest.html").toFile();
        assertTrue(unzippedFile.exists());
        assertTrue(unzippedFile.isFile());

        unzippedFile = Paths.get(unzippedFolder.getAbsolutePath(), "dir", "dirTest.txt").toFile();
        assertTrue(unzippedFile.exists());
        assertTrue(unzippedFile.isFile());

        unzippedFile = Paths.get(unzippedFolder.getAbsolutePath(), "test.html").toFile();
        assertTrue(unzippedFile.exists());
        assertTrue(unzippedFile.isFile());

        unzippedFile = Paths.get(unzippedFolder.getAbsolutePath(), "test.txt").toFile();
        assertTrue(unzippedFile.exists());
        assertTrue(unzippedFile.isFile());

        Files.walkFileTree(unzippedFolder.toPath(), new DeleteFileVisitor());
    }
}

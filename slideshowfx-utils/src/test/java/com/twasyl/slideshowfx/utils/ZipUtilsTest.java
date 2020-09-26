package com.twasyl.slideshowfx.utils;

import com.twasyl.slideshowfx.utils.io.DeleteFileVisitor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

public class ZipUtilsTest {

    private static final Logger LOGGER = Logger.getLogger(ZipUtilsTest.class.getName());

    private static File testResultsDir;
    private static File resourcesDir;

    @BeforeAll
    public static void beforeClass() throws URISyntaxException {

        testResultsDir = new File("build/tmp");
        resourcesDir = new File("src/test/resources/com/twasyl/slideshowfx/utils/zip");

        assertTrue(resourcesDir.exists());
    }

    @Test
    public void zip() throws IOException {
        final File zip = new File(testResultsDir, "zipped.zip");
        ZipUtils.zip(resourcesDir, zip);

        // Verify all content of the zip
        try (final FileInputStream fileInput = new FileInputStream(zip);
             final ZipInputStream input = new ZipInputStream(fileInput)) {

            final int expectedNumberOfEntries = 5;
            int numberOfEntries = 0;

            ZipEntry entry;

            while ((entry = input.getNextEntry()) != null) {
                numberOfEntries++;

                switch (entry.getName()) {
                    case "dir/otherDir/otherDirTest.html":
                        assertFalse(entry.isDirectory(), "otherDirTest.html is a directory");
                        break;
                    case "dir/dirTest.txt":
                        assertFalse(entry.isDirectory(), "dirTest.txt is a directory");
                        break;
                    case "archive.zip":
                        assertFalse(entry.isDirectory(), "archive.zip is a directory");
                        break;
                    case "test.html":
                        assertFalse(entry.isDirectory(), "test.html is a directory");
                        break;
                    case "test.txt":
                        assertFalse(entry.isDirectory(), "test.txt is a directory");
                        break;
                    default:
                        fail("Unknown entry name: [" + entry.getName() + "]");
                }
            }

            assertEquals(expectedNumberOfEntries, numberOfEntries);
        }

        zip.delete();
    }

    @Test
    public void unzip() throws IOException {
        final File zip = new File(resourcesDir, "archive.zip");
        final File unzippedFolder = new File(testResultsDir, "unzipped").toPath().toAbsolutePath().toFile();

        if (!unzippedFolder.exists()) unzippedFolder.mkdir();

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

    @Test
    public void zipSlipVulnerabilityManaged() throws IOException {
        final File zipFile = new File(testResultsDir, "zipSlipVulnerability.zip");
        zipFile.deleteOnExit();

        try (final FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
             final ZipOutputStream outputStream = new ZipOutputStream(fileOutputStream)) {
            outputStream.putNextEntry(new ZipEntry("../zipSlipFile.txt"));
            outputStream.write("Zip Slip vulnerability !!!".getBytes(UTF_8));
            outputStream.closeEntry();
            outputStream.flush();
        }

        final File unzippedFolder = new File(testResultsDir, "zipSlipVulnerability").toPath().toAbsolutePath().toFile();
        if (!unzippedFolder.exists()) unzippedFolder.mkdir();
        ZipUtils.unzip(zipFile, unzippedFolder);

        final File unzippedFile = Paths.get(testResultsDir.getAbsolutePath(), "zipSlipFile.txt").toFile();
        assertFalse(unzippedFile.exists());

        Files.walkFileTree(unzippedFolder.toPath(), new DeleteFileVisitor());
    }
}

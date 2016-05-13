package com.twasyl.slideshowfx.utils;

import com.twasyl.slideshowfx.utils.io.DeleteFileVisitor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.*;

public class ZipUtilsTest {

    private static final Logger LOGGER = Logger.getLogger(ZipUtilsTest.class.getName());

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

    @Test public void testListFilesForDirectory() throws IOException {

        final FileVisitor<File> visitor = new SimpleFileVisitor<File>() {
            @Override
            public FileVisitResult preVisitDirectory(File dir, BasicFileAttributes attrs) throws IOException {
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(File file, BasicFileAttributes attrs) throws IOException {
                if(!file.isDirectory()) {

                }
                return super.visitFile(file, attrs);
            }


        };

        final File dir = new File(System.getProperty("user.home"), "Images");

        final DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toPath());
        stream.forEach(System.out::println);
    }
}

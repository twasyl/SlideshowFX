package com.twasyl.slideshowfx.utils;

import com.twasyl.slideshowfx.utils.io.ListFilesFileVisitor;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;

/**
 * This class provides utility methods for working with Zip files.
 *
 * @author Thierry Wasylczenko
 */
public class ZipUtils {

    private static final Logger LOGGER = Logger.getLogger(ZipUtils.class.getName());

    private ZipUtils() {
    }

    /**
     * Unzip the given archive into the provided destination. If the destination does not exist it is created.
     *
     * @param archive     The archive file to unzip.
     * @param destination The destination directory where the archive will be unzipped.
     * @throws IOException          If the archive file does not exist.
     * @throws NullPointerException If the archive file or the destination is null.
     */
    public static void unzip(File archive, File destination) throws IOException {
        if (archive == null) throw new NullPointerException("The ZIP file can not be null");
        if (!archive.exists()) throw new FileNotFoundException("The ZIP file does not exist");

        // Unzip
        LOGGER.log(FINE, "Extracting file {0}", archive.toURI().toASCIIString());

        final FileInputStream inputStream = new FileInputStream(archive);
        unzip(inputStream, destination);
    }

    /**
     * Unzip the given archive into the provided destination. If the destination does not exist it is created.
     *
     * @param archive     The archive file to unzip.
     * @param destination The destination directory where the archive will be unzipped.
     * @throws IOException          If the archive file does not exist.
     * @throws NullPointerException If the archive file or the destination is null.
     */
    public static void unzip(InputStream archive, File destination) throws IOException {
        if (archive == null) throw new NullPointerException("The ZIP file can not be null");
        if (destination == null) throw new NullPointerException("The destination can not be null");

        if (!destination.exists() && !destination.mkdirs()) {
            throw new IOException("Can not create destination folder");
        }

        final ZipInputStream zipReader = new ZipInputStream(archive);
        ZipEntry zipEntry;
        File extractedFile;

        while ((zipEntry = zipReader.getNextEntry()) != null) {
            extractedFile = new File(destination, zipEntry.getName());


            if (isFileInParent(extractedFile, destination)) {
                LOGGER.fine("Extracting file: " + extractedFile.getAbsolutePath());

                if (zipEntry.isDirectory()) {
                    if (!extractedFile.exists() && !extractedFile.mkdirs()) {
                        throw new IOException("Can not create folder");
                    }
                } else {
                    // Ensure to create the parents directories
                    if (!extractedFile.getParentFile().exists() && !extractedFile.getParentFile().mkdirs()) {
                        throw new IOException("Can not create the parent folder");
                    }

                    int length;
                    final byte[] buffer = new byte[1024];

                    try (final FileOutputStream extractedFileOutputStream = new FileOutputStream(extractedFile)) {

                        while ((length = zipReader.read(buffer)) > 0) {
                            extractedFileOutputStream.write(buffer, 0, length);
                        }

                        extractedFileOutputStream.flush();
                    }
                }
            } else {
                LOGGER.severe("Skipping unzipping entry " + zipEntry.getName() + " as it isn't in the destination");
            }
        }

        LOGGER.fine("Extraction done");

        zipReader.closeEntry();
        zipReader.close();
    }

    /**
     * Compress the given fileToZip into the given destination. This method manages if the fileToZip is a folder or a simple file.
     *
     * @param fileToZip   The content to compress.
     * @param destination The destination into the content will be compressed.
     * @throws NullPointerException  If the fileToZip or the destination is null.
     * @throws FileNotFoundException If the fileToZip does not exist.
     */
    public static void zip(File fileToZip, File destination) throws IOException {
        if (fileToZip == null) throw new NullPointerException("The file to zip can not be null");
        if (!fileToZip.exists()) throw new FileNotFoundException("The file to zip does not exist");
        if (destination == null) throw new NullPointerException("The destination can not be null");

        final ListFilesFileVisitor visitor = new ListFilesFileVisitor();
        Files.walkFileTree(fileToZip.toPath(), visitor);
        final List<File> filesToZip = visitor.getFiles();


        try (final ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(destination))) {
            ZipEntry entry;
            String entryName;
            final byte[] buffer = new byte[1024];
            int length;

            final String prefixToDelete;
            if (fileToZip.isDirectory()) {
                prefixToDelete = fileToZip.getAbsolutePath() + File.separator;
            } else {
                prefixToDelete = "";
            }

            for (File file : filesToZip) {
                LOGGER.fine("Compressing file: " + file.getAbsolutePath());

                entryName = file.getAbsolutePath().substring(prefixToDelete.length(), file.getAbsolutePath().length());
                entryName = entryName.replaceAll("\\\\", "/");
                LOGGER.log(FINEST, "Entry name: {0}", entryName);

                if (file.isDirectory()) {
                    entry = new ZipEntry(entryName + "/");
                    zipOutput.putNextEntry(entry);
                } else {
                    entry = new ZipEntry(entryName);
                    zipOutput.putNextEntry(entry);

                    try (final FileInputStream fileInput = new FileInputStream(file)) {
                        while ((length = fileInput.read(buffer)) > 0) {
                            zipOutput.write(buffer, 0, length);
                        }
                    }
                }
            }

            zipOutput.closeEntry();
            zipOutput.flush();
        }

        LOGGER.fine("File compressed");
    }

    /**
     * Tests if the given file is contained within a given parent folder.
     *
     * @param file   The file to test the presence in the parent.
     * @param parent The parent that should contain the file.
     * @return {@code true} if the file is within the parent, {@code false} otherwise.
     * @throws IOException If something went wrong during the check.
     */
    protected static boolean isFileInParent(final File file, final File parent) throws IOException {
        return file.getCanonicalPath().startsWith(parent.getCanonicalPath());
    }
}

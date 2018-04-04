package com.twasyl.slideshowfx.utils;

import com.twasyl.slideshowfx.utils.io.ListFilesFileVisitor;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This class provides utility methods for working with Zip files.
 *
 * @author Thierry Wasylczenko
 */
public class ZipUtils {

    private static final Logger LOGGER = Logger.getLogger(ZipUtils.class.getName());

    /**
     * Unzpi the given archive into the provided destination. If the destination does not exist it is created.
     * @param archive The archive file to unzip.
     * @param destination The destination directory where the archive will be unzipped.
     * @throws IOException If the archive file does not exist.
     * @throws java.lang.NullPointerException If the archive file or the destination is null.
     */
    public static void unzip(File archive, File destination) throws IOException, IllegalArgumentException {
        if(archive == null) throw new NullPointerException("The ZIP file can not be null");
        if(!archive.exists()) throw new FileNotFoundException("The ZIP file does not exist");

        // Unzip
        LOGGER.fine("Extracting file " + archive.toURI().toASCIIString());

        final FileInputStream inputStream = new FileInputStream(archive);
        unzip(inputStream, destination);
    }

    /**
     * Unzpi the given archive into the provided destination. If the destination does not exist it is created.
     * @param archive The archive file to unzip.
     * @param destination The destination directory where the archive will be unzipped.
     * @throws IOException If the archive file does not exist.
     * @throws java.lang.NullPointerException If the archive file or the destination is null.
     */
    public static void unzip(InputStream archive, File destination) throws IOException, IllegalArgumentException {
        if(archive == null) throw new NullPointerException("The ZIP file can not be null");
        if(destination == null) throw new NullPointerException("The destination can not be null");

        if(!destination.exists()) {
            if(!destination.mkdirs()) {
                throw new IOException("Can not create destination folder");
            }
        }

        ZipInputStream zipReader = new ZipInputStream(archive);
        ZipEntry zipEntry;
        File extractedFile;

        while((zipEntry = zipReader.getNextEntry()) != null) {
            extractedFile = new File(destination, zipEntry.getName());

            LOGGER.fine("Extracting file: " + extractedFile.getAbsolutePath());

            if(zipEntry.isDirectory()) {
                if(!extractedFile.exists() && !extractedFile.mkdirs()) {
                    throw new IOException("Can not create folder");
                }
            }
            else {
                // Ensure to create the parents directories
                if(!extractedFile.getParentFile().exists() && !extractedFile.getParentFile().mkdirs()) {
                    throw new IOException("Can not create the parent folder");
                }

                int length;
                byte[] buffer = new byte[1024];

                try(final FileOutputStream extractedFileOutputStream = new FileOutputStream(extractedFile)) {

                    while ((length = zipReader.read(buffer)) > 0) {
                        extractedFileOutputStream.write(buffer, 0, length);
                    }

                    extractedFileOutputStream.flush();
                }
            }
        }

        LOGGER.fine("Extraction done");

        zipReader.closeEntry();
        zipReader.close();
    }

    /**
     * Compress the given fileToZip into the given destination. This method manages if the fileToZip is a folder or a simple file.
     * @param fileToZip The content to compress.
     * @param destination The destination into the content will be compressed.
     * @throws java.lang.NullPointerException If the fileToZip or the destination is null.
     * @throws java.io.FileNotFoundException If the fileToZip does not exist.
     */
    public static void zip(File fileToZip, File destination) throws IOException {
        if(fileToZip == null) throw new NullPointerException("The file to zip can not be null");
        if(!fileToZip.exists()) throw new FileNotFoundException("The file to zip does not exist");
        if(destination == null) throw new NullPointerException("The destination can not be null");

        final ListFilesFileVisitor visitor = new ListFilesFileVisitor();
        Files.walkFileTree(fileToZip.toPath(), visitor);
        final List<File> filesToZip = visitor.getFiles();

        FileInputStream fileInput = null;
        ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(destination));
        ZipEntry entry;
        String entryName;
        byte[] buffer  = new byte[1024];
        int length;

        String prefixToDelete;
        if(fileToZip.isDirectory()) {
            prefixToDelete = fileToZip.getAbsolutePath() + File.separator;
        } else {
            prefixToDelete = "";
        }

        for(File file : filesToZip) {
            LOGGER.fine("Compressing file: " + file.getAbsolutePath());

            entryName = file.getAbsolutePath().substring(prefixToDelete.length(), file.getAbsolutePath().length());
            entryName = entryName.replaceAll("\\\\", "/");
            LOGGER.finest("Entry name: " + entryName);

            if(file.isDirectory()) {
                entry = new ZipEntry(entryName + "/");
                zipOutput.putNextEntry(entry);
            } else {
                entry = new ZipEntry(entryName);
                zipOutput.putNextEntry(entry);

                try {
                    fileInput = new FileInputStream(file);

                    while((length = fileInput.read(buffer)) > 0) {
                        zipOutput.write(buffer, 0, length);
                    }
                } finally {
                    fileInput.close();
                }
            }
        }

        zipOutput.closeEntry();
        zipOutput.flush();
        zipOutput.close();

        LOGGER.fine("File compressed");
    }
}

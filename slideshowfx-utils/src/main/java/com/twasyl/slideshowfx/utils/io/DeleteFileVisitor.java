package com.twasyl.slideshowfx.utils.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * This class extends {@link java.nio.file.SimpleFileVisitor} in order to delete directories.
 * This implementation overrides the following methods:
 * <ul>
 *     <li>{@link java.nio.file.SimpleFileVisitor#postVisitDirectory(Object, java.io.IOException)}</li>
 *     <li>{@link java.nio.file.SimpleFileVisitor#visitFile(Object, java.nio.file.attribute.BasicFileAttributes)}</li>
 *     <li>{@link java.nio.file.SimpleFileVisitor#visitFileFailed(Object, java.io.IOException)}</li>
 * </ul>
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class DeleteFileVisitor extends SimpleFileVisitor<Path> {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc == null) {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        } else {
            // directory iteration failed; propagate exception
            throw exc;
        }
    }
}

package com.twasyl.slideshowfx.utils.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * This class is used to copy a directory to another one. This class overrides:
 * <ul>
 *     <li>{@link java.nio.file.SimpleFileVisitor#preVisitDirectory(Object, java.nio.file.attribute.BasicFileAttributes)}</li>
 *     <li>{@link java.nio.file.SimpleFileVisitor#visitFile(Object, java.nio.file.attribute.BasicFileAttributes)}</li>
 * </ul>
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class CopyFileVisitor extends SimpleFileVisitor<Path> {

    private Path target;
    private Path source;

    public CopyFileVisitor(Path target, Path source) {
        this.target = target;
        this.source = source;
    }

    /**
     * Get the target where data will be copied into.
     * @return The target where the files will be copied into.
     */
    public Path getTarget() { return target; }

    /**
     * Set the target where files will be copied into.
     * @param target the new target of files.
     */
    public void setTarget(Path target) { this.target = target; }

    /**
     * Get the source directory to copy into the target.
     * @return The source directory to copy into the target.
     */
    public Path getSource() { return source; }

    /**
     * Set the new directory to copy into the target.
     * @param source The new source to copy into the target.
     */
    public void setSource(Path source) { this.source = source; }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        final Path parent = source.getParent();

        if(parent != null) {
            Path copiedDirectory = target.resolve(parent.relativize(dir));

            /**
             * Manages the fact {@code dir} is equal to {@code source}
             */
            if(copiedDirectory.equals(target)) {
                copiedDirectory = new File(target.toFile(), dir.toFile().getName()).toPath();
            }

            if(!Files.exists(copiedDirectory)) {
                Files.createDirectories(copiedDirectory);
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        final Path parent = source.getParent();

        if(parent != null) {
            Path copiedFile = target.resolve(parent.relativize(file));
            Files.copy(file, copiedFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return FileVisitResult.CONTINUE;
    }
}

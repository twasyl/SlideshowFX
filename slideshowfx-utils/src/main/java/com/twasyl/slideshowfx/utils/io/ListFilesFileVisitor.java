package com.twasyl.slideshowfx.utils.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An implementation of {@link SimpleFileVisitor} that lists all paths within a directory.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 */
public class ListFilesFileVisitor extends SimpleFileVisitor<Path> {

    private List<Path> paths = new ArrayList<>();
    private Set<Path> nonEmptyDirectories = new HashSet<>();

    /**
     * Get the paths visited by this {@link java.nio.file.FileVisitor}.
     *
     * @return The list of all paths.
     */
    public List<Path> getPaths() {
        return paths;
    }

    /**
     * Get the paths visited by this {@link java.nio.file.FileVisitor}.
     *
     * @return The list of all files.
     */
    public List<File> getFiles() {
        return paths.stream().map(Path::toFile).collect(Collectors.toList());
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        this.paths.add(file);
        this.nonEmptyDirectories.add(file.getParent());

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        this.paths.add(dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        if (this.nonEmptyDirectories.contains(dir)) {
            this.paths.remove(dir);
            this.nonEmptyDirectories.remove(dir);
        }

        return FileVisitResult.CONTINUE;
    }
}

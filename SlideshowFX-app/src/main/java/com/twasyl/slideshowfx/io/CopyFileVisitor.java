/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.io;

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
 * @since SlideshowFX 1.0.0
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
        Path copiedDirectory = target.resolve(source.getParent().relativize(dir));

        /**
         * Manages the fact <code>dir</code> is equal to <code>source</code>
         */
        if(copiedDirectory.equals(target)) {
            copiedDirectory = new File(target.toFile(), dir.toFile().getName()).toPath();
        }

        if(!Files.exists(copiedDirectory)) {
            Files.createDirectories(copiedDirectory);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path copiedFile = target.resolve(source.getParent().relativize(file));
        Files.copy(file, copiedFile, StandardCopyOption.REPLACE_EXISTING);

        return FileVisitResult.CONTINUE;
    }
}

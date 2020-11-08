package com.twasyl.slideshowfx.gradle.plugins.sfxrelease.internal;

import com.twasyl.slideshowfx.gradle.plugins.sfxrelease.extensions.ReleaseExtension;
import org.gradle.api.logging.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 * This implementation of {@link SimpleFileVisitor} looks for source files and update their content if it contains the
 * token represented by the {@link ReleaseExtension#getNextVersionToken()}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class UpdateProductVersionNumberFileVisitor extends SimpleFileVisitor<Path> {
    private final Logger logger;
    private final ReleaseExtension extension;

    public UpdateProductVersionNumberFileVisitor(final Logger logger, ReleaseExtension extension) {
        this.logger = logger;
        this.extension = extension;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        final var fileName = dir.toFile().getName();
        if (fileName.equals("build") || fileName.equals(".gradle")) {
            this.logger.debug("Folder {} is skipped.", dir);
            return SKIP_SUBTREE;
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        final var fileName = file.toFile().getName();
        if (fileName.endsWith(".java") || fileName.endsWith(".yml")) {
            this.updateFile(file);
        } else {
            this.logger.debug("File {} is not considered to be potentially updated.");
        }

        return CONTINUE;
    }

    private void updateFile(final Path file) throws IOException {
        final var content = Files.readString(file, UTF_8);

        if (content.contains(this.extension.getNextVersionToken().get())) {
            this.logger.debug("The file {} contains the next version token and will be updated", file);

            try (final var writer = new FileWriter(file.toFile(), UTF_8)) {
                writer.write(content.replaceAll(this.extension.getNextVersionToken().get(), this.extension.getProductVersion().get()));
                this.logger.debug("The file {} has been updated with the product version", file);
            }
        } else {
            this.logger.debug("The file {} doesn't contain the next version token and won't be updated", file);
        }
    }
}
package com.twasyl.slideshowfx.gradle.plugins.sfxrelease.tasks;

import com.twasyl.slideshowfx.gradle.plugins.DefaultSlideshowFXTask;
import com.twasyl.slideshowfx.gradle.plugins.sfxrelease.extensions.ReleaseExtension;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.regex.Pattern.MULTILINE;

/**
 * This tasks updates all build file containing a {@code -SNAPSHOT} version.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class RemoveSnapshots extends DefaultSlideshowFXTask<ReleaseExtension> {
    private static final Pattern versionRegex = Pattern.compile("(^version\\s+=\\s+'\\d+(?:\\.\\d+)*)-SNAPSHOT(\'\\s+$)", MULTILINE);

    public RemoveSnapshots() {
        super(ReleaseExtension.class);
        this.setGroup("Release");
        setDescription("Remove the -SNAPSHOT qualifier from versions from build scripts.");
    }

    @TaskAction
    public void execute() {
        getProject().getSubprojects().stream()
                .filter(p -> p.getVersion().toString().endsWith("-SNAPSHOT"))
                .map(Project::getBuildFile)
                .forEach(this::updateVersion);
    }

    private void updateVersion(final File buildFile) throws GradleException {
        getLogger().debug("The build file {} will be updated because of it's snapshot version.", buildFile);
        try {
            var content = Files.readString(buildFile.toPath(), UTF_8);
            final var matcher = versionRegex.matcher(content);
            content = matcher.replaceAll(matchResult -> matchResult.group(1) + matchResult.group(2));

            try (final var writer = new FileWriter(buildFile, UTF_8)) {
                writer.write(content);
                getLogger().debug("The build file {} has been updated.", buildFile);
            }
        } catch (IOException e) {
            throw new GradleException("Error updating the build script", e);
        }
    }
}

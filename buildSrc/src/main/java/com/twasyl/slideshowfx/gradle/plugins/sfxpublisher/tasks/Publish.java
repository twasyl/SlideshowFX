package com.twasyl.slideshowfx.gradle.plugins.sfxpublisher.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Set;

import static com.twasyl.slideshowfx.gradle.Utils.stripProjectVersion;
import static com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.BUNDLES_CONFIGURATION_NAME;
import static com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.BUNDLE_TASK_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Task responsible of triggering the publication of a SlideshowFX plugin. The publication is currently done on
 * Bintray.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class Publish extends DefaultTask {

    private final boolean isPlugin;
    private final boolean isDistribution;

    public Publish() {
        final TaskContainer tasks = getProject().getTasks();
        final PluginContainer plugins = getProject().getPlugins();

        isPlugin = plugins.hasPlugin("sfx-plugin");
        isDistribution = plugins.hasPlugin(DistributionPlugin.class);

        if (isPlugin) {
            this.dependsOn(tasks.findByName(BUNDLE_TASK_NAME));
        } else if (isDistribution) {
            this.dependsOn(tasks.findByName("distZip"));
        }

        this.setEnabled(!getProject().getGradle().getStartParameter().isOffline() && hasNetworkAccess() && (isPlugin || isDistribution));
    }

    @TaskAction
    public void publish() throws IOException {
        if (hasNetworkAccess()) {
            if (packageNotExists()) {
                createPackage();
            }

            if (versionNotExists()) {
                createVersion();
                uploadFile();
            }
        } else {
            throw new GradleException("No network access for executing the publishing task");
        }
    }

    private boolean hasNetworkAccess() {
        try {
            final HttpURLConnection connection = openConnection("/");
            final int responseCode = connection.getResponseCode();
            connection.disconnect();
            return 200 == responseCode;
        } catch (IOException e) {
            return false;
        }
    }

    private String getBintrayUsername() {
        String bintrayUserName = System.getProperty("bintrayUserName");
        if (bintrayUserName == null || bintrayUserName.trim().isEmpty()) {
            bintrayUserName = System.getenv("BINTRAY_USER_NAME");
        }
        return bintrayUserName;
    }

    private String getBintrayApiKey() {
        String bintrayApiKey = System.getProperty("bintrayApiKey");
        if (bintrayApiKey == null || bintrayApiKey.trim().isEmpty()) {
            bintrayApiKey = System.getenv("BINTRAY_API_KEY");
        }
        return bintrayApiKey;
    }

    private HttpURLConnection openConnection(final String endpoint) throws IOException {
        final URL url = buildBintrayUrl(endpoint);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        authenticateConnection(connection);
        return connection;
    }

    private URL buildBintrayUrl(final String endpoint) throws MalformedURLException {
        return new URL("https://api.bintray.com" + endpoint);
    }

    private void authenticateConnection(URLConnection connection) {
        final String credentials = getBintrayUsername() + ":" + getBintrayApiKey();
        final String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        connection.setRequestProperty("Authorization", basicAuth);
    }

    private boolean packageNotExists() throws IOException {
        final HttpURLConnection connection = openConnection("/packages/" + getBintrayUsername() + "/SlideshowFX/" + getProject().getName());

        final int responseCode = connection.getResponseCode();
        connection.disconnect();

        if (200 == responseCode) {
            getLogger().info("Package {} already exists", getProject().getName());
        } else if (404 == responseCode) {
            getLogger().info("package {} doesn't exist", getProject().getName());
        }

        return 404 == responseCode;
    }

    private void createPackage() throws IOException {
        getLogger().info("Create package {}", getProject().getName());
        final HttpURLConnection connection = openConnection("/packages/" + getBintrayUsername() + "/SlideshowFX");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        final String content = "{" +
                "\"name\": \"" + getProject().getName() + "\", " +
                "\"desc\": \"" + getProject().getDescription() + "\", " +
                "\"licenses\": [\"Apache-2.0\"], " +
                "\"vcs_url\": \"https://github.com/twasyl/SlideshowFX.git\", " +
                "\"website_url\": \"https://slideshowfx.github.io\", " +
                "\"issue_tracker_url\": \"https://github.com/twasyl/SlideshowFX/issues\", " +
                "\"github_repo\": \"twasyl/SlideshowFX\", " +
                "\"github_release_notes_file\": \"CHANGELOG.md\", " +
                "\"public_download_numbers\": false, " +
                "\"public_stats\": false}";

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(content.length()));
        connection.getOutputStream().write(content.getBytes(UTF_8));

        final int responseCode = connection.getResponseCode();
        connection.disconnect();

        if (201 != responseCode) {
            throw new GradleException("Can not create package " + getProject().getName() + ". Response code: " + responseCode);
        } else {
            getLogger().info("Package {} created", getProject().getName());
        }
    }

    private boolean versionNotExists() throws IOException {
        final String version = stripProjectVersion(getProject());

        final HttpURLConnection connection = openConnection("/packages/" + getBintrayUsername() + "/SlideshowFX/" + getProject().getName() + "/versions/" + version);
        final int responseCode = connection.getResponseCode();
        connection.disconnect();

        if (200 == responseCode) {
            getLogger().info("Version {} already exists for package {}", version, getProject().getName());
        } else if (404 == responseCode) {
            getLogger().info("Version {} doesn't exist for package {}", version, getProject().getName());
        }

        return 404 == responseCode;
    }

    private void createVersion() throws IOException {
        getLogger().info("Create version {} for package {}", stripProjectVersion(getProject()), getProject().getName());
        final HttpURLConnection connection = openConnection("/packages/" + getBintrayUsername() + "/SlideshowFX/" + getProject().getName() + "/versions");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        final String content = "{\"name\": \"" + stripProjectVersion(getProject()) + "\"}";
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(content.length()));
        connection.getOutputStream().write(content.getBytes(UTF_8));

        final int responseCode = connection.getResponseCode();
        connection.disconnect();

        if (responseCode != 201) {
            throw new GradleException("Can not create version: " + responseCode);
        } else {
            getLogger().info("Version {} for package {} created", stripProjectVersion(getProject()), getProject().getName());
        }
    }

    public void uploadFile() throws IOException {
        final Set<File> artifacts = getProject().getConfigurations().getByName(BUNDLES_CONFIGURATION_NAME).getArtifacts().getFiles().getFiles();
        int chunkSize = 1024;

        for (final File artifact : artifacts) {
            getLogger().info("Uploading file {}", artifact.getAbsolutePath());
            final HttpURLConnection connection = openConnection("/content/" + getBintrayUsername() + "/SlideshowFX/" + getProject().getName() + "/" + stripProjectVersion(getProject()) + "/" + artifact.getName() + "?publish=0&override=0");
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(chunkSize);

            final OutputStream output = connection.getOutputStream();
            try (final FileInputStream input = new FileInputStream(artifact)) {
                byte[] buffer = new byte[chunkSize];
                int bytesRead;

                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            } finally {
                output.flush();
                output.close();
            }

            final int responseCode = connection.getResponseCode();
            connection.disconnect();

            if (201 != responseCode) {
                throw new GradleException("Error uploading artifact " + artifact.getName() + ". Response code: " + responseCode);
            } else {
                getLogger().info("Artifact {} uploaded", artifact.getAbsolutePath());
            }
        }
    }
}

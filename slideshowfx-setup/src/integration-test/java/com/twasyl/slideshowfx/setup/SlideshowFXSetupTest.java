package com.twasyl.slideshowfx.setup;

import com.twasyl.slideshowfx.setup.app.SlideshowFXSetup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class SlideshowFXSetupTest extends SlideshowFXSetup {

    protected static final File basedir = new File("slideshowfx-setup/build/integration-tests", "package");
    protected static final File pluginsDir = new File(basedir, "plugins");
    protected static final File documentationDir = new File(basedir, "documentations");
    protected static final File artifactFile = new File(basedir, "SlideshowFX");

    protected static void initializeResources() {
        if (!basedir.exists()) {
            basedir.mkdirs();
        }

        if (!pluginsDir.exists()) {
            pluginsDir.mkdir();
        }

        final File markupsDir = new File(pluginsDir, "markups");
        if (!markupsDir.exists()) {
            markupsDir.mkdir();
        }

        final File executorsDir = new File(pluginsDir, "executors");
        if (!executorsDir.exists()) {
            executorsDir.mkdir();
        }

        final File extensionsDir = new File(pluginsDir, "extensions");
        if (!extensionsDir.exists()) {
            extensionsDir.mkdir();
        }

        final File hostingConnectorsDir = new File(pluginsDir, "hostingConnectors");
        if (!hostingConnectorsDir.exists()) {
            hostingConnectorsDir.mkdir();
        }

        if (!documentationDir.exists()) {
            documentationDir.mkdir();
        }

        if (!artifactFile.exists()) {
            final File internalStruct = new File(artifactFile, "Contents/Java");
            internalStruct.mkdirs();
            final File configFile = new File(internalStruct, "SlideshowFX.cfg");
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        createDummyPluginJar(markupsDir, "slideshowfx-markdown", "Alert");
        createDummyPluginJar(executorsDir, "slideshowfx-java-executor", "Java executor");
        createDummyPluginJar(extensionsDir, "slideshowfx-code-extension", "Code extension");
        createDummyPluginJar(hostingConnectorsDir, "slideshowfx-box-connector", "Box hosting connector");
    }

    private static void createDummyPluginJar(File dir, final String name, String label) {
        final Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.putValue("Bundle-Version", "1.0");
        attributes.putValue("Bundle-Description", "This is a dummy plugin + " + System.currentTimeMillis());
        attributes.putValue("Setup-Wizard-Label", label);
        attributes.putValue("Setup-Wizard-Icon-Name", "EXCLAMATION_TRIANGLE");

        final File jar = new File(dir, name + ".jar");
        try (final FileOutputStream fos = new FileOutputStream(jar);
             final JarOutputStream output = new JarOutputStream(fos, manifest)) {
            final ZipEntry entry = new ZipEntry("test.txt");
            output.putNextEntry(entry);
            output.write("Dummy plugin".getBytes());
            output.closeEntry();
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Properties getSetupProperties() throws IOException {
        final Properties properties = new Properties();
        properties.setProperty(SETUP_PLUGINS_DIRECTORY_PROPERTY, pluginsDir.getAbsolutePath());
        properties.setProperty(SETUP_APPLICATION_ARTIFACT_PROPERTY, artifactFile.getAbsolutePath());
        properties.setProperty(SETUP_DOCUMENTATIONS_DIRECTORY_PROPERTY, documentationDir.getAbsolutePath());
        properties.setProperty(SETUP_APPLICATION_NAME_PROPERTY, "SlideshowFX");
        properties.setProperty(SETUP_APPLICATION_VERSION_PROPERTY, "DEVELOPMENT");
        properties.setProperty(SETUP_SERVICE_TWITTER_CONSUMER_KEY_PROPERTY, "consumerkey");
        properties.setProperty(SETUP_SERVICE_TWITTER_CONSUMER_SECRET_PROPERTY, "consumersecret");

        return properties;
    }

    public static void main(String[] args) {
        initializeResources();

        System.setProperty("setup.plugins.directory", pluginsDir.getAbsolutePath());
        System.setProperty("setup.documentations.directory", documentationDir.getAbsolutePath());
        System.setProperty("setup.application.artifact", artifactFile.getAbsolutePath());
        System.setProperty("setup.application.name", "SlideshowFX");
        System.setProperty("setup.application.version", "TEST-VERSION");

        launch(args);
    }
}

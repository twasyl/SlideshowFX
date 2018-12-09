package com.twasyl.slideshowfx.setup;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.setup.app.SlideshowFXSetup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static java.util.jar.Attributes.Name.MANIFEST_VERSION;

/**
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class SlideshowFXSetupTest extends SlideshowFXSetup {

    private static final File integrationTestsDir = new File("slideshowfx-setup/build", "integration-tests");
    private static final File applicationDir = new File(integrationTestsDir, "SFXInstallation");
    private static final File packageDir = new File(integrationTestsDir, "package");
    private static final File pluginsDir = new File(packageDir, "plugins");
    private static final File documentationDir = new File(packageDir, "documentations");
    private static final File artifactFile = new File(packageDir, "SlideshowFX");

    private static void initializeResources() {
        if (!integrationTestsDir.exists()) {
            integrationTestsDir.mkdirs();
        }

        GlobalConfiguration.createApplicationDirectory();
        GlobalConfiguration.createPluginsDirectory();

        if (!packageDir.exists()) {
            packageDir.mkdirs();
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

        createDummyPluginJar(GlobalConfiguration.getPluginsDirectory(), "slideshowfx-markdown", "Alert", "1.0");
        createDummyPluginJar(GlobalConfiguration.getPluginsDirectory(), "slideshowfx-java-executor", "Java executor", "1.0");
        createDummyPluginJar(GlobalConfiguration.getPluginsDirectory(), "slideshowfx-code-extension", "Code extension", "2.0");

        createDummyPluginJar(markupsDir, "slideshowfx-markdown", "Alert", "2.0");
        createDummyPluginJar(executorsDir, "slideshowfx-java-executor", "Java executor", "1.0");
        createDummyPluginJar(extensionsDir, "slideshowfx-code-extension", "Code extension", "1.0");
        createDummyPluginJar(hostingConnectorsDir, "slideshowfx-box-connector", "Box hosting connector", "1.0");
    }

    private static void createDummyPluginJar(File dir, final String name, String label, String version) {
        final Manifest manifest = new Manifest();
        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(MANIFEST_VERSION, "1.0.0");
        attributes.put(new Name("Bundle-Version"), version);
        attributes.put(new Name("Bundle-Description"), "This is a dummy plugin + " + System.currentTimeMillis());
        attributes.put(new Name("Setup-Wizard-Label"), label);
        attributes.put(new Name("Setup-Wizard-Icon-Name"), "EXCLAMATION_TRIANGLE");

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
        System.setProperty("application.dir", applicationDir.getAbsolutePath());
        System.setProperty("setup.plugins.directory", pluginsDir.getAbsolutePath());
        System.setProperty("setup.documentations.directory", documentationDir.getAbsolutePath());
        System.setProperty("setup.application.artifact", artifactFile.getAbsolutePath());
        System.setProperty("setup.application.name", "SlideshowFX");
        System.setProperty("setup.application.version", "TEST-VERSION");

        initializeResources();
        launch(args);
    }
}

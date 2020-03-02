package com.twasyl.slideshowfx.setup;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.plugin.manager.internal.PluginFile;
import com.twasyl.slideshowfx.setup.app.SetupProperties;
import com.twasyl.slideshowfx.setup.app.SlideshowFXSetup;
import com.twasyl.slideshowfx.utils.OSUtils;
import com.twasyl.slideshowfx.utils.io.IOUtils;
import javafx.scene.control.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.jar.Attributes.Name.MANIFEST_VERSION;

/**
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class SlideshowFXSetupTest extends SlideshowFXSetup {

    private static final File integrationTestsDir = new File("slideshowfx-setup/build", "integration-tests");
    private static final File applicationDir = new File(integrationTestsDir, ".SlideshowFX");
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
            final File internalStruct;

            if (OSUtils.isMac()) {
                internalStruct = new File(artifactFile, "Contents/Java");
            } else {
                internalStruct = new File(artifactFile, "app");
            }

            internalStruct.mkdirs();
            final File configFile = new File(internalStruct, "SlideshowFX.cfg");
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        createDummyPlugin(GlobalConfiguration.getPluginsDirectory(), "slideshowfx-markdown", "Alert", "1.0");
        createDummyPlugin(GlobalConfiguration.getPluginsDirectory(), "slideshowfx-java-executor", "Java executor", "1.0");
        createDummyPlugin(GlobalConfiguration.getPluginsDirectory(), "slideshowfx-code-extension", "Code extension", "2.0");

        createDummyPlugin(markupsDir, "slideshowfx-markdown", "Alert", "2.0");
        createDummyPlugin(executorsDir, "slideshowfx-java-executor", "Java executor", "1.0");
        createDummyPlugin(extensionsDir, "slideshowfx-code-extension", "Code extension", "1.0");
        createDummyPlugin(hostingConnectorsDir, "slideshowfx-box-connector", "Box hosting connector", "1.0");
    }

    private static void createDummyPlugin(File dir, final String name, String label, String version) {
        final Manifest manifest = new Manifest();
        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(MANIFEST_VERSION, "1.0.0");
        attributes.put(new Name("Plugin-Name"), label);
        attributes.put(new Name("Plugin-Version"), version);
        attributes.put(new Name("Plugin-Description"), "This is a dummy plugin + " + System.currentTimeMillis());
        attributes.put(new Name("Setup-Wizard-Icon-Name"), "EXCLAMATION_TRIANGLE");


        try (final ZipOutputStream plugin = new ZipOutputStream(new FileOutputStream(new File(dir, name + "-" + version + PluginFile.EXTENSION)));
             final ByteArrayOutputStream jarContent = new ByteArrayOutputStream();
             final JarOutputStream jar = new JarOutputStream(jarContent, manifest)) {

            jar.putNextEntry(new ZipEntry("test.txt"));
            jar.write("Dummy plugin".getBytes());
            jar.closeEntry();
            jar.flush();

            plugin.putNextEntry(new ZipEntry(name + "-" + version + ".jar"));
            plugin.write(jarContent.toByteArray());
            plugin.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws Exception {
        super.init();
        SetupProperties.getInstance()
                .withPluginsDirectory(pluginsDir)
                .withApplicationArtifact(artifactFile)
                .withDocumentationsDirectory(documentationDir)
                .withApplicationName("SlideshowFX")
                .withApplicationVersion("DEVELOPMENT")
                .withTwitterConsumerKey("consumerkey")
                .withTwitterConsumerSecret("consumersecret")
                .withDefaultInstallationLocation(integrationTestsDir);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        IOUtils.deleteDirectory(integrationTestsDir);
    }

    public static void main(String[] args) {
        new Button("Test");
        System.setProperty("application.dir", applicationDir.getAbsolutePath());

        initializeResources();
        launch(args);
    }
}

package com.twasyl.slideshowfx.plugin.manager;

import com.twasyl.slideshowfx.plugin.IPlugin;
import com.twasyl.slideshowfx.plugin.manager.internal.PluginFile;

import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.io.File.separator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static java.util.jar.Attributes.Name.MANIFEST_VERSION;
import static javax.tools.JavaFileObject.Kind.SOURCE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class PluginTestUtils {

    private PluginTestUtils() {
    }

    private static class DummyPluginClass extends SimpleJavaFileObject {
        public DummyPluginClass() {
            super(URI.create("string:///com/twasyl/slideshowfx/dummy/plugin/Dummy.java"), SOURCE);
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return """
                   package com.twasyl.slideshowfx.dummy.plugin;
                   
                   import com.twasyl.slideshowfx.plugin.AbstractPlugin;
                   
                   public class Dummy extends AbstractPlugin {
                   
                       public Dummy() {
                           super("Dummy");
                       }
                   }
                   """;
        }
    }

    public static File createDummyPlugin(final String name, String label, String version) {
        final Manifest manifest = new Manifest();
        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(MANIFEST_VERSION, "1.0.0");
        attributes.put(new Attributes.Name("Plugin-Name"), label);
        attributes.put(new Attributes.Name("Plugin-Version"), version);
        attributes.put(new Attributes.Name("Plugin-Description"), "This is a dummy plugin + " + System.currentTimeMillis());
        attributes.put(new Attributes.Name("Setup-Wizard-Icon-Name"), "EXCLAMATION_TRIANGLE");

        final File pluginsDir = new File(System.getProperty("plugins.dir"));
        final File pluginFile = new File(pluginsDir, name + "-" + version + PluginFile.EXTENSION);

        try (final ZipOutputStream plugin = new ZipOutputStream(new FileOutputStream(pluginFile));
             final ByteArrayOutputStream jarContent = new ByteArrayOutputStream();
             final JarOutputStream jar = new JarOutputStream(jarContent, manifest)) {

            jar.putNextEntry(new ZipEntry("com/twasyl/slideshowfx/dummy/plugin/Dummy.class"));
            jar.write(createDummyPluginClass());
            jar.closeEntry();
            jar.putNextEntry(new ZipEntry("META-INF/services/" + IPlugin.class.getName()));
            jar.write("com.twasyl.slideshowfx.dummy.plugin.Dummy".getBytes(UTF_8));
            jar.closeEntry();
            jar.putNextEntry(new ZipEntry("com/twasyl/slideshowfx/dummy/plugin/Application.fxml"));
            jar.write("Some random content".getBytes(UTF_8));
            jar.closeEntry();
            jar.flush();
            jar.close();

            plugin.putNextEntry(new ZipEntry(name + "-" + version + ".jar"));
            plugin.write(jarContent.toByteArray());
            plugin.closeEntry();
        } catch (IOException e) {
            fail(e);
        }

        assertTrue(pluginFile.exists());
        return pluginFile;
    }

    public static byte[] createDummyPluginClass() {
        final File compiledClass = new File("build/tmp/com/twasyl/slideshowfx/dummy/plugin/Dummy.class");

        if (!compiledClass.exists()) {
            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            final Optional<SourceVersion> sourceVersion = compiler.getSourceVersions().stream().max(Comparator.naturalOrder());

            try (final StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
                 final var writer = new StringWriter()) {
                final var task = compiler.getTask(writer, manager, null,
                                                  Arrays.asList("-source", sourceVersion.get().ordinal() + "", "-d", "build/tmp/", "-cp", ".." + separator + "slideshowfx-plugin" + separator + "build" + separator + "production" + separator + "slideshowfx.plugin"), null, singletonList(new DummyPluginClass()));

                if (Boolean.FALSE.equals(task.call())) {
                    fail(writer.toString());
                }
            } catch (IOException e) {
                fail(e);
            }
        }

        try (final FileInputStream classFile = new FileInputStream(compiledClass);
             final ByteArrayOutputStream classContent = new ByteArrayOutputStream()) {
            final byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = classFile.read(buffer)) != -1) {
                classContent.write(buffer, 0, bytesRead);
            }

            return classContent.toByteArray();
        } catch (IOException e) {
            fail(e);
        }

        return null;
    }
}

package com.twasyl.slideshowfx.gradle.annotation.processors;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static javax.lang.model.SourceVersion.RELEASE_16;
import static javax.lang.model.element.ElementKind.CLASS;

/**
 * This annotation processor process the {@code com.twasyl.slideshowfx.plugin.Plugin} annotation and generates the
 * corresponding service files for the plugin in the {@code META-INF/services} directory to be included in the resulting
 * JAR archive.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
@SupportedAnnotationTypes("com.twasyl.slideshowfx.plugin.Plugin")
@SupportedSourceVersion(RELEASE_16)
@SupportedOptions(PluginProcessor.GENERATED_DIR_OPTION_NAME)
public class PluginProcessor extends AbstractProcessor {

    public static final String GENERATED_DIR_OPTION_NAME = "generated.dir";
    public static final String CONTENT_EXTENSION_INTERFACE_NAME = "com.twasyl.slideshowfx.content.extension.IContentExtension";
    public static final String HOSTING_CONNECTOR_INTERFACE_NAME = "com.twasyl.slideshowfx.hosting.connector.IHostingConnector";
    public static final String MARKUP_INTERFACE_NAME = "com.twasyl.slideshowfx.markup.IMarkup";
    public static final String SNIPPET_EXECUTOR_INTERFACE_NAME = "com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

            for (Element annotatedElement : annotatedElements) {
                processAnnotatedClass(annotatedElement);
            }
        }
        return true;
    }

    private void processAnnotatedClass(final Element annotatedClass) {
        if (annotatedClass.getKind() == CLASS) {
            final String annotatedClassName = annotatedClass.asType().toString();
            createServiceFile("com.twasyl.slideshowfx.plugin.IPlugin", annotatedClassName);
            checkInterfaces(annotatedClassName, annotatedClass);
            processSuperClasses(annotatedClassName, (TypeElement) annotatedClass);
        }
    }

    private void processSuperClasses(final String annotatedClassName, TypeElement clazz) {
        final Object superclassObj = clazz.getSuperclass();

        if (superclassObj instanceof DeclaredType) {
            final DeclaredType superclass = (DeclaredType) superclassObj;
            final TypeElement element = (TypeElement) superclass.asElement();
            checkInterfaces(annotatedClassName, element);
            processSuperClasses(annotatedClassName, element);
        }
    }

    private void checkInterfaces(final String annotatedClassName, final Element clazz) {
        final TypeElement typeElement = (TypeElement) clazz;
        final List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
        interfaces.stream()
                .map(iface -> (DeclaredType) iface)
                .map(DeclaredType::asElement)
                .forEach(iface -> checkInterface(annotatedClassName, iface));
    }

    private void checkInterface(final String annotatedClassName, final Element iface) {
        String pluginTypeInterfaceName = null;
        final String interfaceName = iface.asType().toString();

        if (interfaceName.startsWith(CONTENT_EXTENSION_INTERFACE_NAME)) {
            pluginTypeInterfaceName = CONTENT_EXTENSION_INTERFACE_NAME;
        } else if (interfaceName.startsWith(HOSTING_CONNECTOR_INTERFACE_NAME)) {
            pluginTypeInterfaceName = HOSTING_CONNECTOR_INTERFACE_NAME;
        } else if (interfaceName.startsWith(MARKUP_INTERFACE_NAME)) {
            pluginTypeInterfaceName = MARKUP_INTERFACE_NAME;
        } else if (interfaceName.startsWith(SNIPPET_EXECUTOR_INTERFACE_NAME)) {
            pluginTypeInterfaceName = SNIPPET_EXECUTOR_INTERFACE_NAME;
        }

        if (pluginTypeInterfaceName != null) {
            createServiceFile(pluginTypeInterfaceName, annotatedClassName);
        }
    }

    private void createServiceFile(final String pluginTypeInterfaceName, final String pluginImplementationClassName) {
        final File generatedDir = new File(processingEnv.getOptions().get(GENERATED_DIR_OPTION_NAME));
        final File metaInfDir = new File(generatedDir, "META-INF");
        final File servicesDir = new File(metaInfDir, "services");

        if (!servicesDir.exists()) {
            servicesDir.mkdirs();
        }

        final File serviceFile = new File(servicesDir, pluginTypeInterfaceName);

        try (final FileOutputStream output = new FileOutputStream(serviceFile)) {
            output.write(pluginImplementationClassName.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Can not create service file for class " + pluginImplementationClassName, e);
        }
    }
}

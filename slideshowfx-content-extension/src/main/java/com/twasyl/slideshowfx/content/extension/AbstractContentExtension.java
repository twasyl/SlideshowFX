package com.twasyl.slideshowfx.content.extension;

import com.twasyl.slideshowfx.icons.Icon;
import com.twasyl.slideshowfx.plugin.AbstractPlugin;
import com.twasyl.slideshowfx.utils.ZipUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.content.extension.ResourceLocation.EXTERNAL;
import static java.util.logging.Level.SEVERE;

/**
 * This class defines the basic behavior of a content extension.
 *
 * @author Thierry Wasylczenko
 * @version 1.3-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public abstract class AbstractContentExtension<T extends AbstractContentExtensionController> extends AbstractPlugin implements IContentExtension<T> {
    private static final Logger LOGGER = Logger.getLogger(AbstractContentExtension.class.getName());

    protected final String code;
    protected final Icon icon;
    protected final String toolTip;
    protected final String title;
    protected final URL resourcesArchive;
    protected Set<Resource> resources = new LinkedHashSet<>();
    protected URL fxmlURL;
    protected T controller;

    /**
     * Creates a new instance of the content extension.
     *
     * @param code             The code of the content extension. Can not be null or empty.
     * @param fxmlURL
     * @param resourcesArchive The archive that contains all resources that will be extracted for the presentation.
     * @param icon             The icon for this content extension that will be used in the SlideshowFX's UI.
     * @param toolTip          The tooltip for this content extension that will be used in the SlideshowFX's UI.
     * @param title            The title of the window of this content extension.
     * @throws NullPointerException     If the code is null.
     * @throws IllegalArgumentException If the code is empty.
     */
    protected AbstractContentExtension(String code, URL fxmlURL, URL resourcesArchive, Icon icon, String toolTip, String title) {
        super(code);

        if (code == null) throw new NullPointerException("The code of the content extension is null");

        this.code = code.trim();
        if (this.code.isEmpty())
            throw new IllegalArgumentException("The code of the content extension can not be empty");

        this.fxmlURL = fxmlURL;
        this.resourcesArchive = resourcesArchive;
        this.icon = icon;
        this.toolTip = toolTip;
        this.title = title;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public URL getResourcesArchive() {
        return this.resourcesArchive;
    }

    @Override
    public Set<Resource> getResources() {
        return this.resources;
    }

    /**
     * This method allows to declare resources for this content extension and return this content extension. The
     * {@link ResourceLocation location} of the resource will be {@link ResourceLocation#INTERNAL}. The resource URL
     * will be set to {@code null}.
     *
     * @param type    The type of the resource.
     * @param content The content that will be added to the presentation of the resource.
     * @return This content extension.
     */
    protected AbstractContentExtension putResource(ResourceType type, String content) {
        if (content != null && !content.isEmpty()) {
            this.resources.add(new Resource(type, content));
        }

        return this;
    }

    /**
     * This method allows to declare resources for this content extension and return this content extension. The
     * {@link Resource#getLocation() location} will be set to {@link ResourceLocation#EXTERNAL}. The given URL must
     * correspond to a file and not a directory.
     *
     * @param type        The type of the resource.
     * @param content     The content that will be added to the presentation of the resource.
     * @param resourceUrl The {@link URL} of the resource.
     * @return This content extension.
     */
    protected AbstractContentExtension putResource(ResourceType type, String content, final URL resourceUrl) {
        if (content != null && !content.isEmpty() && resourceUrl != null) {
            this.resources.add(new Resource(type, content, EXTERNAL, resourceUrl));
        }

        return this;
    }

    @Override
    public void extractResources(final File directory) {
        if (directory == null)
            throw new NullPointerException("The directory where to extract the resources can not be null");

        if (!directory.exists() && !directory.mkdir()) {
            LOGGER.log(SEVERE, "Can not create the directory where the resources must be extracted");
        }

        if (this.getResourcesArchive() != null && this.getResourcesArchive().getFile() != null) {
            try {
                ZipUtils.unzip(this.getClass().getResourceAsStream(this.getResourcesArchive().getFile()), directory);
            } catch (IOException e) {
                LOGGER.log(SEVERE, "Can not extract the resources", e);
            }
        }

        this.resources.stream()
                .filter(resource -> resource.getLocation() == EXTERNAL)
                .forEach(resource -> {
                    final File resourceFile = new File(directory, resource.getContent());
                    final File destinationDirectory = resourceFile.getParentFile();

                    boolean destinationDirExists = destinationDirectory.exists();
                    if (!destinationDirExists) {
                        destinationDirExists = destinationDirectory.mkdirs();
                    }

                    if (destinationDirExists) {
                        try (final InputStream stream = resource.getResourceUrl().openStream();
                             final FileOutputStream output = new FileOutputStream(resourceFile)) {
                            final byte[] buffer = new byte[512];
                            int bytesRead;

                            while ((bytesRead = stream.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                            }

                            output.flush();
                        } catch (IOException e) {
                            LOGGER.log(SEVERE, "Can't extract external resource: " + resourceFile.getAbsolutePath(), e);
                        }
                    } else {
                        LOGGER.severe("The destination directory for the external doesn't exist or can't be created: " + destinationDirectory.getAbsolutePath());
                    }
                });
    }

    @Override
    public Icon getIcon() {
        return this.icon;
    }

    @Override
    public String getToolTip() {
        return this.toolTip;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public T getController() {
        if (this.controller == null) {
            LOGGER.warning("The controller for the "
                    + getClass().getSimpleName()
                    + " is null. The getUI() method may not have been called or didn't initialize the controller");
        }
        return this.controller;
    }

    @Override
    public Pane getUI() {
        FXMLLoader loader = new FXMLLoader(this.fxmlURL);
        Pane root = null;

        try {
            loader.setClassLoader(getClass().getClassLoader());
            root = loader.load();
            this.controller = loader.getController();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not load UI for " + getClass().getSimpleName(), e);
        }

        return root;
    }
}

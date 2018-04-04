package com.twasyl.slideshowfx.content.extension;

import com.twasyl.slideshowfx.icons.Icon;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.plugin.IPlugin;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.layout.Pane;

import java.io.File;
import java.net.URL;
import java.util.Set;

/**
 * Defines the contract to be considered as a content extension for SlideshowFX. A content extension is a feature allowing
 * to insert evolved content in a slide, for example a chart. Indeed, such content may need additional resources as
 * images, JavaScript libraries and so on.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public interface IContentExtension extends IPlugin {

    /**
     * Get the code of this content extension. The code represents a unique ID between all content extensions in order
     * to identify it and to insert it in the presentation configuration file.
     *
     * @return The code of this content extension
     */
    String getCode();

    /**
     * Get the icon that will be used in the SlideshowFX UI to make the content extension available.
     *
     * @return The icon of the content extension.
     */
    Icon getIcon();

    /**
     * Get the tooltip that will be used in the SlideshowFX UI to make the content extension available.
     *
     * @return The tooltip to be used in the UI of SlideshowFX, for example on a button.
     */
    String getToolTip();

    /**
     * Get the title of this content extension. The title is typically used in the dialog SlideshowFX will create for
     * displaying the UI returned by {@link #getUI()}.
     *
     * @return The title for this content extension.
     */
    String getTitle();

    /**
     * Get the resources this extension need. For example it is all the JavaScript libraries needed for the evolved
     * content to be working.
     *
     * @return The list of resources needed for this evolved content to be working in the presentation.
     */
    Set<Resource> getResources();

    /**
     * Get the URL of the resources archive. Typically the archive is a ZIP file that contains a complete JavaScript
     * library for example. The archive is usually present within the content extension project.
     *
     * @return The URL of the archive containing all resources for this content extension.
     */
    URL getResourcesArchive();

    /**
     * Extract the resources needed for this content extension to be working in a presentation in the given <code>directory</code>.
     * The default behavior will extract the resources in the in {@code directory/#getExtractBaseDirectory()}.
     * Resources to extract are the ones contained within the archive located by the {@link #getResourcesArchive()}.
     *
     * @param directory The directory where the resources will be extracted.
     * @throws NullPointerException If the given directory is null.
     */
    void extractResources(File directory);

    /**
     * Get the UI allowing to specify parameters for creating the evolved content.
     *
     * @return The pane containing the UI for this content extension.
     */
    Pane getUI();

    /**
     * Build the content defined by the {@link #getUI()} method according the given markup. If the given markup is null,
     * the default content string returned by {@link #buildDefaultContentString()} must be returned.
     *
     * @param markup The markup to generate the content in. For example in HTML.
     * @return The content converted in the markup language.
     */
    String buildContentString(IMarkup markup);

    /**
     * Build the default content defined by the {@link #getUI()} method. The default content string should be an HTML
     * representation but it is not mandatory.
     *
     * @return The content converted in the default markup language.
     */
    String buildDefaultContentString();

    /**
     * Indicates if the inputs present in the UI of the content extension are valid. This can be used in order to
     * disable buttons in a particular UI when the inputs are not valid.
     *
     * @return a {@link ReadOnlyBooleanProperty} instance indicating whether or not the input fields are valid in the UI.
     */
    ReadOnlyBooleanProperty areInputsValid();
}

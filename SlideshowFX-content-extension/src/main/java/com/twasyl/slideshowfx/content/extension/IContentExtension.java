/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.content.extension;

import com.twasyl.slideshowfx.markup.IMarkup;
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
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public interface IContentExtension {

    /**
     * Get the code of this content extension. The code represents a unique ID between all content extensions in order
     * to identify it and to insert it in the presentation configuration file.
     * @return The code of this content extension
     */
    String getCode();

    /**
     * Get the icon that will be used in the SlideshowFX UI to make the content extension available.
     * @return The InputStream corresponding of the icon of the content extension.
     */
    URL getIcon();

    /**
     * Get the tooltip that will be used in the SlideshowFX UI to make the content extension available.
     * @return The tooltip to be used in the UI of SlideshowFX, for example on a button.
     */
    String getToolTip();

    /**
     * Get the title of this content extension. The title is typically used in the dialog SlideshowFX will create for
     * displaying the UI returned by {@link #getUI()}.
     * @return The title for this content extension.
     */
    String getTitle();

    /**
     * Get the resources this extension need. For example it is all the JavaScript libraries needed for the evolved
     * content to be working.
     * @return The list of resources needed for this evolved content to be working in the presentation.
     */
    Set<Resource> getResources();

    /**
     * Get the resources location prefix. The resources location prefix is typically the beginning of the path where all
     * resources are located inside the architecture of the content extension. For exemple, imagine all resources are
     * located in <code>/com/twasyl/slideshowfx/content/extension/custom/mylibjs/</code>. During the extraction in order to
     * not extract all resources in this whole path but only in <code>mylibjs</code>, the prefix should be
     * <code>/com/twasyl/slideshowfx/content/extension/custom/</code>
     * @return The resource location prefix.
     */
    String getResourcesLocationPrefix();

    /**
     * Get the resources location inside this content extension. Typically it is each file considered as resource that is
     * contained inside the architecture of the content extension. These resources locations will be extracted by
     * {@link #extractResources(java.io.File)}
     * @return The locations of resources inside the content extension.
     */
    Set<String> getResourcesLocation();

    /**
     * Extract the resources needed for this content extension to be working in a presentation in the given <code>directory</code>.
     * The default behavior will extract the resources in the in {@code directory/#getExtractBaseDirectory()}.
     * @param directory The directory where the resources will be extracted.
     * @throws java.lang.NullPointerException If the given directory is null.
     */
    void extractResources(File directory);

    /**
     * Get the UI allowing to specify parameters for creating the evolved content.
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
     * @return The content converted in the default markup language.
     */
    String buildDefaultContentString();
}

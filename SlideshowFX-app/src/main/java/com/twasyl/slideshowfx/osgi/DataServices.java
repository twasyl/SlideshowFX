/*
 * Copyright 2014 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.osgi;

import com.twasyl.slideshowfx.dao.PresentationDAO;

/**
 * This class provides services to access specific values of the presentation. It is used by content extensions if they
 * need to access resources of a presentation through OSGi.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class DataServices {
    public static final String PRESENTATION_FOLDER = "presentation.folder";
    public static final String PRESENTATION_RESOURCES_FOLDER = "presentation.resources.folder";

    /**
     * Get the value associated to the given property.
     * @param property The property to get the value for.
     * @return The value associated to the given property, or {@code null} if the property is not found.
     */
    public Object get(String property) {
        Object value = null;

        if(PRESENTATION_FOLDER.equals(property) && PresentationDAO.getInstance().getCurrentPresentation() != null) {
            value = PresentationDAO.getInstance().getCurrentPresentation().getWorkingDirectory();
        } else if(PRESENTATION_RESOURCES_FOLDER.equals(property)
                && PresentationDAO.getInstance().getCurrentPresentation() != null
                && PresentationDAO.getInstance().getCurrentPresentation().getTemplateConfiguration() != null) {
            value = PresentationDAO.getInstance().getCurrentPresentation().getTemplateConfiguration().getResourcesDirectory();
        }

        return value;
    }
}

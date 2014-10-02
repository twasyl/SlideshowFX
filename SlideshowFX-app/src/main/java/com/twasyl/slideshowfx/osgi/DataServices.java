package com.twasyl.slideshowfx.osgi;

import com.twasyl.slideshowfx.dao.PresentationDAO;

/**
 * This class provides services to access specific values of the presentation. It is used by content extensions if they
 * need to access resources of a presentation through OSGi.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
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

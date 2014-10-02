package com.twasyl.slideshowfx.dao;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;

/**
 * This class allows to access presentations stored in memory and provide methods for manipulating it. This DAO works
 * as a singleton. The singleton can be accessed using {@link #getInstance()}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class PresentationDAO {

    private static PresentationDAO singleton = new PresentationDAO();
    private static PresentationEngine currentPresentation;

    private PresentationDAO() {}

    public static PresentationDAO getInstance() { return singleton; }

    /**
     * Get the current Presentation used by SlideshowFX.
     * @return The current presentation used by SlideshowFX.
     */
    public PresentationEngine getCurrentPresentation() {
        synchronized (currentPresentation) {
            return currentPresentation;
        }
    }

    /**
     * Set the current presentation used by SlideshowFX.
     * @param currentPresentation The new current presentation.
     */
    public void setCurrentPresentation(PresentationEngine currentPresentation) {
        synchronized (currentPresentation) {
            PresentationDAO.currentPresentation = currentPresentation;
        }
    }
}

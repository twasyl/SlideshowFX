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

package com.twasyl.slideshowfx.dao;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;

/**
 * This class allows to access presentations stored in memory and provide methods for manipulating it. This DAO works
 * as a singleton. The singleton can be accessed using {@link #getInstance()}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
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

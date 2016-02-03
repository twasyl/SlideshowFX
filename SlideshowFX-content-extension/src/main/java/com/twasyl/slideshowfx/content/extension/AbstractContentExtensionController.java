/*
 * Copyright 2016 Thierry Wasylczenko
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

import javafx.application.Platform;
import javafx.fxml.Initializable;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX
 */
public abstract class AbstractContentExtensionController implements Initializable {

    /**
     * Ensure the given action is executed under the FX application thread. This method checks {@link Platform#isFxApplicationThread()}
     * in order to determine if the current thread is the FX one.
     * @param action The action to run
     */
    public void executeUnderFXThread(final Runnable action) {
        if(Platform.isFxApplicationThread()) action.run();
        else Platform.runLater(action);
    }
}

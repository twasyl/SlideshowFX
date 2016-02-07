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

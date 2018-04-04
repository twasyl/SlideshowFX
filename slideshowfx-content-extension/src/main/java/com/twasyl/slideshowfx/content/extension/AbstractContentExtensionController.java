package com.twasyl.slideshowfx.content.extension;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.Initializable;

/**
 * Abstract implementation of {@link Initializable} in order to provide common methods for content extensions.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public abstract class AbstractContentExtensionController implements Initializable {

    /**
     * Ensure the given action is executed under the FX application thread. This method checks {@link Platform#isFxApplicationThread()}
     * in order to determine if the current thread is the FX one.
     *
     * @param action The action to run
     */
    public void executeUnderFXThread(final Runnable action) {
        if (Platform.isFxApplicationThread()) action.run();
        else Platform.runLater(action);
    }

    /**
     * Indicates if the inputs present in the UI of the content extension are valid. This can be used in order to
     * disable buttons in a particular UI when the inputs are not valid.
     *
     * @return a {@link ReadOnlyBooleanProperty} instance indicating whether or not the input fields are valid in the UI.
     */
    public abstract ReadOnlyBooleanProperty areInputsValid();
}

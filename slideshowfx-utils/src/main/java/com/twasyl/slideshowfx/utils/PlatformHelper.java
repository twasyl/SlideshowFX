package com.twasyl.slideshowfx.utils;

import javafx.application.Platform;

/**
 * This class provides helpers to perform UI task.
 *
 * @author Thierry Wasylczenko
 */
public class PlatformHelper {
    private PlatformHelper() {
    }

    /**
     * This method run the given treatment by testing if it is currently in a
     * JavaFX application thread.
     *
     * @param treatment the treatment to perform.
     */
    public static void run(Runnable treatment) {
        if (treatment == null) throw new IllegalArgumentException("The treatment to perform can not be null");

        if (Platform.isFxApplicationThread()) treatment.run();
        else Platform.runLater(treatment);
    }
}

package com.twasyl.slideshowfx.utils;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * This class provides helpers to perform UI task.
 *
 * @author Thierry Wasylczenko
 */
public class PlatformHelper {

    /**
     * This method run the given treatment by testing if it is currently in a
     * JavaFX application thread.
     *
     * @param treatment the treatment to perform.
     */
    public static void run(Runnable treatment) {
        if(treatment == null) throw new IllegalArgumentException("The treatment to perform can not be null");

        if(Platform.isFxApplicationThread()) treatment.run();
        else Platform.runLater(treatment);
    }

    /**
     * This method creates a Scene for the given parent.
     *
     * @param parent the content of the Scene.
     * @return the Scene containing the given parent.
     */
    public static Scene createScene(final Parent parent) {
        Scene scene = null;

        if(Platform.isFxApplicationThread()) {
            scene = new Scene(parent);
        } else {
            FutureTask<Scene> future = new FutureTask<Scene>(new Callable<Scene>() {
                @Override
                public Scene call() throws Exception {
                    Scene scene = new Scene(parent);

                    return scene;
                }
            });

            Platform.runLater(future);
            try {
                scene = future.get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }

        return scene;
    }
}

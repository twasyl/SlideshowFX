/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

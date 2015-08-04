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

package com.twasyl.slideshowfx.ui;

import com.twasyl.slideshowfx.controls.notification.NotificationCenter;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Thierry Wasylczenko
 */
public class NotificationCenterTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Task<Void> indefiniteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                PlatformHelper.run(() -> ((SimpleStringProperty) this.titleProperty()).set("Indefinite task"));

                while(true) {
                    Thread.sleep(5000);
                }
            }
        };

        final Task<Void> errorTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                PlatformHelper.run(() -> ((SimpleStringProperty) this.titleProperty()).set("Error task"));
                throw new NullPointerException("This is a voluntary error");
            }

            @Override
            protected void failed() {
                super.failed();
                this.updateMessage("Oops");
            }
        };

        final Task<Void> successfulTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                PlatformHelper.run(() -> ((SimpleStringProperty) this.titleProperty()).set("Successful task"));
                return null;
            }
        };

        final NotificationCenter center = new NotificationCenter();

        PlatformHelper.run(() -> {
            center.setCurrentTask(indefiniteTask);
            center.setCurrentTask(errorTask);
            center.setCurrentTask(successfulTask);

            new Thread(indefiniteTask).start();
            new Thread(errorTask).start();
            new Thread(successfulTask).start();
        });

        final Scene scene = new Scene(center, 500, 300);
        scene.getStylesheets().addAll(
                ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/css/Default.css"),
                ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/css/SlideshowFX.css")
        );
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

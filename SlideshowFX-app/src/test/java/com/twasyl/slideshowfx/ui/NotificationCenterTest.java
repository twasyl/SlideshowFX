package com.twasyl.slideshowfx.ui;

import com.twasyl.slideshowfx.controls.notification.NotificationCenter;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import com.twasyl.slideshowfx.utils.concurrent.SlideshowFXTask;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Thierry Wasylczenko
 */
public class NotificationCenterTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final SlideshowFXTask<Void> indefiniteTask = new SlideshowFXTask<Void>() {
            @Override
            protected Void call() throws Exception {
                PlatformHelper.run(() -> ((SimpleStringProperty) this.titleProperty()).set("Indefinite task"));

                while(true) {
                    Thread.sleep(5000);
                }
            }
        };

        final SlideshowFXTask<Void> errorTask = new SlideshowFXTask<Void>() {
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

        final SlideshowFXTask<Void> successfulTask = new SlideshowFXTask<Void>() {
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

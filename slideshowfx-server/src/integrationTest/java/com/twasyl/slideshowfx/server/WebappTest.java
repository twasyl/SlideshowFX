package com.twasyl.slideshowfx.server;

import com.twasyl.slideshowfx.server.service.*;
import com.twasyl.slideshowfx.utils.NetworkUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import static java.util.concurrent.TimeUnit.SECONDS;

public class WebappTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final String ip = NetworkUtils.getIP();
        final int port = 50080;
        final SlideshowFXServer server = SlideshowFXServer.create(ip, port, null);
        server.start(WebappService.class,
                AttendeeChatService.class,
                PresenterChatService.class,
                QuizService.class,
                TwitterService.class).get(5, SECONDS);


        final WebView browser = new WebView();
        browser.getEngine().load("http://" + ip + ":" + port + "/slideshowfx");

        final Scene scene = new Scene(browser);
        primaryStage.setScene(scene);
        primaryStage.show();
        System.out.println("http://" + ip + ":" + port + "/slideshowfx");
    }

    @Override
    public void stop() throws Exception {
        SlideshowFXServer.getSingleton().stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

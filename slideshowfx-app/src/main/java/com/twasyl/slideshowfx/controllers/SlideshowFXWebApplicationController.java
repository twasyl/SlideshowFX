package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.server.SlideshowFXServer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller allowing to display the SlideshowFX web application.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class SlideshowFXWebApplicationController implements Initializable {

    @FXML private WebView browser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(SlideshowFXServer.getSingleton() != null) {
            this.browser.getEngine().load(
                    String.format("http://%1$s:%2$s/%3$s",
                            SlideshowFXServer.getSingleton().getHost(),
                            SlideshowFXServer.getSingleton().getPort(),
                            SlideshowFXServer.CONTEXT_PATH));

        }
    }
}

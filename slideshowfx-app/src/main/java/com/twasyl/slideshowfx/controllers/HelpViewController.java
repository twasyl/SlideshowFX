package com.twasyl.slideshowfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class of the {@code HelpView.fxml} view.
 *
 * @author Thierry Wasylczenko
 * @version 1.3-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class HelpViewController implements ThemeAwareController {

    @FXML
    private StackPane root;
    @FXML
    private WebView userDocumentationBrowser;
    @FXML
    private WebView developerDocumentationBrowser;

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public void postInitialize(URL location, ResourceBundle resources) {
        this.loadUserDocumentation();
        this.loadDeveloperDocumentation();
    }

    protected void loadUserDocumentation() {
        this.userDocumentationBrowser.getEngine().load(getUserDocumentation());
    }

    protected void loadDeveloperDocumentation() {
        this.developerDocumentationBrowser.getEngine().load(getDeveloperDocumentation());
    }

    protected String getUserDocumentation() {
        return HelpViewController.class.getResource("/com/twasyl/slideshowfx/documentation/SlideshowFX_user.html").toExternalForm();
    }

    protected String getDeveloperDocumentation() {
        return HelpViewController.class.getResource("/com/twasyl/slideshowfx/documentation/SlideshowFX_developer.html").toExternalForm();
    }
}

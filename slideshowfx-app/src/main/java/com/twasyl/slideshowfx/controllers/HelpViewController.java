package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.utils.io.IOUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class of the {@code HelpView.fxml} view.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
public class HelpViewController implements Initializable {

    @FXML
    private WebView userDocumentationBrowser;
    @FXML
    private WebView developerDocumentationBrowser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.loadUserDocumentation();
        this.loadDeveloperDocumentation();
    }

    protected void loadUserDocumentation() {
        this.userDocumentationBrowser.getEngine().loadContent(getUserDocumentation());
    }

    protected void loadDeveloperDocumentation() {
        this.developerDocumentationBrowser.getEngine().loadContent(getDeveloperDocumentation());
    }

    protected String getUserDocumentation() {
        return this.getDocumentation("/com/twasyl/slideshowfx/documentation/html/SlideshowFX_user.html");
    }

    protected String getDeveloperDocumentation() {
        return this.getDocumentation("/com/twasyl/slideshowfx/documentation/html/SlideshowFX_developer.html");
    }

    protected String getDocumentation(final String documentationFile) {
        final String documentation = IOUtils.read(HelpViewController.class.getResourceAsStream(documentationFile));
        return documentation;
    }
}

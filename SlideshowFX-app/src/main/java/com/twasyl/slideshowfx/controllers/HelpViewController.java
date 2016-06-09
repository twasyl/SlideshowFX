package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import org.asciidoctor.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class of the {@code HelpView.fxml} view.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class HelpViewController implements Initializable {

    @FXML private WebView userDocumentationBrowser;
    @FXML private WebView developerDocumentationBrowser;

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
        return this.getDocumentation("/com/twasyl/slideshowfx/documentation/SlideshowFX_user.asciidoc");
    }

    protected String getDeveloperDocumentation() {
        return this.getDocumentation("/com/twasyl/slideshowfx/documentation/SlideshowFX_developer.asciidoc");
    }

    protected String getDocumentation(final String documentationFile) {
        final String documentationOriginalContent = ResourceHelper.readResource(documentationFile);
        final String documentation = this.getAsciidoctorConverter().convert(documentationOriginalContent, this.getAsciidoctorOptions());
        return documentation;
    }

    protected Asciidoctor getAsciidoctorConverter() {
        final Asciidoctor asciidoctor = Asciidoctor.Factory.create(HelpViewController.class.getClassLoader());
        return asciidoctor;
    }

    protected Options getAsciidoctorOptions() {
        final Options options = OptionsBuilder.options()
                .attributes(getAsciidoctorAttributes())
                .headerFooter(true)
                .get();

        return options;
    }

    protected Attributes getAsciidoctorAttributes() {
        final Attributes attributes = AttributesBuilder.attributes()
                .backend("html5")
                .linkCss(false)
                .experimental(true)
                .tableOfContents(Placement.LEFT)
                .styleSheetName("slideshowfx.css")
                .stylesDir(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/documentation/css"))
                .imagesDir(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/documentation/images"))
                .noFooter(true)
                .get();

        return attributes;
    }
}

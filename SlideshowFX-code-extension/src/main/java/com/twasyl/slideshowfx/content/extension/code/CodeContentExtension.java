package com.twasyl.slideshowfx.content.extension.code;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.content.extension.code.controllers.CodeContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.MULTILINE;

/**
 * The CodeContentExtension extends the AbstractContentExtension. It allows to build a content containing code to insert
 * inside a SlideshowFX presentation.
 * This extension uses PrismJS in order to manage programming language syntax coloration.
 * This extension supports HTML and Textile markup languages.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class CodeContentExtension extends AbstractContentExtension {
    private static final Logger LOGGER = Logger.getLogger(CodeContentExtension.class.getName());

    protected CodeContentExtensionController controller;

    public CodeContentExtension() {
        super("CODE",
                CodeContentExtension.class.getResource("/com/twasyl/slideshowfx/content/extension/code/resources/prism.zip"),
                FontAwesomeIcon.CODE,
                "Insert code",
                "Insert code");

        final String baseURL = "prism/";

        // Add URL
        this.putResource(ResourceType.CSS_FILE, baseURL.concat("prism.css"));
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("prism.js"));
    }

    @Override
    public Pane getUI() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("/com/twasyl/slideshowfx/content/extension/code/fxml/CodeContentExtension.fxml"));
        Pane root = null;

        try {
            loader.setClassLoader(getClass().getClassLoader());
            root = loader.load();
            this.controller = loader.getController();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not load UI for CodeContentExtension", e);
        }

        return root;
    }

    @Override
    public String buildContentString(IMarkup markup) {
        final StringBuilder builder = new StringBuilder();

        if(markup == null || "HTML".equals(markup.getCode())) {
            builder.append(this.buildDefaultContentString());
        } else if("TEXTILE".equals(markup.getCode())) {
            builder.append(buildTextileContentString());
        } else {
            builder.append(this.buildDefaultContentString());
        }

        return builder.toString();
    }

    @Override
    public String buildDefaultContentString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("<pre class=\"")
                .append(this.controller.getLanguage().getCssClass());

        if(this.controller.isShowingLineNumbers()) {
            builder.append(" line-numbers");
        }

        builder.append("\"><code class=\"")
                .append(this.controller.getLanguage().getCssClass())
                .append("\">")
                .append(this.controller.getCode())
                .append("</code></pre>");

        return builder.toString();
    }

    private String buildTextileContentString() {
        final StringBuilder builder = new StringBuilder("bc(")
                .append(this.controller.getLanguage().getCssClass());

        if(this.controller.isShowingLineNumbers()) {
            builder.append(" line-numbers");
        }

        builder.append(")")
               .append(codeContainsBlankLines(this.controller.getCode()) ? ".." : ".")
               .append(" ")
               .append(this.controller.getCode());

        return builder.toString();
    }

    private boolean codeContainsBlankLines(final String code) {
        final Pattern pattern = Pattern.compile("^\\s*$", MULTILINE);
        final Matcher matcher = pattern.matcher(code);

        return matcher.find();
    }
}

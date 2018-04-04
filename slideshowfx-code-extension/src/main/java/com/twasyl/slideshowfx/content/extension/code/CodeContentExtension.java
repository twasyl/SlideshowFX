package com.twasyl.slideshowfx.content.extension.code;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.content.extension.code.controllers.CodeContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.twasyl.slideshowfx.icons.Icon.CODE;
import static java.util.regex.Pattern.MULTILINE;

/**
 * The CodeContentExtension extends the AbstractContentExtension. It allows to build a content containing code to insert
 * inside a SlideshowFX presentation.
 * This extension uses PrismJS in order to manage programming language syntax coloration.
 * This extension supports HTML and Textile markup languages.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
public class CodeContentExtension extends AbstractContentExtension {
    private static final Logger LOGGER = Logger.getLogger(CodeContentExtension.class.getName());
    protected static final String LINE_NUMBERS_CSS_CLASS = "line-numbers";

    protected CodeContentExtensionController controller;

    public CodeContentExtension() {
        super("CODE",
                CodeContentExtension.class.getResource("/com/twasyl/slideshowfx/content/extension/code/resources/prism.zip"),
                CODE,
                "Insert code",
                "Insert code");

        final String baseURL = "prism/1.11.0/";

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

        if (markup == null || "HTML".equals(markup.getCode())) {
            builder.append(this.buildDefaultContentString());
        } else if ("TEXTILE".equals(markup.getCode())) {
            builder.append(this.buildTextileContentString());
        } else if ("MARKDOWN".equals(markup.getCode()) && !this.controller.isShowingLineNumbers()) {
            builder.append(this.buildMarkdownContentString());
        } else {
            builder.append(this.buildDefaultContentString());
        }

        return builder.toString();
    }

    @Override
    public String buildDefaultContentString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("<pre").append(this.buildDefaultCssClass()).append("><code").append(this.buildDefaultCssClass())
                .append(">")
                .append(this.controller.getCode())
                .append("</code></pre>");

        return builder.toString();
    }

    protected String buildDefaultCssClass() {
        final String prefix = " class=\"";
        final String suffix = "\"";
        final StringJoiner cssClass = new StringJoiner(" ", prefix, suffix);

        if (this.controller.getLanguage() != null) cssClass.add(this.controller.getLanguage().getCssClass());

        if (this.controller.isShowingLineNumbers()) cssClass.add(LINE_NUMBERS_CSS_CLASS);

        return cssClass.length() == (prefix + suffix).length() ? "" : cssClass.toString();
    }

    protected String buildTextileContentString() {
        final StringBuilder builder = new StringBuilder("bc").append(this.buildTextileCssClass())
                .append(codeContainsBlankLines(this.controller.getCode()) ? ".." : ".")
                .append(" ")
                .append(this.controller.getCode());

        return builder.toString();
    }

    protected String buildTextileCssClass() {
        final StringJoiner cssClass = new StringJoiner(" ", "(", ")");

        if (this.controller.getLanguage() != null) cssClass.add(this.controller.getLanguage().getCssClass());

        if (this.controller.isShowingLineNumbers()) cssClass.add(LINE_NUMBERS_CSS_CLASS);

        return cssClass.length() == 2 ? "" : cssClass.toString();
    }

    protected String buildMarkdownContentString() {
        final StringBuilder builder = new StringBuilder("```");

        if (this.controller.getLanguage() != null) {
            builder.append(this.controller.getLanguage().getCssClass());
        }

        builder.append("\n").append(this.controller.getCode()).append("\n```");

        return builder.toString();
    }

    protected boolean codeContainsBlankLines(final String code) {
        final Pattern pattern = Pattern.compile("^\\s*$", MULTILINE);
        final Matcher matcher = pattern.matcher(code);

        return matcher.find();
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        return this.controller.areInputsValid();
    }
}

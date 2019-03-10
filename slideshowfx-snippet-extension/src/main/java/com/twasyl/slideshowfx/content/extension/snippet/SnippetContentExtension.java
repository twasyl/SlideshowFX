package com.twasyl.slideshowfx.content.extension.snippet;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.snippet.controllers.SnippetContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import javafx.beans.property.ReadOnlyBooleanProperty;

import java.util.Base64;

import static com.twasyl.slideshowfx.content.extension.ResourceType.CSS_FILE;
import static com.twasyl.slideshowfx.content.extension.ResourceType.JAVASCRIPT_FILE;
import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;
import static com.twasyl.slideshowfx.icons.FontAwesome.*;
import static com.twasyl.slideshowfx.icons.Icon.TERMINAL;

/**
 * The content extension that allows to insert code snippet in a presentation. This extension only supports HTML for
 * inserting the content in the presentation, meaning that HTML code will always be returned when calling
 * {@link #buildDefaultContentString()} and {@link #buildContentString(IMarkup)}.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class SnippetContentExtension extends AbstractContentExtension<SnippetContentExtensionController> {

    public SnippetContentExtension() {
        super("SNIPPET",
                SnippetContentExtension.class.getClassLoader().getResource("/com/twasyl/slideshowfx/content/extension/snippet/fxml/SnippetContentExtension.fxml"),
                SnippetContentExtension.class.getResource("/com/twasyl/slideshowfx/content/extension/snippet/resources/snippet-executor.zip"),
                TERMINAL,
                "Insert an executable code snippet", "Insert an executable code snippet");

        final String baseURL = "snippet-executor/";

        // Add URL
        this.putResource(CSS_FILE, baseURL.concat("prism/1.15.0/prism.css"));
        this.putResource(JAVASCRIPT_FILE, String.format("%sfont-awesome/%s/js/%s", baseURL, getFontAwesomeVersion(), getFontAwesomeJSFilename()), getFontAwesomeJSFile());
        this.putResource(JAVASCRIPT_FILE, baseURL.concat("prism/1.15.0/prism.js"));
    }

    @Override
    public String buildContentString(IMarkup markup) {
        return this.buildDefaultContentString();
    }

    @Override
    public String buildDefaultContentString() {
        final StringBuilder builder = new StringBuilder();

        long id = System.currentTimeMillis();
        final String codeSnippetId = "code-snippet-" + id;
        final String codeSnippetConsoleOutputId = "code-snippet-output-" + id;
        final String codeSnippetConsoleId = "code-snippet-console-" + id;
        final String executeCodeSnippetId = "code-snippet-execute-" + id;

        builder.append("<div style=\"width: 100%; height: 50px; background-color: #ECECEC;")
                .append("border-radius: 10px 10px 0 0\" id=\"").append(codeSnippetId).append("\">\n")
                .append("   <i id=\"").append(executeCodeSnippetId).append("\" class=\"fas fa-terminal fa-fw\" ")
                .append("onclick=\"javascript:executeCodeSnippet('")
                .append(this.getController().getSnippetExecutor().getCode())
                .append("', '").append(Base64.getEncoder().encodeToString(this.getController().getCodeSnippet().toJson().getBytes(getDefaultCharset()))).append("', '")
                .append(id).append("');\"></i>\n")
                .append("</div>\n")
                .append("<pre id=\"").append(codeSnippetConsoleId).append("\" style=\"margin-top: 0\" ")
                .append("class=\"").append(this.getController().getSnippetExecutor().getCssClass()).append("\">")
                .append("<code class=\"").append(this.getController().getSnippetExecutor().getCssClass()).append("\">")
                .append(this.getController().getCodeSnippet().getCode())
                .append("</code></pre>\n")
                .append("<pre id=\"").append(codeSnippetConsoleOutputId).append("\" class=\"language-bash\" style=\"display: none; margin-top: 0;\">")
                .append("<code class=\"language-bash\"></code></pre>");
        return builder.toString();
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        return this.getController().areInputsValid();
    }
}

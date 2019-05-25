package com.twasyl.slideshowfx.engine.presentation.configuration;


import com.twasyl.slideshowfx.engine.Variable;
import com.twasyl.slideshowfx.engine.template.configuration.SlideElementTemplate;
import com.twasyl.slideshowfx.utils.TemplateProcessor;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;
import static java.util.logging.Level.SEVERE;

/**
 * Represent an element of a slide that can be modified by the user. It is typically the title of a slide, it's content
 * and so on.
 *
 * @author Thierry Wasylczenko
 * @version 1.1-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class SlideElement {
    private static final Logger LOGGER = Logger.getLogger(SlideElement.class.getName());

    private String id;
    private SlideElementTemplate template;
    private String htmlContent;
    private String originalContent;
    private String originalContentCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public String getHtmlContentAsBase64() {
        return Base64.getEncoder().encodeToString(getHtmlContent().getBytes(getDefaultCharset()));
    }

    /**
     * Replace all variables stored in the given {@code variables} in the HTML content and return a variable free
     * HTML content. If the current HTML content is {@code null} or empty, an empty String is returned.
     * The original HTML content is not affected by the modification.
     *
     * @return A variable free HTML content.
     */
    public String getClearedHtmlContent(Set<Variable> variables) {
        final StringBuilder builder = new StringBuilder();

        if (this.htmlContent != null && !this.htmlContent.isEmpty()) {
            final Map<String, String> tokens = variables.stream().collect(Collectors.toMap(Variable::getName, Variable::getValue));

            try (StringWriter writer = new StringWriter()) {
                final Template htmlContentTemplate = new Template("variable", new StringReader(this.getHtmlContent()), TemplateProcessor.getDefaultConfiguration());
                htmlContentTemplate.process(tokens, writer);
                writer.flush();

                builder.append(writer.toString());
            } catch (IOException | TemplateException e) {
                LOGGER.log(SEVERE, "Can not get a variable free HTML content", e);
            }
        }
        return builder.toString();
    }

    /**
     * Sets the HTML content for this {@link SlideElement}. The HTML content must not be encoded.
     *
     * @param htmlContent The HTML content.
     */
    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    /**
     * Sets the HTML content for this {@link SlideElement}. The provided HTML content is decoded and then set using
     * {@link #setHtmlContent(String)}.
     *
     * @param htmlContentAsBase64 The HTML content encoded in Base64.
     */
    public void setHtmlContentAsBase64(String htmlContentAsBase64) {
        final byte[] bytes = Base64.getDecoder().decode(htmlContentAsBase64);
        setHtmlContent(new String(bytes, getDefaultCharset()));
    }

    /**
     * Get the original content of this {@link SlideElement}. The content is not encoded.
     *
     * @return The original content.
     */
    public String getOriginalContent() {
        return originalContent;
    }

    /**
     * Get the original content of this {@link SlideElement}. The content is encoded in Base64.
     *
     * @return The encoded original content.
     */
    public String getOriginalContentAsBase64() {
        return Base64.getEncoder().encodeToString(getOriginalContent().getBytes(getDefaultCharset()));
    }

    /**
     * Sets the original content of this {@link SlideElement}. The original content must not be encoded.
     *
     * @param originalContent The original content.
     */
    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    /**
     * Sets the original content of this {@link SlideElement}. The original content is decoded and set using {@link #setOriginalContent(String)}.
     *
     * @param originalContentAsBase64 The original content encoded in Base64.
     */
    public void setOriginalContentAsBase64(String originalContentAsBase64) {
        final byte[] bytes = Base64.getDecoder().decode(originalContentAsBase64);
        setOriginalContent(new String(bytes, getDefaultCharset()));
    }

    public String getOriginalContentCode() {
        return originalContentCode;
    }

    public void setOriginalContentCode(String originalContentCode) {
        this.originalContentCode = originalContentCode;
    }

    public SlideElementTemplate getTemplate() {
        return template;
    }

    public void setTemplate(SlideElementTemplate template) {
        this.template = template;
    }
}

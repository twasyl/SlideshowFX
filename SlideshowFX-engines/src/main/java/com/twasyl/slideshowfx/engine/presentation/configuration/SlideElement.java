package com.twasyl.slideshowfx.engine.presentation.configuration;


import com.twasyl.slideshowfx.engine.template.configuration.SlideElementTemplate;
import com.twasyl.slideshowfx.utils.TemplateProcessor;
import com.twasyl.slideshowfx.utils.beans.Pair;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SlideElement {
    private static final Logger LOGGER = Logger.getLogger(SlideElement.class.getName());

    private String id;
    private SlideElementTemplate template;
    private String htmlContent;
    private String originalContent;
    private String originalContentCode;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHtmlContent() { return htmlContent; }

    public String getHtmlContentAsBase64() {
        String base64 = null;
        try {
            base64 = Base64.getEncoder().encodeToString(getHtmlContent().getBytes("UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return base64;
    }

    /**
     * Replace all variables stored in the given {@code variables} in the HTML content and return a variable free
     * HTML content. If the current HTML content is {@code null} or empty, an empty String is returned.
     * The original HTML content is not affected by the modification.
     * @return A variable free HTML content.
     */
    public String getClearedHtmlContent(Set<Pair<String, String>> variables) {
        final StringBuilder builder = new StringBuilder();

        if(this.htmlContent != null && !this.htmlContent.isEmpty()) {
            final Map<String, String> tokens = variables.stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue));

            try (StringWriter writer = new StringWriter()) {
                final Template template = new Template("variable", new StringReader(this.getHtmlContent()), TemplateProcessor.getDefaultConfiguration());
                template.process(tokens, writer);
                writer.flush();

                builder.append(writer.toString());
            } catch (IOException | TemplateException e) {
                LOGGER.log(Level.SEVERE, "Can not get a variable free HTML content", e);
            }
        }
        return builder.toString();
    }

    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }

    public void setHtmlContentAsBase64(String htmlContentAsBase64) {
        setHtmlContent(new String(
                Base64.getDecoder().decode(htmlContentAsBase64.getBytes())
        ));
    }

    public String getOriginalContent() { return originalContent; }

    public String getOriginalContentAsBase64() {
        String base64 = null;
        try {
            base64 = Base64.getEncoder().encodeToString(getOriginalContent().getBytes("UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return base64;
    }

    public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }

    public void setOriginalContentAsBase64(String originalContentAsBase64) {
        setOriginalContent(new String(
                Base64.getDecoder().decode(originalContentAsBase64.getBytes())
        ));
    }

    public String getOriginalContentCode() { return originalContentCode; }
    public void setOriginalContentCode(String originalContentCode) { this.originalContentCode = originalContentCode; }

    public SlideElementTemplate getTemplate() { return template; }
    public void setTemplate(SlideElementTemplate template) { this.template = template; }
}

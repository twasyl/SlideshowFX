package com.twasyl.slideshowfx.engine.template;

/**
 * This class represents attributes that should be defined before adding a slide to a presentation
 */
public class DynamicAttribute {
    private String attribute;
    private String promptMessage;
    private String templateExpression;

    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }

    public String getPromptMessage() { return promptMessage; }
    public void setPromptMessage(String promptMessage) {  this.promptMessage = promptMessage; }

    public String getTemplateExpression() { return templateExpression; }
    public void setTemplateExpression(String templateExpression) { this.templateExpression = templateExpression; }
}

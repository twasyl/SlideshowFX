/*
 * Copyright 2014 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.builder.template;

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

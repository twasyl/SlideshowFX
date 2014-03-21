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

package com.twasyl.slideshowfx.builder;


import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class SlideElement {
    private String id;
    private String htmlContent;
    private String originalContent;
    private String originalContentCode;
    private String defaultContent;

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

    public String getDefaultContent() { return defaultContent; }
    public void setDefaultContent(String defaultContent) { this.defaultContent = defaultContent; }
}

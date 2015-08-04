/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.content.extension;

/**
 * Indicates the type of resources contained in the content extension.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public enum ResourceType {
    /**
     * Indicates the resource is a JavaScript file. According this, when inserting it as custom resource, it should
     * be included as a {@code <script type="text/javascript" src="..."></script>} block.
     */
    JAVASCRIPT_FILE,
    /**
     * Indicates the resource is a CSS file. According this, when inserting it as custom resource, it should be
     * included as a {@code <link rel="stylesheet" href="...">} block.
     */
    CSS_FILE,
    /**
     * Indicates the resource is a JavaScript script. According this, when inserting it as custom resource, it should be
     * included as a {@code <script type="text/javascript">...</script>} block.
     */
    SCRIPT,
    /**
     * Indicates the resource is a CSS fragment. According this, when inserting it as custom resource, it should be be
     * included as a {@code <style>...</style>} block.
     */
    CSS
}

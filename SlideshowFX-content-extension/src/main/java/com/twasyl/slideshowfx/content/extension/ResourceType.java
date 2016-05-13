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

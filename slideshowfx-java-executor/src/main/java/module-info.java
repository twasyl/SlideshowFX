module slideshowfx.java.executor {
    provides com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor with com.twasyl.slideshowfx.snippet.executor.java.JavaSnippetExecutor;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.snippet.executor.java.JavaSnippetExecutor;

    requires java.logging;
    requires javafx.controls;
    requires javafx.graphics;
    requires slideshowfx.global.configuration;
    requires slideshowfx.snippet.executor;
    requires slideshowfx.utils;
}
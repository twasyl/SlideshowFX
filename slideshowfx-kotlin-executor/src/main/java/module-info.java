module slideshowfx.kotlin.executor {
    provides com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor with com.twasyl.slideshowfx.snippet.executor.kotlin.KotlinSnippetExecutor;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.snippet.executor.kotlin.KotlinSnippetExecutor;

    requires java.logging;
    requires javafx.controls;
    requires javafx.graphics;
    requires slideshowfx.global.configuration;
    requires slideshowfx.snippet.executor;
    requires slideshowfx.utils;
}
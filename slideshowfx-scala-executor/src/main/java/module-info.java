module slideshowfx.scala.executor {
    provides com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor with com.twasyl.slideshowfx.snippet.executor.scala.ScalaSnippetExecutor;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.snippet.executor.scala.ScalaSnippetExecutor;

    requires java.logging;
    requires javafx.controls;
    requires javafx.graphics;
    requires slideshowfx.global.configuration;
    requires slideshowfx.snippet.executor;
    requires slideshowfx.utils;
}
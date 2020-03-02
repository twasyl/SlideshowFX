module slideshowfx.ruby.executor {

    provides com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor with com.twasyl.slideshowfx.snippet.executor.ruby.RubySnippetExecutor;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.snippet.executor.ruby.RubySnippetExecutor;

    requires java.logging;
    requires javafx.controls;
    requires javafx.graphics;
    requires slideshowfx.global.configuration;
    requires slideshowfx.snippet.executor;
    requires slideshowfx.utils;
}
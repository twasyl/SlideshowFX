module slideshowfx.javascript.executor {
    provides com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor with com.twasyl.slideshowfx.snippet.executor.javascript.JavaScriptSnippetExecutor;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.snippet.executor.javascript.JavaScriptSnippetExecutor;

    requires java.logging;
    requires java.scripting;
    requires javafx.graphics;
    requires slideshowfx.snippet.executor;
}
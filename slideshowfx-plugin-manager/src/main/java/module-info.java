module slideshowfx.plugin.manager {
    exports com.twasyl.slideshowfx.plugin.manager;
    exports com.twasyl.slideshowfx.plugin.manager.internal;

    requires java.logging;
    requires slideshowfx.engines;
    requires slideshowfx.global.configuration;
    requires slideshowfx.plugin;
    requires slideshowfx.utils;

    uses com.twasyl.slideshowfx.plugin.IPlugin;
}
package com.twasyl.slideshowfx.plugin;

/**
 * A basic implementation of a {@link IPlugin}.
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 */
public class AbstractPlugin<T extends IPluginOptions> implements IPlugin<T> {
    private String name;
    private T options;

    /**
     * The constructor to create an instance of a {@link IPlugin}.
     * @param name The name of the plugin.
     */
    protected AbstractPlugin(final String name) {
        this.name = name;
        this.options = null;
    }

    /**
     * Creates a {@link IPlugin} with a given name and default options.
     * @param name The name of the plugin.
     * @param options The options of the plugin.
     */
    protected AbstractPlugin(final String name, final T options) {
        this(name);
        this.options = options;
    }

    @Override
    public String getName() { return this.name; }

    @Override
    public T getOptions() { return this.options; }

    @Override
    public void setOptions(T options) throws NullPointerException {
        if(options == null) throw new NullPointerException("The options can not be null");

        this.options = options;
    }
}

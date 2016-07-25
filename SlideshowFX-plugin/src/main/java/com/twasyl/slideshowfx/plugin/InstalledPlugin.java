package com.twasyl.slideshowfx.plugin;

/**
 * A representation of an installed plugin. It is composed by a name and version.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class InstalledPlugin extends AbstractPlugin {
    private String version;

    public InstalledPlugin(String name, String version) {
        super(name);
        this.version = version;
    }

    /**
     * Get the version of the plugin.
     * @return The version of the plugin.
     */
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "InstalledPlugin{" +
                "name='" + getName() + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}

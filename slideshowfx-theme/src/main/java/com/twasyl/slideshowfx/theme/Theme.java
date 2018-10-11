package com.twasyl.slideshowfx.theme;

/*
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */

import java.net.URL;

public class Theme {
    private String name;
    private URL cssFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getCssFile() {
        return cssFile;
    }

    public void setCssFile(URL cssFile) {
        this.cssFile = cssFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Theme theme = (Theme) o;

        return name.equals(theme.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

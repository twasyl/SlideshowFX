package com.twasyl.slideshowfx.style.theme;

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
    private String slideEditorTheme;

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

    public String getSlideEditorTheme() {
        return slideEditorTheme;
    }

    public void setSlideEditorTheme(String slideEditorTheme) {
        this.slideEditorTheme = slideEditorTheme;
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

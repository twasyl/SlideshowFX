package com.twasyl.slideshowfx.icons;

import static com.twasyl.slideshowfx.icons.FontType.BRAND;
import static com.twasyl.slideshowfx.icons.FontType.REGULAR;
import static com.twasyl.slideshowfx.icons.FontType.SOLID;

/**
 * Enum representing FontAwesome icons supported.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 2.0
 */
public enum Icon {
    ARCHIVE("\uf187", SOLID),
    ARROW_ALT_CIRCLE_LEFT("\uf359", REGULAR),
    ARROW_ALT_CIRCLE_RIGHT("\uf35a", REGULAR),
    ARROW_RIGHT("\uf061", SOLID),
    CHECK_CIRCLE("\uf058", SOLID),
    CHEVRON_LEFT("\uf053", SOLID),
    CHEVRON_RIGHT("\uf054", SOLID),
    CODE("\uf121", SOLID),
    COG("\uf013", SOLID),
    COMMENTS_O("\uf086", REGULAR),
    DESKTOP("\uf108", SOLID),
    EXCLAMATION_CIRCLE("\uf06a", SOLID),
    EXCLAMATION_TRIANGLE("\uf071", SOLID),
    FILE("\uf15b", SOLID),
    FILE_TEXT_ALT("\uf0f6", SOLID),
    FILES("\uf0c5", SOLID),
    FLOPPY("\uf0c7", SOLID),
    FOLDER("\uf07b", SOLID),
    FOLDER_OPEN("\uf07c", SOLID),
    LINK("\uf0c1", SOLID),
    REFRESH("\uf021", SOLID),
    PICTURE_ALT("\uf03e", REGULAR),
    PLAY("\uf04b", SOLID),
    PLUS("\uf067", SOLID),
    PLUS_SQUARE("\uf0fe", SOLID),
    POWER_OFF("\uf011", SOLID),
    PRINT("\uf02f", SOLID),
    QRCODE("\uf029", SOLID),
    QUESTION("\uf128", SOLID),
    QUOTE_LEFT("\uf10d", SOLID),
    SHARE_ALT_SQUARE("\uf1e1", SOLID),
    SIGN_OUT_ALT("\uf2f5", SOLID),
    SPINNER("\uf110", SOLID),
    STAR("\uf005", REGULAR),
    TERMINAL("\uf120", SOLID),
    TIMES("\uf00d", SOLID),
    TIMES_CIRCLE("\uf057", REGULAR),
    TRASH_ALT("\uf2ed", REGULAR),
    TWITTER("\uf099", BRAND);

    private final String unicode;
    private final FontType type;

    Icon(final String unicode, final FontType fontType) {
        this.unicode = unicode;
        this.type = fontType;
    }

    public String getUnicode() {
        return unicode;
    }

    public FontType getType() {
        return type;
    }
}

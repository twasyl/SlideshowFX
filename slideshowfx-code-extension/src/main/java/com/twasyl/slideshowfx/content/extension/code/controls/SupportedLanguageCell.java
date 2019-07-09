package com.twasyl.slideshowfx.content.extension.code.controls;

import com.twasyl.slideshowfx.content.extension.code.enums.SupportedLanguage;
import javafx.scene.control.ListCell;

/**
 * Implementation of the {@link ListCell} of {@link SupportedLanguage}. The text of the cell is the
 * {@link SupportedLanguage#getName() name} of the {@link SupportedLanguage language}.
 *
 * @author Thierry Wasylczenko
 * @version 1.1-SNAPSHOT
 * @since SlideshowFX 1.3
 */
public class SupportedLanguageCell extends ListCell<SupportedLanguage> {

    public SupportedLanguageCell() {
        getStyleClass().add("supported-language-cell");
    }

    @Override
    protected void updateItem(SupportedLanguage language, boolean empty) {
        super.updateItem(language, empty);

        if (language != null && !empty) {
            this.setText(language.getName());
        } else {
            this.setText(null);
        }
    }
}

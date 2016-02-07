package com.twasyl.slideshowfx.content.extension.snippet.controls;

import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;

/**
 * This class extends the {@link javafx.scene.control.ListCell} in order to display a
 * {@link com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor}.
 * The cell displays the value returned vy {@link com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor#getLanguage()}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class SnippetExecutorListCell extends ListCell<ISnippetExecutor> {

    @Override
    protected void updateItem(ISnippetExecutor item, boolean empty) {
        super.updateItem(item, empty);

        if(item != null && !empty) {
            final Text text = new Text(item.getLanguage());
            text.getStyleClass().add("text");
            this.setGraphic(text);
        } else {
            this.setGraphic(null);
        }
    }
}

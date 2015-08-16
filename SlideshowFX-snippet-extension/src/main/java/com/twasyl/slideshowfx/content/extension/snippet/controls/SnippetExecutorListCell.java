/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

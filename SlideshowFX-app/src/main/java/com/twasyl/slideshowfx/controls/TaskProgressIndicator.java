/*
 * Copyright 2014 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.utils.PlatformHelper;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class TaskProgressIndicator extends HBox {

    private final ProgressBar bar = new ProgressBar(0);
    private final Label label = new Label();
    private final SequentialTransition labelTransition;

    public TaskProgressIndicator() {
        this.bar.setPrefWidth(200);

        getChildren().addAll(this.bar, this.label);
        setSpacing(5);

        final PauseTransition pause = new PauseTransition(Duration.seconds(2));

        final FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), this.label);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        this.labelTransition = new SequentialTransition(this.label, pause, fadeOut);
    }

    public String getText() { return this.label.getText(); }

    public void setText(String text) {
        PlatformHelper.run(() -> {
            this.label.setText(text);
            this.labelTransition.playFromStart();
        });
    }

    public double getProgress() { return this.bar.getProgress(); }
    public void setProgress(double progress) {
        PlatformHelper.run(() -> this.bar.setProgress(progress));
    }

    /**
     * Updates the TaskProgressIndicator with the given progress and text.
     * @param progress The new progress
     * @param text The new text
     */
    public void update(double progress, String text) {
        this.setProgress(progress);
        this.setText(text);
    }
}

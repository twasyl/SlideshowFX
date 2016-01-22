/*
 * Copyright 2016 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.utils.concurrent;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * An extension of a {@link Task} that holds the time when its state has changed. That time is updated each time the
 * state changes.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public abstract class SlideshowFXTask<V> extends Task<V> {
    private ReadOnlyObjectProperty<LocalTime> statusChangedTime = new SimpleObjectProperty<>();

    public SlideshowFXTask() {
        super();

        this.stateProperty().addListener((value, oldState, newState) -> {
            ((SimpleObjectProperty) this.statusChangedTime).set(LocalTime.now());
        });
    }

    public ReadOnlyObjectProperty<LocalTime> statusChangedTimeProperty() { return statusChangedTime; }
    public LocalTime getStatusChangedTime() { return statusChangedTime.get(); }
}

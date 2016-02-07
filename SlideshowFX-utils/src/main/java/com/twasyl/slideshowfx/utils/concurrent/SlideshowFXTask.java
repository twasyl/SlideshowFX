package com.twasyl.slideshowfx.utils.concurrent;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

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

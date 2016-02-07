package com.twasyl.slideshowfx.utils.beans.binding;

import javafx.beans.binding.ObjectExpression;
import javafx.beans.binding.StringBinding;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * This binding returns the time each time a {@link java.time.LocalTime} changes.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class LocalTimeBinding extends StringBinding {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
    private final ObjectExpression<LocalTime> time;

    public LocalTimeBinding(final ObjectExpression<LocalTime> time) {
        if(time == null) throw new NullPointerException("The time can not be null");

        this.time = time;
        super.bind(this.time);
    }

    @Override
    protected String computeValue() {
        return time.get() == null ? "" : time.get().format(formatter);
    }

    @Override
    public void dispose() {
        super.unbind(this.time);
    }
}

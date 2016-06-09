package com.twasyl.slideshowfx.utils.beans.binding;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.Test;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static org.junit.Assert.assertEquals;

/**
 * This class tests the {@link LocalTimeBinding} class.
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 */
public class LocalTimeBindingTest {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);

    /**
     * Test that creating a binding with a null property throws a {@link NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void testWithNullProperty() {
        final LocalTimeBinding binding = new LocalTimeBinding(null);
    }

    /**
     * Test that "" is returned for a property.
     */
    @Test public void testWithPropertyWithDefaultValue() {
        final ObjectProperty<LocalTime> prop = new SimpleObjectProperty<>();
        final LocalTimeBinding binding = new LocalTimeBinding(prop);

        assertEquals(binding.get(), "");
    }

    /**
     * Test that the time is returned for a property.
     */
    @Test public void testWithTrueProperty() {
        final LocalTime now = LocalTime.now();
        final ObjectProperty<LocalTime> prop = new SimpleObjectProperty<>(now);
        final LocalTimeBinding binding = new LocalTimeBinding(prop);

        assertEquals(now.format(formatter), binding.get());
    }

    @Test public void testWithChangingProperty() {
        final LocalTime now = LocalTime.now();
        final ObjectProperty<LocalTime> prop = new SimpleObjectProperty<>(now);
        final LocalTimeBinding binding = new LocalTimeBinding(prop);

        assertEquals(now.format(formatter), binding.get());

        final LocalTime after = now.plusHours(1);
        prop.set(after);

        assertEquals(after.format(formatter), binding.get());
    }
}

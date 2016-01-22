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

package com.twasyl.slideshowfx.utils.beans.binding;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
 * @since SlideshowFX 1.0.0
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

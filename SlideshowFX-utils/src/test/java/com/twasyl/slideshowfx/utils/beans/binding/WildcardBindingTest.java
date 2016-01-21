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
import javafx.beans.property.SimpleBooleanProperty;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This class tests the {@link WildcardBinding} class.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 */
public class WildcardBindingTest {
    /**
     * Test that creating a binding with a null property throws a {@link NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void testWithNullProperty() {
        final WildcardBinding binding = new WildcardBinding(null);
    }

    /**
     * Test that "" is returned for a property.
     */
    @Test public void testWithPropertyWithDefaultValue() {
        final BooleanProperty prop = new SimpleBooleanProperty();
        final WildcardBinding binding = new WildcardBinding(prop);

        assertEquals(binding.get(), "");
    }

    /**
     * Test that "*" is returned for a property.
     */
    @Test public void testWithTrueProperty() {
        final BooleanProperty prop = new SimpleBooleanProperty(true);
        final WildcardBinding binding = new WildcardBinding(prop);

        assertEquals(binding.get(), "*");
    }

    /**
     * Test that "" is returned for a property.
     */
    @Test public void testWithFalseProperty() {
        final BooleanProperty prop = new SimpleBooleanProperty(false);
        final WildcardBinding binding = new WildcardBinding(prop);

        assertEquals(binding.get(), "");
    }

    @Test public void testWithChangingProperty() {
        final BooleanProperty prop = new SimpleBooleanProperty(false);
        final WildcardBinding binding = new WildcardBinding(prop);

        assertEquals(binding.get(), "");

        prop.set(true);

        assertEquals(binding.get(), "*");
    }
}

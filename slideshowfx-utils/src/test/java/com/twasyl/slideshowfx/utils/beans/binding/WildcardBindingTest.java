package com.twasyl.slideshowfx.utils.beans.binding;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This class tests the {@link WildcardBinding} class.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 */
public class WildcardBindingTest {
    /**
     * Test that creating a binding with a null property throws a {@link NullPointerException}.
     */
    @Test
    public void testWithNullProperty() {
        assertThrows(NullPointerException.class, () -> new WildcardBinding(null));
    }

    /**
     * Test that "" is returned for a property.
     */
    @Test public void testWithPropertyWithDefaultValue() {
        final BooleanProperty prop = new SimpleBooleanProperty();
        final WildcardBinding binding = new WildcardBinding(prop);

        assertEquals("", binding.get());
    }

    /**
     * Test that "*" is returned for a property.
     */
    @Test public void testWithTrueProperty() {
        final BooleanProperty prop = new SimpleBooleanProperty(true);
        final WildcardBinding binding = new WildcardBinding(prop);

        assertEquals("*", binding.get());
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

        assertEquals("", binding.get());

        prop.set(true);

        assertEquals("*", binding.get());
    }
}

package com.twasyl.slideshowfx.utils.beans.binding;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * This class tests the {@link com.twasyl.slideshowfx.utils.beans.binding.FilenameBinding} class.
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class FilenameBindingTest {

    /**
     * Test that creating a binding with a null property throws a NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testWithNullProperty() {
        final FilenameBinding binding = new FilenameBinding(null);
    }

    /**
     * Test that "Untitled" is returned for a property having a file which is null.
     */
    @Test public void testWithPropertyWithNullValue() {
        final ObjectProperty<File> prop = new SimpleObjectProperty<>(null);
        final FilenameBinding binding = new FilenameBinding(prop);

        assertEquals("Untitled", binding.get());
    }

    /**
     * Test that the name of the file is returned when the binding is created with a file that exists
     */
    @Test public void testWithCorrectProperty() {
        final ObjectProperty<File> prop = new SimpleObjectProperty<>(new File("/Test.sfx"));
        final FilenameBinding binding = new FilenameBinding(prop);

        assertEquals("Test.sfx", binding.get());
    }

    /**
     * Tests that the value of the binding changes when the file changes.
     */
    @Test public void testWhenFileChanges() {
        final ObjectProperty<File> prop = new SimpleObjectProperty<>(null);
        final FilenameBinding binding = new FilenameBinding(prop);

        assertEquals("Untitled", binding.get());

        prop.set(new File("/Test.sfx"));
        assertEquals("Test.sfx", binding.get());
    }
}

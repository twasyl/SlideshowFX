package com.twasyl.slideshowfx.hosting.connector.io;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Performs test on the {@link RemoteFile} class.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class RemoteFileTest {

    /**
     * Ensure the {@link RemoteFile#toString()} method to return the expected value when a RemoteFile is constructed
     * with only the name and no parent.
     */
    @Test
    public void toStringWithOnlyName() {
        final RemoteFile parent = new RemoteFile("parent");

        assertEquals("/parent", parent.toString());
    }

    /**
     * Ensure the {@link RemoteFile#isEmpty()} returns {@code true} when the RemoteFile is constructed with only the
     * name and no parent.
     */
    @Test public void isEmptyWithOnlyName() {
        final RemoteFile parent = new RemoteFile("parent");

        assertTrue(parent.isEmpty());
    }

    /**
     * Ensure the {@link RemoteFile#toString()} method to return the expected value when a RemoteFile is constructed
     * with the name and a parent.
     */
    @Test public void toStringWithParent() {
        final RemoteFile parent = new RemoteFile("parent");
        final RemoteFile child = new RemoteFile(parent, "child");

        assertEquals("/parent/child", child.toString());
    }

    @Test public void toStringWithParentWithNullName() {
        final RemoteFile parent = new RemoteFile(null);
        final RemoteFile child = new RemoteFile(parent, "child");

        assertEquals("/child", child.toString());
    }

    /**
     * Ensure the {@link RemoteFile#toString()} method to return the expected value when a RemoteFile is the root folder.
     */
    @Test public void toStringForRoot() {
        final  RemoteFile root = new RemoteFile(null);

        assertEquals("/", root.toString());
    }

    /**
     * Ensure the {@link RemoteFile#isEmpty()} returns {@code false} when the RemoteFile is parent of another instance.
     */
    @Test public void isNotEmptyForParent() {
        final RemoteFile parent = new RemoteFile("parent");
        final RemoteFile child = new RemoteFile(parent, "child");

        assertFalse(parent.isEmpty());
    }
}

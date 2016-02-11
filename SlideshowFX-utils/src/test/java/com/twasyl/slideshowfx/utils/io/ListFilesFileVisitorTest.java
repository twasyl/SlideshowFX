package com.twasyl.slideshowfx.utils.io;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the class {@link ListFilesFileVisitor}
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0.0
 */
public class ListFilesFileVisitorTest {

    private final File baseLocation = new File("./src/test/resources/com/twasyl/slideshowfx/utils/io");
    private ListFilesFileVisitor visitor;

    @Before
    public void beforeTest() {
        this.visitor = new ListFilesFileVisitor();
    }

    @Test
    public void testWalkOnExistingFile() throws IOException {
        final File file = new File(baseLocation, "file.txt");
        Files.walkFileTree(file.toPath(), visitor);

        assertFalse(visitor.getPaths().isEmpty());
        assertEquals(file.toPath(), visitor.getPaths().get(0));
    }

    @Test(expected = NoSuchFileException.class)
    public void testWalkOnInvalidFile() throws IOException {
        final File file = new File(baseLocation, "this_file_dont_exist.txt");
        Files.walkFileTree(file.toPath(), visitor);
    }

    @Test
    public void testOnEmptyDir() throws IOException {
        final File emptyDir = new File(baseLocation, "emptyDir");
        Files.walkFileTree(emptyDir.toPath(), visitor);

        assertFalse(visitor.getPaths().isEmpty());
        assertEquals(emptyDir.toPath(), visitor.getPaths().get(0));
    }

    @Test
    public void testOnFileTree() throws IOException {
        final File emptyDir = baseLocation;
        Files.walkFileTree(emptyDir.toPath(), visitor);

        assertEquals(5, visitor.getPaths().size());
        assertTrue(visitor.getPaths().contains(new File(baseLocation, "dir/dir2").toPath()));
        assertTrue(visitor.getPaths().contains(new File(baseLocation, "dir/dir3/file2.txt").toPath()));
        assertTrue(visitor.getPaths().contains(new File(baseLocation, "dir/file1.txt").toPath()));
        assertTrue(visitor.getPaths().contains(new File(baseLocation, "emptyDir").toPath()));
        assertTrue(visitor.getPaths().contains(new File(baseLocation, "file.txt").toPath()));
    }
}

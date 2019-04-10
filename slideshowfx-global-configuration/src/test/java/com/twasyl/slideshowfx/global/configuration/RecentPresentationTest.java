package com.twasyl.slideshowfx.global.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Base64;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("A recent presentation")
public class RecentPresentationTest {

    @Test
    @DisplayName("has its ID equal to its normalized path encoded in Base64")
    void idIsNormalizedPathEncodedInBase64() {
        final RecentPresentation presentation = new RecentPresentation("presentation1.sfx", null);
        final String expectedId = Base64.getEncoder().encodeToString(presentation.getNormalizedPath().getBytes(getDefaultCharset()));
        assertEquals(expectedId, presentation.getId());
    }

    @Test
    @DisplayName("is equal to itself")
    void isEqualToItself() {
        final RecentPresentation presentation = new RecentPresentation("presentation1.sfx", null);
        assertTrue(presentation.equals(presentation));
    }

    @Test
    @DisplayName("is not equal to a null object")
    void notEqualToNull() {
        final RecentPresentation presentation = new RecentPresentation("presentation1.sfx", null);
        assertFalse(presentation.equals(null));
    }

    @Test
    @DisplayName("is not equal to an object of different type")
    void notEqualToDifferentType() {
        final RecentPresentation presentation = new RecentPresentation("presentation1.sfx", null);
        assertFalse(presentation.equals("Hello"));
    }

    @Test
    @DisplayName("is equal to another with same normalized path")
    void equalIfSameNormalizedPath() {
        final RecentPresentation presentation = new RecentPresentation("presentation1.sfx", null);
        final RecentPresentation another = new RecentPresentation("presentation1.sfx", null);
        assertTrue(presentation.equals(another));
    }

    @Test
    @DisplayName("can not be created with a null path")
    void cantCreateWithNullPath() {
        assertThrows(NullPointerException.class, () -> new RecentPresentation(null, now()));
    }

    @Test
    @DisplayName("is greater than a null one")
    void compareToNull() {
        final RecentPresentation presentation = new RecentPresentation("presentation1.sfx", null);
        assertTrue(presentation.compareTo(null) > 0);
    }

    @Test
    @DisplayName("is greater than another")
    void compareToGreaterThanAnother() {
        final RecentPresentation presentation1 = new RecentPresentation("presentation1.sfx", null);
        final RecentPresentation presentation2 = new RecentPresentation("presentation2.sfx", null);
        assertTrue(presentation2.compareTo(presentation1) > 0);
    }

    @Test
    @DisplayName("is greater than another")
    void compareToLowerThanAnother() {
        final RecentPresentation presentation1 = new RecentPresentation("presentation1.sfx", null);
        final RecentPresentation presentation2 = new RecentPresentation("presentation2.sfx", null);
        assertTrue(presentation1.compareTo(presentation2) < 0);
    }

    @Test
    @DisplayName("is equal to itself using compareTo")
    void compareToIsEqual() {
        final RecentPresentation presentation1 = new RecentPresentation("presentation1.sfx", null);
        assertEquals(0, presentation1.compareTo(presentation1));
    }
}

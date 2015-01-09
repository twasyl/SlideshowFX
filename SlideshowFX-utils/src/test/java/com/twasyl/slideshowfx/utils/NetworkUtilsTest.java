package com.twasyl.slideshowfx.utils;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

import java.util.List;

/**
 * This class provides tests for the class {@link com.twasyl.slideshowfx.utils.NetworkUtils}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class NetworkUtilsTest {

    /**
     * Tests the method of {@link NetworkUtils#getIPs()} that returns all IPv4 addresses for the host.
     */
    @Test public void testGetIps() {
        final List<String> ips = NetworkUtils.getIPs();

        assertNotNull(ips);
        assertTrue(ips.size() > 0);
    }

}

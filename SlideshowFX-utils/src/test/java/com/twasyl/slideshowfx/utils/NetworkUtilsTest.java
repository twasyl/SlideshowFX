/*
 * Copyright 2015 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.utils;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    @Test
    public void testGetIps() {
        final List<String> ips = NetworkUtils.getIPs();

        assertNotNull(ips);
        assertTrue(ips.size() > 0);
        assertTrue(ips.contains("127.0.0.1"));
    }
}

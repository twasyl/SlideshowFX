package com.twasyl.slideshowfx.server;

import com.twasyl.slideshowfx.utils.NetworkUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Thierry Wasylczenko
 */
public class SlideshowFXServerTest {

    private static final int SERVER_PORT = 50080;
    private static final String SERVER_HOST = NetworkUtils.getIP();

    @BeforeEach public void before() {
        SlideshowFXServer.create(SERVER_HOST, SERVER_PORT, null);
    }

    @AfterEach public void after() {
        SlideshowFXServer.getSingleton().stop();
    }

    @Test public void portIsSet() {
        assertEquals(SERVER_PORT, SlideshowFXServer.getSingleton().getPort());
    }

    @Test public void hostIsSet() {
        assertEquals(SERVER_HOST, SlideshowFXServer.getSingleton().getHost());
    }
}

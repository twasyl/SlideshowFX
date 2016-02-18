package com.twasyl.slideshowfx.server;

import com.twasyl.slideshowfx.utils.NetworkUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Thierry Wasylczenko
 */
public class SlideshowFXServerTest {

    private static final int SERVER_PORT = 50080;
    private static final String SERVER_HOST = NetworkUtils.getIP();

    @BeforeClass
    public static void setUp() {
        SlideshowFXServer.create(SERVER_HOST, SERVER_PORT, null);
    }

    @Before public void before() {
        SlideshowFXServer.getSingleton().start();
    }

    @After public void after() {
        SlideshowFXServer.getSingleton().stop();
    }

    @Test public void portIsSet() {
        assertEquals(SERVER_PORT, SlideshowFXServer.getSingleton().getPort());
    }

    @Test public void hostIsSet() {
        assertEquals(SERVER_HOST, SlideshowFXServer.getSingleton().getHost());
    }

    @Test public void connectWebSocketWrongPath() {

    }

    @Test public void connectWebSocketCorrectPath() {

    }
}

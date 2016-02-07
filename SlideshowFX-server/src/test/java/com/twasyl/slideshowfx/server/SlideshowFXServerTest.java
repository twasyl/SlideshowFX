package com.twasyl.slideshowfx.server;

import com.twasyl.slideshowfx.utils.NetworkUtils;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @author Thierry Wasylczenko
 */
@RunWith(io.vertx.ext.unit.junit.VertxUnitRunner.class)
public class SlideshowFXServerTest {

    @BeforeClass
    public static void setUp() {
        SlideshowFXServer.create(NetworkUtils.getIP(), 10080, null);
        SlideshowFXServer.getSingleton().start();
    }
}

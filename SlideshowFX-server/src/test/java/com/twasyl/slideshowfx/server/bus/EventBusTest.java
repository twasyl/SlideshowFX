package com.twasyl.slideshowfx.server.bus;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the {@link EventBus} class.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0.0
 */
public class EventBusTest {

    private static abstract class DummyActor implements Actor {
        boolean gotMessage = false;
    }

    private static class DontSupportMessage extends DummyActor {
        @Override
        public boolean supportsMessage(Object message) {
            return false;
        }

        @Override
        public void onMessage(Object message) {

        }
    }

    private static class SupportMessage extends DummyActor {
        @Override
        public boolean supportsMessage(Object message) {
            return true;
        }

        @Override
        public void onMessage(Object message) {
            gotMessage = true;
        }
    }

    public static final String ENDPOINT_1 = "endpoint.1";
    public static final String ENDPOINT_2 = "endpoint.2";
    public static final String ENDPOINT_UNKNOWN = "endpoint.unknown";

    public static DontSupportMessage DONT_SUPPORT_MESSAGE_ACTOR;
    public static SupportMessage SUPPORT_MESSAGE_ACTOR_1;
    public static SupportMessage SUPPORT_MESSAGE_ACTOR_2;

    @BeforeClass
    public static void setUp() {
        DONT_SUPPORT_MESSAGE_ACTOR = new DontSupportMessage();
        SUPPORT_MESSAGE_ACTOR_1 = new SupportMessage();
        SUPPORT_MESSAGE_ACTOR_2 = new SupportMessage();
    }

    @Before public void before() {
        EventBus.getInstance().subscribe(ENDPOINT_1, DONT_SUPPORT_MESSAGE_ACTOR);
        EventBus.getInstance().subscribe(ENDPOINT_2, DONT_SUPPORT_MESSAGE_ACTOR);

        EventBus.getInstance().subscribe(ENDPOINT_1, SUPPORT_MESSAGE_ACTOR_1);
        EventBus.getInstance().subscribe(ENDPOINT_2, SUPPORT_MESSAGE_ACTOR_1);

        EventBus.getInstance().subscribe(ENDPOINT_2, SUPPORT_MESSAGE_ACTOR_2);
    }

    @After public void after() {
        DONT_SUPPORT_MESSAGE_ACTOR.gotMessage = false;
        SUPPORT_MESSAGE_ACTOR_1.gotMessage = false;
        SUPPORT_MESSAGE_ACTOR_2.gotMessage = false;
    }

    @Test(expected = NullPointerException.class)
    public void tryToSubscribeNullActor() throws Exception {
        EventBus.getInstance().subscribe(ENDPOINT_1, null);
    }

    @Test(expected = NullPointerException.class)
    public void tryToSubscribeNullEndpoint() throws Exception {
        EventBus.getInstance().subscribe(null, SUPPORT_MESSAGE_ACTOR_1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tryToSubscribeEmptyEndpoint() throws Exception {
        EventBus.getInstance().subscribe("", SUPPORT_MESSAGE_ACTOR_1);
    }

    @Test public void onMessageNotCalledWhenUnsupported() throws InterruptedException {
        EventBus.getInstance().broadcast(ENDPOINT_1, "Test");
        Thread.sleep(10);

        assertFalse(DONT_SUPPORT_MESSAGE_ACTOR.gotMessage);
    }

    @Test public void onMessageCalledWhenSupported() throws InterruptedException {
        EventBus.getInstance().broadcast(ENDPOINT_1, "Test");
        Thread.sleep(10);

        assertTrue(SUPPORT_MESSAGE_ACTOR_1.gotMessage);
    }

    @Test public void onMessageCalledWhenSupportedForMultipleActors() throws InterruptedException {
        EventBus.getInstance().broadcast(ENDPOINT_2, "Test");
        Thread.sleep(10);

        assertTrue(SUPPORT_MESSAGE_ACTOR_1.gotMessage);
        assertTrue(SUPPORT_MESSAGE_ACTOR_2.gotMessage);
    }

    @Test public void unSubscribe() throws InterruptedException {
        EventBus.getInstance().unsubscribe(ENDPOINT_1, SUPPORT_MESSAGE_ACTOR_1);
        EventBus.getInstance().broadcast(ENDPOINT_1, "Test");
        Thread.sleep(10);

        assertFalse(SUPPORT_MESSAGE_ACTOR_1.gotMessage);
    }

    @Test public void unSubscribeWithUnknownEndpoint() {
        EventBus.getInstance().unsubscribe(ENDPOINT_UNKNOWN, SUPPORT_MESSAGE_ACTOR_1);
    }

    @Test public void onMessageOnlyCalledForSpecificEndpoint() throws InterruptedException {
        EventBus.getInstance().broadcast(ENDPOINT_1, "Test");
        Thread.sleep(10);

        assertTrue(SUPPORT_MESSAGE_ACTOR_1.gotMessage);
        assertFalse(SUPPORT_MESSAGE_ACTOR_2.gotMessage);
    }

    @Test public void broadcastToUnknownEndpoint() {
        EventBus.getInstance().broadcast(ENDPOINT_UNKNOWN, "Test");
    }
}

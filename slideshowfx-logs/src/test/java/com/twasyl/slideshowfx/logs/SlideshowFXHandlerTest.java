package com.twasyl.slideshowfx.logs;

import org.junit.jupiter.api.*;

import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("The SlideshowFXHandler")
public class SlideshowFXHandlerTest {
    private static final Logger LOGGER = Logger.getLogger(SlideshowFXHandlerTest.class.getName());
    private SlideshowFXHandler handler;
    private static Locale DEFAULT_LOCALE = Locale.getDefault();

    @BeforeAll
    static void setUp() {
        Locale.setDefault(ENGLISH);
    }

    @BeforeEach
    void initializeHandlerAndLogger() {
        handler = new SlideshowFXHandler();
        LOGGER.addHandler(handler);
        LOGGER.setLevel(INFO);
    }

    @AfterEach
    void cleanLoggerFromHandler() {
        LOGGER.removeHandler(handler);
        handler = null;
    }

    @AfterAll
    static void tearDown() {
        Locale.setDefault(DEFAULT_LOCALE);
    }

    @Test
    @DisplayName("is defined as handler")
    void handlerProperlyRegistered() {
        assertTrue(asList(LOGGER.getHandlers()).contains(handler));
    }

    @Test
    @DisplayName("returns all logs")
    void returnsAllLogs() {
        LOGGER.log(INFO, "Information message");
        LOGGER.warning("Warning message");

        assertTrue(handler.getAllLogs().contains("INFO: Information message"));
        assertTrue(handler.getAllLogs().contains("WARNING: Warning message"));
    }

    @Test
    @DisplayName("should contain the proper latest log")
    void correctLatestLog() {
        LOGGER.info("First message");
        LOGGER.info("Second message");

        assertTrue(handler.getLatestLog().contains("INFO: Second message"));
    }

    @Test
    @DisplayName("doesn't contain the latest log if the log level doesn't allow it")
    void logLevelTooHighForLatestLog() {
        LOGGER.setLevel(WARNING);
        LOGGER.info("Should not be logged");

        assertNull(handler.getLatestLog());
    }

    @Test
    @DisplayName("doesn't contain the latest log if the publish method of the handler is called with a wrong log level")
    void logLevelTooHighForLatestLogWithPublishMethod() {
        handler.setLevel(WARNING);
        handler.publish(new LogRecord(INFO, "Should not be logged bis"));

        assertNull(handler.getLatestLog());
    }

    @Test
    @DisplayName("fires an event when the latest log changes")
    void eventFired() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Boolean> eventFired = new CompletableFuture<>();
        handler.addPropertyChangeListener(event -> eventFired.complete(true));
        LOGGER.info("A message for event");

        assertTrue(eventFired.get(5, SECONDS));
    }

    @Test
    @DisplayName("allows to remove a PropertyChangeListener for the latest log")
    void removePropertyChangeListener() {
        final CompletableFuture<Boolean> eventFired = new CompletableFuture<>();
        final PropertyChangeListener listener = event -> eventFired.completeExceptionally(new IllegalStateException("Event shouldn't be fired"));

        handler.addPropertyChangeListener(listener);
        handler.removePropertyChangeListener(listener);

        LOGGER.info("Don't fire event");

        assertThrows(TimeoutException.class, () -> eventFired.get(5, SECONDS));
    }
}

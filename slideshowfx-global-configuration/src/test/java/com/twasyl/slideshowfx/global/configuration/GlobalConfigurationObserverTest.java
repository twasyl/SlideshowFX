package com.twasyl.slideshowfx.global.configuration;

import com.twasyl.slideshowfx.global.configuration.events.ThemeChangeEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("A GlobalConfigurationObserver")
public class GlobalConfigurationObserverTest {

    @Test
    @DisplayName("is notified of a theme change")
    void observersAreNotifiedWhenThemeChanges() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Boolean> eventThrown = new CompletableFuture<>();

        final GlobalConfigurationObserver observer = (oldTheme, newTheme) -> eventThrown.complete(true);

        final GlobalConfigurationObservable observable = new GlobalConfigurationObservable();
        observable.addObserver(observer);
        observable.notifyThemeChanged("old", "new");

        assertTrue(eventThrown.get(5, SECONDS));
    }

    @Test
    @DisplayName("has correct old and new theme values")
    void correctOldAndNewTheme() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<String> oldTheme = new CompletableFuture<>();
        final CompletableFuture<String> newTheme = new CompletableFuture<>();
        final CompletableFuture<Void> themeValues = CompletableFuture.allOf(oldTheme, newTheme);

        final GlobalConfigurationObserver observer = (oldThemeValue, newThemeValue) -> {
            oldTheme.complete(oldThemeValue);
            newTheme.complete(newThemeValue);
        };

        final GlobalConfigurationObservable observable = new GlobalConfigurationObservable();
        observable.addObserver(observer);
        observable.notifyThemeChanged("old", "new");

        themeValues.get(5, SECONDS);
        assertEquals("old", oldTheme.get());
        assertEquals("new", newTheme.get());
    }

    @Test
    @DisplayName("ignores non ThemeChangeEvent")
    void ignoresNonThemeChangeEvent() {
        final CompletableFuture<String> oldTheme = new CompletableFuture<>();
        final CompletableFuture<String> newTheme = new CompletableFuture<>();
        final CompletableFuture<Void> themeValues = CompletableFuture.allOf(oldTheme, newTheme);

        final GlobalConfigurationObserver observer = (oldThemeValue, newThemeValue) -> {
            oldTheme.complete(oldThemeValue);
            newTheme.complete(newThemeValue);
        };

        final GlobalConfigurationObservable observable = spy(GlobalConfigurationObservable.class);
        doAnswer((Answer<Void>) invocation -> {
            doCallRealMethod().when(observable).notifyObservers(anyString());
            observable.notifyObservers("Hello");
            return null;
        }).when(observable).notifyObservers(any(ThemeChangeEvent.class));

        observable.addObserver(observer);
        observable.notifyThemeChanged("Hello", "World");

        assertThrows(Exception.class, () -> themeValues.get(5, SECONDS));
    }
}

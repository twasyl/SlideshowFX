package com.twasyl.slideshowfx.global.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("A GlobalConfigurationObservable")
public class GlobalConfigurationObservableTest {

    @Test
    @DisplayName("doesn't add a null observer")
    void dontAddNullObserver() {
        final GlobalConfigurationObservable observable = new GlobalConfigurationObservable();
        observable.addObserver(null);
        assertEquals(0, observable.countObservers());
    }

    @Test
    @DisplayName("doesn't add non GlobalConfigurationObserver instances")
    void dontAcceptNonGlobalConfigurationObservers() {
        final GlobalConfigurationObservable observable = new GlobalConfigurationObservable();
        observable.addObserver((o, arg) -> {
            // We don't care
        });
        assertEquals(0, observable.countObservers());
    }

    @Test
    @DisplayName("accepts GlobalConfigurationObserver instances")
    void addGlobalConfigurationObservers() {
        final GlobalConfigurationObservable observable = new GlobalConfigurationObservable();
        observable.addObserver((GlobalConfigurationObserver) (oldTheme, newTheme) -> {
            // We don't care
        });
        assertEquals(1, observable.countObservers());
    }
}

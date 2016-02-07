package com.twasyl.slideshowfx.utils.beans;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * This class allows to modelize a pair composed by a key and a value. The advantage regarding the {@link javafx.util.Pair} classes is
 * that the key and the value are {@link javafx.beans.property.Property properties}.
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class Pair<K, V> {
    private final ObjectProperty<K> key = new SimpleObjectProperty<>();
    private final ObjectProperty<V> value = new SimpleObjectProperty<>();

    public Pair() {
    }

    public Pair(K key, V value) {
        this.setKey(key);
        this.setValue(value);
    }

    /**
     * Get the property corresponding to the key of this pair.
     * @return The property of the key.
     */
    public ObjectProperty<K> keyProperty() { return key; }

    /**
     * Get the value of the key for this pair.
     * @return The value of the key.
     */
    public K getKey() { return key.get(); }

    /**
     * Set the value of the key for this pair.
     * @param key The new value for the key of this pair.
     */
    public void setKey(K key) { this.key.set(key); }

    /**
     * Get the property corresponding to the value of this pair.
     * @return The property of the value.
     */
    public ObjectProperty<V> valueProperty() { return value; }

    /**
     * Get the value of the value for this pair.
     * @return The value of the value.
     */
    public V getValue() { return value.get(); }

    /**
     * Set the value of the value of for this pair.
     * @param value The new value for the value of this pair.
     */
    public void setValue(V value) { this.value.set(value); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (!key.get().equals(pair.key.get())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.get().hashCode();
        return result;
    }
}

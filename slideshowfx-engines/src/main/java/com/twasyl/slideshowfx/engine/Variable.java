package com.twasyl.slideshowfx.engine;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Base64;
import java.util.Objects;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;

/**
 * Variable that can be defined within a {@link com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration}
 * or {@link com.twasyl.slideshowfx.engine.presentation.configuration.PresentationConfiguration}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class Variable {
    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        final String oldName = this.name;
        this.name = name;
        this.propertyChangeSupport.firePropertyChange("name", oldName, this.name);
    }

    public String getValue() {
        return value;
    }

    public String getValueAsBase64() {
        return Base64.getEncoder().encodeToString(this.getValue().getBytes(getDefaultCharset()));
    }

    public void setValue(String value) {
        final String oldValue = this.value;
        this.value = value;
        this.propertyChangeSupport.firePropertyChange("value", oldValue, this.value);
    }

    public void setValueAsBase64(final String valueAsBase64) {
        final byte[] value = Base64.getDecoder().decode(valueAsBase64);
        this.setValue(new String(value, getDefaultCharset()));
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Variable variable = (Variable) o;
        return name.equals(variable.name) &&
                Objects.equals(value, variable.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}

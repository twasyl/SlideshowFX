package com.twasyl.slideshowfx.logs;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.WARNING;

/**
 * An implementation of the {@link StreamHandler} class that allows to store the logs into a {@link ByteArrayOutputStream}
 * in order to display them in a screen. The logs are only living during the application life.
 * Each time a new log message is received by this implementation, a change event is raised on the property {@code latestLog}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class SlideshowFXHandler extends StreamHandler {
    private static final Logger LOGGER = Logger.getLogger(SlideshowFXHandler.class.getName());

    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    protected String latestLog;
    protected ByteArrayOutputStream byteOutput;

    public SlideshowFXHandler() {
        super();
        this.latestLog = null;
        this.byteOutput = new ByteArrayOutputStream();

        try {
            this.setEncoding(UTF_8.displayName());
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(WARNING, "Encoding can not be set on handler", e);
        }

        super.setOutputStream(this.byteOutput);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Get the latest log message received by this handler.
     *
     * @return The latest log or {@code null} if none.
     */
    public String getLatestLog() {
        return latestLog;
    }

    protected void setLatestLog(String latestLog) {
        final String previousMessage = this.latestLog;
        this.latestLog = latestLog;
        propertyChangeSupport.firePropertyChange("latestLog", previousMessage, latestLog);
    }

    /**
     * Get all logs that this handler has received.
     *
     * @return All logs formatted as string.
     */
    public synchronized String getAllLogs() {
        String logs = "";

        try {
            super.flush();
            logs = new String(this.byteOutput.toByteArray(), this.getEncoding());
        } catch (IOException e) {
            LOGGER.log(WARNING, "Error retrieving all logs", e);
        }

        return logs;
    }

    @Override
    public synchronized void publish(LogRecord record) {
        if (super.isLoggable(record)) {
            final String message = super.getFormatter().format(record);
            this.setLatestLog(message);
        }
        super.publish(record);
    }
}

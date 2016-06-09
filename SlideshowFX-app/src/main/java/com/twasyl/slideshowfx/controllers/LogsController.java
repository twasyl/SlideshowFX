package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.logs.SlideshowFXHandler;
import javafx.beans.property.adapter.ReadOnlyJavaBeanStringProperty;
import javafx.beans.property.adapter.ReadOnlyJavaBeanStringPropertyBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller class for the {@code Logs.fxml} file.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @since 1.0
 */
public class LogsController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(LogsController.class.getName());

    private SlideshowFXHandler handler;

    @FXML private TextArea logsArea;

    private void refreshLogs() {
        if(handler != null) {
            this.logsArea.setText(handler.getAllLogs());
        } else {
            logsArea.setText("");
        }
    }

    private void setLogHandlerListener() throws NoSuchMethodException {
        if(this.handler != null) {
            final ReadOnlyJavaBeanStringProperty latestLog = new ReadOnlyJavaBeanStringPropertyBuilder()
                    .bean(this.handler)
                    .getter("getLatestLog")
                    .name("latestLog")
                    .build();

            latestLog.addListener((value, oldLog, newLog) -> {
                if(newLog != null) {
                    this.logsArea.appendText(System.lineSeparator());
                    this.logsArea.appendText(newLog);
                }
            });
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.handler = SlideshowFXHandler.getSingleton();
        this.refreshLogs();

        try {
            this.setLogHandlerListener();
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE, "Can not initialize the logs controller properly");
        }
    }
}

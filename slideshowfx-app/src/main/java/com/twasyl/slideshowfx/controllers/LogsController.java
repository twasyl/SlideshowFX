package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.logs.SlideshowFXHandler;
import com.twasyl.slideshowfx.ui.controls.ZoomTextArea;
import javafx.beans.property.adapter.ReadOnlyJavaBeanStringProperty;
import javafx.beans.property.adapter.ReadOnlyJavaBeanStringPropertyBuilder;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller class for the {@code Logs.fxml} file.
 *
 * @author Thierry Wasylczenko
 * @version 1.1-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class LogsController implements ThemeAwareController {
    private static final Logger LOGGER = Logger.getLogger(LogsController.class.getName());

    private SlideshowFXHandler handler;
    private ReadOnlyJavaBeanStringProperty latestLog;
    private ChangeListener<String> latestLogChangeListener = (value, oldLog, newLog) -> {
        if (newLog != null) {
            this.logsArea.appendText(System.lineSeparator());
            this.logsArea.appendText(newLog);
        }
    };

    @FXML
    private BorderPane root;

    @FXML
    private ZoomTextArea logsArea;

    private void refreshLogs() {
        if (handler != null) {
            this.logsArea.setText(handler.getAllLogs());
        } else {
            logsArea.setText("");
        }
    }

    private void setLogHandlerListener() throws NoSuchMethodException {
        if (this.handler != null) {
            latestLog = ReadOnlyJavaBeanStringPropertyBuilder.create()
                    .bean(this.handler)
                    .getter("getLatestLog")
                    .name("latestLog")
                    .build();

            latestLog.addListener(latestLogChangeListener);
        }
    }

    private SlideshowFXHandler getHandler() {
        final Handler[] handlers = LOGGER.getParent().getHandlers();

        return Arrays.stream(handlers)
                .filter(h -> h.getClass().equals(SlideshowFXHandler.class))
                .map(h -> (SlideshowFXHandler) h)
                .findAny()
                .orElse(null);
    }

    @Override
    public Parent getRoot() {
        return this.root;
    }

    @Override
    public void postInitialize(URL location, ResourceBundle resources) {
        this.handler = getHandler();
        this.refreshLogs();

        try {
            this.setLogHandlerListener();
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE, "Can not initialize the logs controller properly");
        }

        this.logsArea.sceneProperty().addListener((sceneValue, oldScene, newScene) ->
                newScene.windowProperty().addListener((windowValue, oldWindow, newWindow) -> {
                    if (newWindow != null && latestLog != null && latestLogChangeListener != null) {
                        newWindow.setOnCloseRequest(event -> latestLog.removeListener(latestLogChangeListener));
                    }
                })
        );
    }
}

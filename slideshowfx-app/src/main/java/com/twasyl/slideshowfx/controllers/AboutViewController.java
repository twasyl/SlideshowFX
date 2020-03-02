package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.plugin.manager.PluginManager;
import com.twasyl.slideshowfx.plugin.manager.internal.RegisteredPlugin;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import com.twasyl.slideshowfx.utils.Jar;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller class of the {@code AboutView.fxml} view.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class AboutViewController implements ThemeAwareController {
    private Logger LOGGER = Logger.getLogger(AboutViewController.class.getName());

    @FXML
    private Parent root;
    @FXML
    private Label slideshowFXVersion;
    @FXML
    private Label javaVersion;
    @FXML
    private TableView<RegisteredPlugin> plugins;

    @FXML
    public void exitByClick(final MouseEvent event) {
        this.closeStage();
    }

    protected void closeStage() {
        final Event closeEvent = new WindowEvent(this.root.getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST);
        this.root.getScene().getWindow().fireEvent(closeEvent);
    }

    protected void populatePluginsTable() {
        PluginManager manager = PluginManager.getInstance();
        this.plugins.getItems().addAll(manager.getPlugins(IMarkup.class));
        this.plugins.getItems().addAll(manager.getPlugins(IContentExtension.class));
        this.plugins.getItems().addAll(manager.getPlugins(ISnippetExecutor.class));
        this.plugins.getItems().addAll(manager.getPlugins(IHostingConnector.class));
    }

    @Override
    public Parent getRoot() {
        return this.root;
    }

    @Override
    public void postInitialize(URL location, ResourceBundle resources) {
        this.slideshowFXVersion.setText(String.format("SlideshowFX version: %1$s", getApplicationVersion()));
        this.javaVersion.setText(String.format("Java version: %1$s", System.getProperty("java.version")));
        this.populatePluginsTable();
    }

    /**
     * Get the version of the application. The version is stored within the {@code MANIFEST.MF} file of the {@link JarFile}
     * of the application.
     *
     * @return The version of the application stored in the {@code MANIFEST.MF} file or {@code null} if it can not be found.
     */
    private String getApplicationVersion() {
        String appVersion = null;

        try (final Jar jar = Jar.fromClass(getClass())) {
            appVersion = jar.getImplementationVersion();
        } catch (IOException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Can not get application's version", e);
        }

        return appVersion;
    }
}

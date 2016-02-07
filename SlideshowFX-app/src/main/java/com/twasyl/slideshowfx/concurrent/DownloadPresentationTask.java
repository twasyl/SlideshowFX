package com.twasyl.slideshowfx.concurrent;

import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.exceptions.HostingConnectorException;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
import com.twasyl.slideshowfx.utils.concurrent.SlideshowFXTask;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This task downloads a SlideshowFX presentation from a given service represented by
 * {@link com.twasyl.slideshowfx.hosting.connector.IHostingConnector}. The presentation is downloaded in the given {@code #destination}.
 * The file to download is represented by the {@code #file} attribute.
 * Nothing will be done if the user is not authenticated or if the user doesn't want to overwrite an existing file.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class DownloadPresentationTask extends SlideshowFXTask<File> {
    private static final Logger LOGGER = Logger.getLogger(SavePresentationTask.class.getName());

    private IHostingConnector hostingConnector;
    private RemoteFile file;
    private File destination;

    public DownloadPresentationTask(IHostingConnector hostingConnector, File destination, RemoteFile file) {
        ((SimpleStringProperty) this.titleProperty()).set(String.format("Downloading presentation from %1$s: %2$s", hostingConnector.getName(), file.getName()));
        this.hostingConnector = hostingConnector;
        this.destination = destination;
        this.file = file;
    }

    @Override
    protected File call() throws Exception {

        if(!this.hostingConnector.isAuthenticated()) {
            throw new HostingConnectorException(HostingConnectorException.NOT_AUTHENTICATED);
        }

        final File result = this.hostingConnector.download(this.destination, this.file);

        return result;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        this.updateMessage("Presentation downloaded from " + this.hostingConnector.getName());
        this.updateProgress(0, 0);
    }

    @Override
    protected void running() {
        super.running();
        this.updateMessage("Downloading presentation from " + this.hostingConnector.getName());
        this.updateProgress(-1, 0);
    }

    @Override
    protected void failed() {
        super.failed();
        this.updateMessage("Error while downloading the presentation from " + this.hostingConnector.getName());
        this.updateProgress(0, 0);
        LOGGER.log(Level.SEVERE, "Can not download the presentation", this.getException());
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        this.updateMessage("Cancelled presentation download");
        this.updateProgress(0, 0);
    }
}

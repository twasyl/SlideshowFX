package com.twasyl.slideshowfx.setup.step;

import com.twasyl.slideshowfx.setup.app.SetupProperties;
import com.twasyl.slideshowfx.setup.controllers.FinishViewController;
import com.twasyl.slideshowfx.setup.exceptions.SetupStepException;
import com.twasyl.slideshowfx.utils.OSUtils;
import javafx.fxml.FXMLLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * Step displayed when the setup of the application is finished.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class FinishStep extends AbstractSetupStep<FinishViewController> {
    private static final Logger LOGGER = Logger.getLogger(FinishStep.class.getName());

    /**
     * Create a new finish step.
     */
    public FinishStep() {
        this.title("Installation successful");

        final FXMLLoader loader = new FXMLLoader(FinishStep.class.getResource("/com/twasyl/slideshowfx/setup/fxml/FinishView.fxml"));

        try {
            this.view = loader.load();
            this.controller = loader.getController();

            this.controller
                    .setApplicationName(SetupProperties.getInstance().getApplicationName())
                    .setApplicationVersion(SetupProperties.getInstance().getApplicationVersion());

            this.validProperty().set(true);
        } catch (IOException e) {
            LOGGER.log(SEVERE, "Can not find FXML", e);
        }
    }

    @Override
    public void execute() throws SetupStepException {
        if (OSUtils.isWindows()) {
            if (this.controller.createDesktopShortcut()) {
                createWindowsDesktopShortcut();
            }

        }

        if ((OSUtils.isMac() || OSUtils.isWindows()) && this.controller.startApplicationAfterInstallation()) {
            startApplicationAfterInstallation();
        }
    }

    private void createWindowsDesktopShortcut() throws SetupStepException {
        final File desktop = new File(System.getProperty("user.home"), "Desktop");
        final File link = new File(desktop, SetupProperties.getInstance().getApplicationName());
        final InstallationLocationStep installationLocationStep = this.find(InstallationLocationStep.class);

        if (installationLocationStep != null) {
            final File versionFolderSetup = installationLocationStep.getVersionFolderSetup();
            final File applicationDir = new File(versionFolderSetup, SetupProperties.getInstance().getApplicationName());
            final File executable = new File(applicationDir, SetupProperties.getInstance().getApplicationName() + ".exe");
            try {
                Files.createSymbolicLink(link.toPath(), executable.toPath());
            } catch (IOException e) {
                throw new SetupStepException("Error while creating the desktop shortcut", e);
            }
        }
    }

    private void startApplicationAfterInstallation() {
        final Runnable work = () -> {
            final InstallationLocationStep installationLocationStep = this.find(InstallationLocationStep.class);

            if (installationLocationStep != null) {
                final File versionFolderSetup = installationLocationStep.getVersionFolderSetup();
                final File applicationDir = new File(versionFolderSetup, SetupProperties.getInstance().getApplicationName());
                ProcessBuilder process = getProcessStartingApplication(applicationDir);

                if (process != null) {
                    try {
                        process.start();
                    } catch (IOException e) {
                        LOGGER.log(SEVERE, "Can not start application after installation", e);
                    }
                } else {
                    LOGGER.warning("The process for starting the application is null");
                }
            }
        };

        final Thread daemon = new Thread(work);
        daemon.setDaemon(true);
        daemon.start();
    }

    private ProcessBuilder getProcessStartingApplication(File applicationDir) {
        ProcessBuilder process = null;

        if (OSUtils.isMac()) {
            final var openExec = new File("/usr/bin/open");
            if (openExec.exists() && openExec.canExecute()) {
                process = new ProcessBuilder(openExec.getAbsolutePath(), new File(applicationDir, SetupProperties.getInstance().getApplicationName() + ".app").getAbsolutePath());
            }
        } else if (OSUtils.isWindows()) {
            process = new ProcessBuilder(new File(applicationDir, SetupProperties.getInstance().getApplicationName() + ".exe").getAbsolutePath());
        }
        return process;
    }

    @Override
    public void rollback() throws SetupStepException {
        // This step doesn't perform any operation
    }
}

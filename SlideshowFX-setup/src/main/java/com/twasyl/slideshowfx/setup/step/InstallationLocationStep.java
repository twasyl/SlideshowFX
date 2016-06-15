package com.twasyl.slideshowfx.setup.step;

import com.twasyl.slideshowfx.setup.controllers.InstallationLocationViewController;
import com.twasyl.slideshowfx.setup.exceptions.SetupStepException;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import com.twasyl.slideshowfx.utils.io.CopyFileVisitor;
import com.twasyl.slideshowfx.utils.io.DeleteFileVisitor;
import javafx.fxml.FXMLLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A step allowing to choose the installation location of the application.
 * During the {@link #execute()} method, the application will be copied within the chosen destination.
 * During the {@link #rollback()} method, the application will be removed from the chosen location.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class InstallationLocationStep extends AbstractSetupStep {

    final String applicationName;
    final String applicationVersion;
    final File applicationArtifact;
    final File documentationsFolder;
    File installationLocation;

    /**
     * Create an instance of the step.
     * @param appName The name of the application.
     * @param appVersion The version of the application.
     * @param applicationArtifact The file or directory containing the application.
     */
    public InstallationLocationStep(final String appName, final String appVersion, final File applicationArtifact, final File documentationsFolder) {
        this.title("Installation location");
        this.applicationName = appName;
        this.applicationVersion = appVersion;
        this.applicationArtifact = applicationArtifact;
        this.documentationsFolder = documentationsFolder;

        final FXMLLoader loader = new FXMLLoader(ResourceHelper.getURL("/com/twasyl/slideshowfx/setup/fxml/InstallationLocationView.fxml"));

        try {
            this.view = loader.load();
            this.controller = loader.getController();

            this.validProperty().set(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute() throws SetupStepException {
        final String originalLocation = ((InstallationLocationViewController) this.controller).getLocation().replaceAll("\\\\", "/");
        installationLocation = new File(originalLocation);

        if(!installationLocation.exists()) {
            throw new SetupStepException("The location doesn't exist");
        }

        if(!installationLocation.canWrite() || !installationLocation.canExecute()) {
            throw new SetupStepException("Can not create files in the location");
        }

        final File applicationFolder = this.getApplicationFolderSetup();
        if(!applicationFolder.exists()) {
            applicationFolder.mkdir();
        }

        final File versionFolder = this.getVersionFolderSetup();
        if(!versionFolder.exists()) {
            versionFolder.mkdir();
        }

        final CopyFileVisitor artifactCopier = new CopyFileVisitor(versionFolder.toPath(), this.applicationArtifact.toPath());
        try {
            Files.walkFileTree(this.applicationArtifact.toPath(), artifactCopier);
        } catch (IOException ex) {
            throw new SetupStepException("Error copying application artifact", ex);
        }

        final CopyFileVisitor documentationCopier = new CopyFileVisitor(versionFolder.toPath(), this.documentationsFolder.toPath());
        try {
            Files.walkFileTree(this.documentationsFolder.toPath(), documentationCopier);
        } catch (IOException ex) {
            throw new SetupStepException("Error copying documentations", ex);
        }
    }

    @Override
    public void rollback() throws SetupStepException {
        final File versionFolder = this.getVersionFolderSetup();
        if(versionFolder.exists()) {
            try {
                Files.walkFileTree(versionFolder.toPath(), new DeleteFileVisitor());
            } catch (IOException e) {
                throw new SetupStepException("Can not delete the application version directory", e);
            }
        }

        final File applicationFolder = this.getApplicationFolderSetup();
        if(applicationFolder.list().length == 0) {
            applicationFolder.delete();
        }
    }

    protected File getApplicationFolderSetup() {
        return new File(installationLocation, this.applicationName);
    }

    protected File getVersionFolderSetup() {
        return new File(getApplicationFolderSetup(), this.applicationVersion);
    }
}

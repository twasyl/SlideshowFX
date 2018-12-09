package com.twasyl.slideshowfx.setup.step;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.setup.controllers.InstallationLocationViewController;
import com.twasyl.slideshowfx.setup.exceptions.SetupStepException;
import com.twasyl.slideshowfx.utils.OSUtils;
import com.twasyl.slideshowfx.utils.io.CopyFileVisitor;
import com.twasyl.slideshowfx.utils.io.DeleteFileVisitor;
import com.twasyl.slideshowfx.utils.io.IOUtils;
import javafx.fxml.FXMLLoader;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A step allowing to choose the installation location of the application.
 * During the {@link #execute()} method, the application will be copied within the chosen destination.
 * During the {@link #rollback()} method, the application will be removed from the chosen location.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class InstallationLocationStep extends AbstractSetupStep {

    private boolean applicationDirectoryCreatedDuringSetup = false;
    private boolean configurationFileCreatedDuringSetup = false;
    final String applicationName;
    final String applicationVersion;
    final File applicationArtifact;
    final File documentationsFolder;
    File installationLocation;
    final String twitterConsumerKey;
    final String twitterConsumerSecret;

    /**
     * Create an instance of the step.
     *
     * @param appName               The name of the application.
     * @param appVersion            The version of the application.
     * @param applicationArtifact   The file or directory containing the application.
     * @param twitterConsumerKey    The Twitter consumer key for the application
     * @param twitterConsumerSecret The Twitter consumer secret for the application
     */
    public InstallationLocationStep(final String appName, final String appVersion, final File applicationArtifact, final File documentationsFolder, String twitterConsumerKey, String twitterConsumerSecret) {
        this.title("Installation location");
        this.applicationName = appName;
        this.applicationVersion = appVersion;
        this.applicationArtifact = applicationArtifact;
        this.documentationsFolder = documentationsFolder;
        this.twitterConsumerKey = twitterConsumerKey;
        this.twitterConsumerSecret = twitterConsumerSecret;

        final FXMLLoader loader = new FXMLLoader(InstallationLocationStep.class.getResource("/com/twasyl/slideshowfx/setup/fxml/InstallationLocationView.fxml"));

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

        if (!installationLocation.exists()) {
            throw new SetupStepException("The location doesn't exist");
        }

        if (!installationLocation.canWrite() || !installationLocation.canExecute()) {
            throw new SetupStepException("Can not create files in the location");
        }

        final File applicationFolder = createApplicationFolder();
        final File versionFolder = createVersionFolder();

        copyApplication(versionFolder);
        copyDocumentation(versionFolder);
        createLoggingConfigurationFile(applicationFolder);
        patchApplicationCfgFile(new File(versionFolder, this.applicationArtifact.getName()));
        createApplicationConfigurationFile();
    }

    protected File createApplicationFolder() {
        final File applicationFolder = this.getApplicationFolderSetup();

        if (!applicationFolder.exists()) {
            applicationFolder.mkdir();
        }

        return applicationFolder;
    }

    protected File createVersionFolder() {
        final File versionFolder = this.getVersionFolderSetup();
        if (!versionFolder.exists()) {
            versionFolder.mkdir();
        }
        return versionFolder;
    }

    protected void copyApplication(File versionFolder) throws SetupStepException {
        final CopyFileVisitor artifactCopier = new CopyFileVisitor(versionFolder.toPath(), this.applicationArtifact.toPath());
        try {
            Files.walkFileTree(this.applicationArtifact.toPath(), artifactCopier);
        } catch (IOException ex) {
            throw new SetupStepException("Error copying application artifact", ex);
        }
    }

    protected void copyDocumentation(File versionFolder) throws SetupStepException {
        final CopyFileVisitor documentationCopier = new CopyFileVisitor(versionFolder.toPath(), this.documentationsFolder.toPath());
        try {
            Files.walkFileTree(this.documentationsFolder.toPath(), documentationCopier);
        } catch (IOException ex) {
            throw new SetupStepException("Error copying documentations", ex);
        }
    }

    protected void createLoggingConfigurationFile(final File applicationFolder) {
        final File loggingConfig = new File(applicationFolder, "logging.config");

        GlobalConfiguration.createLoggingConfigurationFile(loggingConfig);
        GlobalConfiguration.fillLoggingConfigurationFileWithDefaultValue();
    }

    protected void patchApplicationCfgFile(final File copiedApplicationArtifact) throws SetupStepException {
        final String cfgFileLocation;

        if (OSUtils.isMac()) {
            cfgFileLocation = "Contents/Java";
        } else {
            cfgFileLocation = "app";
        }

        final File cfgFile = new File(copiedApplicationArtifact, cfgFileLocation + "/SlideshowFX.cfg");

        final List<String> lines = new ArrayList<>();

//        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cfgFile)))) {
//            lines.addAll(reader.lines().collect(Collectors.toList()));
//
//        } catch (IOException e) {
//            throw new SetupStepException("Can not read configuration file to patch", e);
//        }
//
//        try (final PrintWriter writer = new PrintWriter(cfgFile)) {
//            for (String line : lines) {
//                if (line.contains("@@LOGGING_CONFIGURATION_FILE@@")) {
//                    line = line.replace("@@LOGGING_CONFIGURATION_FILE@@", GlobalConfiguration.getLoggingConfigFile().getAbsolutePath());
//                }
//
//                writer.println(line);
//            }
//
//            writer.flush();
//        } catch (IOException e) {
//            throw new SetupStepException("Can not path the configuration file", e);
//        }
    }

    protected void createApplicationConfigurationFile() {
        this.applicationDirectoryCreatedDuringSetup = GlobalConfiguration.createApplicationDirectory();
        this.configurationFileCreatedDuringSetup = GlobalConfiguration.createConfigurationFile();
        GlobalConfiguration.setTwitterConsumerKey(this.twitterConsumerKey);
        GlobalConfiguration.setTwitterConsumerSecret(this.twitterConsumerSecret);
    }

    @Override
    public void rollback() throws SetupStepException {
        final File versionFolder = this.getVersionFolderSetup();
        if (versionFolder.exists()) {
            try {
                Files.walkFileTree(versionFolder.toPath(), new DeleteFileVisitor());
            } catch (IOException e) {
                throw new SetupStepException("Can not delete the application version directory", e);
            }
        }

        final File applicationFolder = this.getApplicationFolderSetup();
        if (applicationFolder.list().length == 0) {
            applicationFolder.delete();
        }

        if (this.configurationFileCreatedDuringSetup) {
            GlobalConfiguration.getConfigurationFile().delete();
        }

        if (this.applicationDirectoryCreatedDuringSetup) {
            try {
                IOUtils.deleteDirectory(GlobalConfiguration.getApplicationDirectory());
            } catch (IOException e) {
                throw new SetupStepException("Can not delete application configuration directory", e);
            }
        }
    }

    protected File getApplicationFolderSetup() {
        return new File(installationLocation, this.applicationName);
    }

    protected File getVersionFolderSetup() {
        return new File(getApplicationFolderSetup(), this.applicationVersion);
    }
}

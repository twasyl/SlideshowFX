package com.twasyl.slideshowfx.setup.app;

import java.io.File;
import java.util.Properties;

/**
 * Class holding the properties of the setup like the application name and version.
 * This class is working as a singleton.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class SetupProperties extends Properties {
    public static final String SETUP_DEFAULT_INSTALLATION_LOCATION_PROPERTY = "setup.default.installation.location";
    public static final String SETUP_PLUGINS_DIRECTORY_PROPERTY = "setup.plugins.directory";
    public static final String SETUP_APPLICATION_ARTIFACT_PROPERTY = "setup.application.artifact";
    public static final String SETUP_DOCUMENTATIONS_DIRECTORY_PROPERTY = "setup.documentations.directory";
    public static final String SETUP_APPLICATION_NAME_PROPERTY = "setup.application.name";
    public static final String SETUP_APPLICATION_VERSION_PROPERTY = "setup.application.version";
    public static final String SETUP_SERVICE_TWITTER_CONSUMER_KEY_PROPERTY = "setup.service.twitter.consumerKey";
    public static final String SETUP_SERVICE_TWITTER_CONSUMER_SECRET_PROPERTY = "setup.service.twitter.consumerSecret";

    private static SetupProperties singleton;

    private SetupProperties() {
        final var props = System.getProperties();
        this.withApplicationName(props.getProperty(SETUP_APPLICATION_NAME_PROPERTY))
                .withApplicationVersion(props.getProperty(SETUP_APPLICATION_VERSION_PROPERTY))
                .withApplicationArtifact(new File(props.getProperty(SETUP_APPLICATION_ARTIFACT_PROPERTY)))
                .withPluginsDirectory(new File(props.getProperty(SETUP_PLUGINS_DIRECTORY_PROPERTY)))
                .withDocumentationsDirectory(new File(props.getProperty(SETUP_DOCUMENTATIONS_DIRECTORY_PROPERTY)))
                .withTwitterConsumerKey(props.getProperty(SETUP_SERVICE_TWITTER_CONSUMER_KEY_PROPERTY))
                .withTwitterConsumerKey(props.getProperty(SETUP_SERVICE_TWITTER_CONSUMER_SECRET_PROPERTY));
    }

    public static synchronized SetupProperties getInstance() {
        if (singleton == null) {
            singleton = new SetupProperties();
        }

        return singleton;
    }

    public File getDefaultInstallationLocation() {
        if (containsKey(SETUP_DEFAULT_INSTALLATION_LOCATION_PROPERTY)) {
            return new File(getProperty(SETUP_DEFAULT_INSTALLATION_LOCATION_PROPERTY));
        }
        return null;
    }

    public SetupProperties withDefaultInstallationLocation(final File installationLocation) {
        setProperty(SETUP_DEFAULT_INSTALLATION_LOCATION_PROPERTY, installationLocation.getAbsolutePath());
        return this;
    }

    public File getPluginsDirectory() {
        if (containsKey(SETUP_PLUGINS_DIRECTORY_PROPERTY)) {
            return new File(getProperty(SETUP_PLUGINS_DIRECTORY_PROPERTY));
        }
        return null;
    }

    public SetupProperties withPluginsDirectory(final File pluginsDirectory) {
        setProperty(SETUP_PLUGINS_DIRECTORY_PROPERTY, pluginsDirectory.getAbsolutePath());
        return this;
    }

    public File getDocumentationsDirectory() {
        if (containsKey(SETUP_DOCUMENTATIONS_DIRECTORY_PROPERTY)) {
            return new File(getProperty(SETUP_DOCUMENTATIONS_DIRECTORY_PROPERTY));
        }
        return null;
    }

    public SetupProperties withDocumentationsDirectory(final File documentationsDirectory) {
        setProperty(SETUP_DOCUMENTATIONS_DIRECTORY_PROPERTY, documentationsDirectory.getAbsolutePath());
        return this;
    }

    public String getApplicationName() {
        return getProperty(SETUP_APPLICATION_NAME_PROPERTY);
    }

    public SetupProperties withApplicationName(final String name) {
        setProperty(SETUP_APPLICATION_NAME_PROPERTY, name);
        return this;
    }

    public String getApplicationVersion() {
        return getProperty(SETUP_APPLICATION_VERSION_PROPERTY);
    }

    public SetupProperties withApplicationVersion(final String version) {
        setProperty(SETUP_APPLICATION_VERSION_PROPERTY, version);
        return this;
    }

    public File getApplicationArtifact() {
        if (containsKey(SETUP_APPLICATION_ARTIFACT_PROPERTY)) {
            return new File(getProperty(SETUP_APPLICATION_ARTIFACT_PROPERTY));
        }
        return null;
    }

    public SetupProperties withApplicationArtifact(final File applicationArtifact) {
        setProperty(SETUP_APPLICATION_ARTIFACT_PROPERTY, applicationArtifact.getAbsolutePath());
        return this;
    }

    public String getTwitterConsumerKey() {
        return getProperty(SETUP_SERVICE_TWITTER_CONSUMER_KEY_PROPERTY);
    }

    public SetupProperties withTwitterConsumerKey(final String twitterConsumerKey) {
        setProperty(SETUP_SERVICE_TWITTER_CONSUMER_KEY_PROPERTY, twitterConsumerKey);
        return this;
    }

    public String getTwitterConsumerSecret() {
        return getProperty(SETUP_SERVICE_TWITTER_CONSUMER_SECRET_PROPERTY);
    }

    public SetupProperties withTwitterConsumerSecret(final String twitterConsumerSecret) {
        setProperty(SETUP_SERVICE_TWITTER_CONSUMER_SECRET_PROPERTY, twitterConsumerSecret);
        return this;
    }
}

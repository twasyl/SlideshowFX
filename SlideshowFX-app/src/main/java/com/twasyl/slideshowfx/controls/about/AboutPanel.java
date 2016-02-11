package com.twasyl.slideshowfx.controls.about;

import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A panel displaying information about the application.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0.0
 */
public class AboutPanel extends StackPane {
    private static final Logger LOGGER = Logger.getLogger(AboutPanel.class.getName());

    public AboutPanel() {
        final ImageView background = getAboutBackgroundView();

        this.getChildren().addAll(background, buildLabelsContainer());
        this.setAlignment(Pos.CENTER);
        this.setPrefSize(background.getFitWidth(), background.getFitHeight());
        this.setBackground(null);
    }

    /**
     * Get the initialized component that will display the about image.
     * @return The component hosting the about image.
     */
    private ImageView getAboutBackgroundView() {
        final Image logoImage = new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/about.png"));
        final ImageView logoView = new ImageView(logoImage);

        return logoView;
    }

    /**
     * Build and fill the container that will host the labels of the screen.
     * @return The container with labels inside.
     */
    private VBox buildLabelsContainer() {
        final VBox container = new VBox(10);
        container.setTranslateX(25);
        container.setTranslateY(150);

        container.getChildren().addAll(
                getSlideshowFXVersionLabel(),
                getJavaVersionLabel());

        return container;
    }

    /**
     * Constructs a {@link Label} containing the version of SlideshowFX.
     * @return The label with the version of SlideshowFX.
     */
    private Label getSlideshowFXVersionLabel() {
        final Label sfxVersion = new Label(String.format("SlideshowFX version: %1$s", getApplicationVersion()));
        sfxVersion.setStyle("-fx-text-fill: white;");

        return sfxVersion;
    }

    /**
     * Get the version of the application. The version is stored within the {@code MANIFEST.MF} file of the {@link JarFile}
     * of the application.
     * @return The version of the application stored in the {@code MANIFEST.MF} file or {@code null} if it can not be found.
     */
    private String getApplicationVersion() {
        String appVersion = null;

        try(final JarFile jarFile = new JarFile(getJARLocation())) {
            final Manifest manifest = jarFile.getManifest();
            final Attributes attrs = manifest.getMainAttributes();
            if(attrs != null) {
                appVersion = attrs.getValue("Implementation-Version");
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Can not get application's version", e);
        }

        return appVersion;
    }

    /**
     * Get the {@link File} that corresponds to the JAR file of the application.
     * @return The file corresponding to JRA file of the application.
     * @throws URISyntaxException If the location can not be determined.
     */
    private File getJARLocation() throws URISyntaxException {
        final File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        return file;
    }

    /**
     * Constructs a label containing the Java version used by the application.
     * @return The label with the Java version.
     */
    private Label getJavaVersionLabel() {
        final Label javaVersion = new Label(String.format("Java version: %1$s", System.getProperty("java.version")));
        javaVersion.setStyle("-fx-text-fill: white;");

        return javaVersion;
    }
}

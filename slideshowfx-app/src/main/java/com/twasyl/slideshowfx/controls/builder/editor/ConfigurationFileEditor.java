package com.twasyl.slideshowfx.controls.builder.editor;

import com.twasyl.slideshowfx.controls.builder.nodes.TemplateConfigurationFilePane;
import com.twasyl.slideshowfx.utils.DialogHelper;
import com.twasyl.slideshowfx.utils.io.DefaultCharsetWriter;
import javafx.scene.control.ScrollPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a control, based on a {@link javafx.scene.control.Tab} to
 * edit a configuration file.
 * The class defines action for accepting drag and drop event that allows to drag other
 * elements to insert text directly in the editor.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class ConfigurationFileEditor extends AbstractFileEditor<TemplateConfigurationFilePane> {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationFileEditor.class.getName());

    public ConfigurationFileEditor(Path workingPath, File file) {
        super();

        final ScrollPane scrollPane = new ScrollPane();

        this.setFileContent(new TemplateConfigurationFilePane());
        this.setEditorScrollPane(scrollPane);

        this.setWorkingPath(workingPath);
        this.getFileContent().setWorkingPath(workingPath);
        this.setFile(file);
    }

    @Override
    public void updateFileContent() {
        if (getFile() == null) throw new NullPointerException("The file to read can not be null.");
        if (!getFile().exists()) throw new IllegalArgumentException("The file does not exist.");
        if (!getFile().canRead()) throw new IllegalArgumentException("The file can not be read.");

        this.getFileContent().setWorkingPath(this.getWorkingPath());

        if (getFile().length() > 0) {
            this.getFileContent().fillWithFile(this.getFile());
        }
    }

    @Override
    public void saveContent() {
        if (getFile() == null) throw new NullPointerException("The fileProperty is null");

        if (this.getFileContent().isContentValid()) {
            try (final DefaultCharsetWriter writer = new DefaultCharsetWriter(getFile())) {
                writer.write(this.getFileContent().getAsString());
                writer.flush();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not save the content", e);
            }
        } else {
            DialogHelper.showError("Invalid configuration", "The given configuration is invalid");
        }
    }
}

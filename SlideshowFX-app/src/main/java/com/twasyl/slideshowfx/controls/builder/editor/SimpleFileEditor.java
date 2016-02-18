package com.twasyl.slideshowfx.controls.builder.editor;

import com.twasyl.slideshowfx.utils.io.DefaultCharsetWriter;
import javafx.scene.control.TextArea;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;

/**
 * @author Thierry Wasylczenko
 */
public class SimpleFileEditor extends AbstractFileEditor<TextArea> {

    private static final Logger LOGGER = Logger.getLogger(SimpleFileEditor.class.getName());

    public SimpleFileEditor() {
        super();

        final TextArea area = new TextArea();
        area.setWrapText(true);

        this.setFileContent(area);
    }

    public SimpleFileEditor(File file) {
        this();
        this.setFile(file);
    }

    @Override
    public void updateFileContent() {
        if(getFile() == null) throw new NullPointerException("The fileProperty is null");

        try(final InputStreamReader inputReader = new InputStreamReader(new FileInputStream(getFile()), getDefaultCharset());
            final BufferedReader reader = new BufferedReader(inputReader)) {
            final StringBuilder builder = new StringBuilder();

            reader.lines().forEach(line -> builder.append(line).append("\n"));

            getFileContent().setText(builder.toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not save the content", e);
        }
    }

    @Override
    public void saveContent() {
        if(getFile() == null) throw new NullPointerException("The fileProperty is null");

        try(final DefaultCharsetWriter writer = new DefaultCharsetWriter(getFile())) {
            writer.write(this.getFileContent().getText());
            writer.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not save the content", e);
        }
    }
}

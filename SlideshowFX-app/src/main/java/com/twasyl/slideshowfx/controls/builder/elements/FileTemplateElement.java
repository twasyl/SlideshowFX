package com.twasyl.slideshowfx.controls.builder.elements;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.nio.file.Path;

/**
 * The FileTemplateElement allows to choose a File as value in a text field.
 * It extends {@link com.twasyl.slideshowfx.controls.builder.elements.AbstractTemplateElement<File>}
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class FileTemplateElement extends AbstractTemplateElement<File> {

    protected final TextField field = new TextField();
    protected final Button browseButton = new Button("...");

    public FileTemplateElement(String name) {
        super();

        this.name.set(name);

        this.field.textProperty().bindBidirectional(this.value, new StringConverter<File>() {
            @Override
            public String toString(File object) {
                if(object == null) return "";
                else if(getWorkingPath() == null) return object.getAbsolutePath().replace("\\", "//");
                else {
                    Path reconstructedFile = getWorkingPath().resolve(object.toPath());
                    return  getWorkingPath().relativize(reconstructedFile).toString().replace("\\", "/");
                }
            }

            @Override
            public File fromString(String string) {
                if(string == null || string.isEmpty()) return null;
                else if(getWorkingPath() == null) return new File(string.replace("\\", "/"));
                else return getWorkingPath().resolve(new File(string).toPath()).toFile();
            }
        });

        this.browseButton.setOnAction(event -> {
            final FileChooser chooser = new FileChooser();
            if(this.getWorkingPath() != null) {
                chooser.setInitialDirectory(this.getWorkingPath().toFile());
            }

            this.validateChosenFile(chooser.showOpenDialog(null));
        });

        this.appendContent(this.field, this.browseButton);
    }

    @Override
    public String getAsString() {
        final StringBuilder builder = new StringBuilder();

        if(getName() != null) builder.append(String.format("\"%1$s\": ", getName()));

        String pathOfFile;

        if(getValue() == null) pathOfFile = "null";
        else if(getWorkingPath() == null) pathOfFile = getValue().getAbsolutePath();
        else {
            final Path reconstructedPath = getWorkingPath().resolve(getValue().toPath());
            pathOfFile = getWorkingPath().relativize(reconstructedPath).toString();
        }

        builder.append(String.format("\"%1$s\"", pathOfFile.replace("\\", "//")));

        return builder.toString();
    }

    /**
     * This method performs action for the file that has been chosen by the user.
     * It will relativize the chosen file according the {@see #getWorkingPath} if it is provided.
     * Be aware that these actions are only performed if the provided {@code chosenFile} is not null.
     *
     * @param chosenFile The chosen file by the user.
     */
    protected void validateChosenFile(File chosenFile) {
        if(chosenFile != null) {
            String path;

            if(getWorkingPath() == null) path = chosenFile.getAbsolutePath();
            else path = getWorkingPath().relativize(chosenFile.toPath()).toString();

            this.field.setText(path.replace("\\", "/"));
        }
    }
}

package com.twasyl.slideshowfx.content.extension.alert;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.content.extension.alert.controllers.AlertContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import javafx.beans.property.ReadOnlyBooleanProperty;

import static com.twasyl.slideshowfx.icons.Icon.EXCLAMATION_TRIANGLE;
import static java.lang.System.currentTimeMillis;

/**
 * The AlertContentExtension extends the AbstractContentExtension. It allows to build a content containing alert to insert
 * inside a SlideshowFX presentation.
 * This extension uses sweet-alert in order to manage programming language syntax coloration.
 * This extension supports HTML markup language.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class AlertContentExtension extends AbstractContentExtension<AlertContentExtensionController> {

    public AlertContentExtension() {
        super("ALERT",
                AlertContentExtension.class.getClassLoader().getResource("/com/twasyl/slideshowfx/content/extension/alert/fxml/AlertContentExtension.fxml"),
                AlertContentExtension.class.getResource("/com/twasyl/slideshowfx/content/extension/alert/resources/sweetalert2.zip"),
                EXCLAMATION_TRIANGLE,
                "Insert an alert", "Insert an alert");

        final String baseURL = "sweetalert2/7.29.1/";

        // Add URL
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("sweetalert2.all.min.js"));
    }

    @Override
    public String buildContentString(IMarkup markup) {
        return this.buildDefaultContentString();
    }

    @Override
    public String buildDefaultContentString() {
        final StringBuilder builder = new StringBuilder();

        final String id = generateID();

        builder.append("<button id=\"").append(id).append("\">").append(this.getController().getButtonText()).append("</button>\n");

        builder.append("<script type=\"text/javascript\">\n");
        builder.append("\tdocument.querySelector('#").append(id).append("').onclick = function() {\n");
        builder.append("\t\tSwal({\n");
        builder.append("\t\t\ttitleText: \"").append(this.getController().getTitle()).append("\"");

        if (this.getController().getText() != null && !this.getController().getText().isEmpty()) {
            builder.append(",\n\t\t\ttext: \"").append(this.getController().getText()).append("\"");
        }

        builder.append(",\n\t\t\ttype: \"").append(this.getController().getType()).append("\",\n");
        builder.append("\t\t\tshowConfirmButton: true,\n");
        builder.append("\t\t\tshowCancelButton: ").append(this.getController().isCancelButtonVisible()).append(",\n");
        builder.append("\t\t\tallowOutsideClick: ").append(this.getController().isClickOutsideAllowed()).append(",\n");
        builder.append("\t\t\tallowEscapeKey: ").append(this.getController().isClickOutsideAllowed()).append(",\n");
        builder.append("\t\t});\n");
        builder.append("\t};\n");
        builder.append("</script>");


        return builder.toString();
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        return this.getController().areInputsValid();
    }

    protected String generateID() {
        return "swal-btn-" + currentTimeMillis();
    }
}

package com.twasyl.slideshowfx.content.extension.alert;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.content.extension.alert.controllers.AlertContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.plugin.Plugin;
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
@Plugin
public class AlertContentExtension extends AbstractContentExtension<AlertContentExtensionController> {

    public AlertContentExtension() {
        super("ALERT",
                "/com/twasyl/slideshowfx/content/extension/alert/fxml/AlertContentExtension.fxml",
                "/com/twasyl/slideshowfx/content/extension/alert/resources/sweetalert2.zip",
                EXCLAMATION_TRIANGLE,
                "Insert an alert", "Insert an alert");

        final String baseURL = "sweetalert2/9.7.2/";

        // Add URL
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("sweetalert2.all.min.js"));
    }

    @Override
    public String buildContentString(IMarkup markup) {
        return this.buildDefaultContentString();
    }

    @Override
    public String buildDefaultContentString() {
        final String id = generateID();

        return String.format(
                """
                <button id="%1$s">%2$s</button>
                <script type="text/javascript">
                    document.querySelector('#%1$s').onclick = function() {
                        Swal.fire({
                            title: "%3$s",
                            text: "%4$s",
                            icon: '%5$s',
                            showConfirmButton: true,
                            showCancelButton: %6$s,
                            allowOutsideClick: %7$s,
                            allowEscapeKey: %7$s
                        });
                    };
                </script>
                """,
                id,
                this.getController().getButtonText(),
                this.getController().getTitle(),
                this.getController().getText(),
                this.getController().getType(),
                this.getController().isCancelButtonVisible(),
                this.getController().isClickOutsideAllowed());
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        return this.getController().areInputsValid();
    }

    protected String generateID() {
        return "swal-btn-" + currentTimeMillis();
    }
}

package com.twasyl.slideshowfx.content.extension.alert;

import com.twasyl.slideshowfx.content.extension.alert.controllers.AlertContentExtensionController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class AlertContentExtensionTest {
    public static String EXPECTED_SCRIPT_WITH_TEXT_FORMAT =
            """
            <button id="%1$s">My button</button>
            <script type="text/javascript">
                document.querySelector('#%1$s').onclick = function() {
                    Swal.fire({
                        title: "%2$s",
                        text: "%3$s",
                        icon: '%4$s',
                        showConfirmButton: %5$s,
                        showCancelButton: %6$s,
                        allowOutsideClick: %7$s,
                        allowEscapeKey: %8$s
                    });
                };
            </script>
            """;

    public static String EXPECTED_SCRIPT_WITHOUT_TEXT_FORMAT =
            """
            <button id="%1$s">My button</button>
            <script type="text/javascript">
                document.querySelector('#%1$s').onclick = function() {
                    Swal.fire({
                        title: "%2$s",
                        text: "",
                        icon: '%3$s',
                        showConfirmButton: %4$s,
                        showCancelButton: %5$s,
                        allowOutsideClick: %6$s,
                        allowEscapeKey: %7$s
                    });
                };
            </script>
            """;

    public static AlertContentExtension extension;
    public static String GENERATED_ID = "swal-btn-" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        extension = spy(new AlertContentExtension());
        doReturn(GENERATED_ID).when(extension).generateID();
    }

    @BeforeEach
    public void before() {
        final AlertContentExtensionController controller = spy(new AlertContentExtensionController());
        doReturn(controller).when(extension).getController();
    }

    @Test
    public void buildDefaultAlertWithText() {
        final String title = "Alert's title";
        final boolean cancelButtonVisible = true;
        final boolean confirmButtonVisible = true;
        final String buttonsText = "My button";
        final boolean closeOnEsc = true;
        final boolean clickOutsideAllowed = true;
        final String alertsText = "Text of the alert";
        final String alertsType = "info";

        final AlertContentExtensionController controller = extension.getController();
        doReturn(cancelButtonVisible).when(controller).isCancelButtonVisible();
        doReturn(clickOutsideAllowed).when(controller).isClickOutsideAllowed();
        doReturn(buttonsText).when(controller).getButtonText();
        doReturn(alertsText).when(controller).getText();
        doReturn(title).when(controller).getTitle();
        doReturn(alertsType).when(controller).getType();

        final String expected = String.format(EXPECTED_SCRIPT_WITH_TEXT_FORMAT, GENERATED_ID, title, alertsText, alertsType, confirmButtonVisible, cancelButtonVisible, clickOutsideAllowed, closeOnEsc);
        final String content = extension.buildDefaultContentString();

        assertEquals(expected, content);
    }

    @Test
    public void buildDefaultAlertWithoutText() {
        final String title = "Alert's title";
        final boolean cancelButtonVisible = true;
        final boolean confirmButtonVisible = true;
        final String buttonsText = "My button";
        final boolean closeOnEsc = true;
        final boolean clickOutsideAllowed = true;
        final String alertsType = "info";

        final AlertContentExtensionController controller = extension.getController();
        doReturn(cancelButtonVisible).when(controller).isCancelButtonVisible();
        doReturn(clickOutsideAllowed).when(controller).isClickOutsideAllowed();
        doReturn(buttonsText).when(controller).getButtonText();
        doReturn("").when(controller).getText();
        doReturn(title).when(controller).getTitle();
        doReturn(alertsType).when(controller).getType();

        final String expected = String.format(EXPECTED_SCRIPT_WITHOUT_TEXT_FORMAT, GENERATED_ID, title, alertsType, confirmButtonVisible, cancelButtonVisible, clickOutsideAllowed, closeOnEsc);
        final String content = extension.buildDefaultContentString();

        assertEquals(expected, content);
    }

    @Test
    public void buildDefaultInfoAlert() {
        final String title = "Alert's title";
        final boolean cancelButtonVisible = true;
        final boolean confirmButtonVisible = true;
        final String buttonsText = "My button";
        final boolean closeOnEsc = true;
        final boolean clickOutsideAllowed = true;
        final String alertsText = "Text of the alert";
        final String alertsType = "info";

        final AlertContentExtensionController controller = extension.getController();
        doReturn(cancelButtonVisible).when(controller).isCancelButtonVisible();
        doReturn(clickOutsideAllowed).when(controller).isClickOutsideAllowed();
        doReturn(buttonsText).when(controller).getButtonText();
        doReturn(alertsText).when(controller).getText();
        doReturn(title).when(controller).getTitle();
        doReturn(alertsType).when(controller).getType();

        final String expected = String.format(EXPECTED_SCRIPT_WITH_TEXT_FORMAT, GENERATED_ID, title, alertsText, alertsType, confirmButtonVisible, cancelButtonVisible, clickOutsideAllowed, closeOnEsc);
        final String content = extension.buildDefaultContentString();

        assertEquals(expected, content);
    }

    @Test
    public void buildDefaultSuccessAlert() {
        final String title = "Alert's title";
        final boolean cancelButtonVisible = true;
        final boolean confirmButtonVisible = true;
        final String buttonsText = "My button";
        final boolean closeOnEsc = true;
        final boolean clickOutsideAllowed = true;
        final String alertsText = "Text of the alert";
        final String alertsType = "success";

        final AlertContentExtensionController controller = extension.getController();
        doReturn(cancelButtonVisible).when(controller).isCancelButtonVisible();
        doReturn(clickOutsideAllowed).when(controller).isClickOutsideAllowed();
        doReturn(buttonsText).when(controller).getButtonText();
        doReturn(alertsText).when(controller).getText();
        doReturn(title).when(controller).getTitle();
        doReturn(alertsType).when(controller).getType();

        final String expected = String.format(EXPECTED_SCRIPT_WITH_TEXT_FORMAT, GENERATED_ID, title, alertsText, alertsType, confirmButtonVisible, cancelButtonVisible, clickOutsideAllowed, closeOnEsc);
        final String content = extension.buildDefaultContentString();

        assertEquals(expected, content);
    }

    @Test
    public void buildDefaultWarningAlert() {
        final String title = "Alert's title";
        final boolean cancelButtonVisible = true;
        final boolean confirmButtonVisible = true;
        final String buttonsText = "My button";
        final boolean closeOnEsc = true;
        final boolean clickOutsideAllowed = true;
        final String alertsText = "Text of the alert";
        final String alertsType = "warning";

        final AlertContentExtensionController controller = extension.getController();
        doReturn(cancelButtonVisible).when(controller).isCancelButtonVisible();
        doReturn(clickOutsideAllowed).when(controller).isClickOutsideAllowed();
        doReturn(buttonsText).when(controller).getButtonText();
        doReturn(alertsText).when(controller).getText();
        doReturn(title).when(controller).getTitle();
        doReturn(alertsType).when(controller).getType();

        final String expected = String.format(EXPECTED_SCRIPT_WITH_TEXT_FORMAT, GENERATED_ID, title, alertsText, alertsType, confirmButtonVisible, cancelButtonVisible, clickOutsideAllowed, closeOnEsc);
        final String content = extension.buildDefaultContentString();

        assertEquals(expected, content);
    }

    @Test
    public void buildDefaultErrorAlert() {
        final String title = "Alert's title";
        final boolean cancelButtonVisible = true;
        final boolean confirmButtonVisible = true;
        final String buttonsText = "My button";
        final boolean closeOnEsc = true;
        final boolean clickOutsideAllowed = true;
        final String alertsText = "Text of the alert";
        final String alertsType = "error";

        final AlertContentExtensionController controller = extension.getController();
        doReturn(cancelButtonVisible).when(controller).isCancelButtonVisible();
        doReturn(clickOutsideAllowed).when(controller).isClickOutsideAllowed();
        doReturn(buttonsText).when(controller).getButtonText();
        doReturn(alertsText).when(controller).getText();
        doReturn(title).when(controller).getTitle();
        doReturn(alertsType).when(controller).getType();

        final String expected = String.format(EXPECTED_SCRIPT_WITH_TEXT_FORMAT, GENERATED_ID, title, alertsText, alertsType, confirmButtonVisible, cancelButtonVisible, clickOutsideAllowed, closeOnEsc);
        final String content = extension.buildDefaultContentString();

        assertEquals(expected, content);
    }
}

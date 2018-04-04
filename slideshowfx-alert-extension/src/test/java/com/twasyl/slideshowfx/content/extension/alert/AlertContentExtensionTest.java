package com.twasyl.slideshowfx.content.extension.alert;

import com.twasyl.slideshowfx.content.extension.alert.controllers.AlertContentExtensionController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class AlertContentExtensionTest {
    public static String EXPECTED_SCRIPT_WITH_TEXT_FORMAT = "<button id=\"{0}\">My button</button>\n" +
            "<script type=\"text/javascript\">\n" +
            "\tdocument.querySelector(''#{0}'').onclick = function() '{'\n" +
            "\t\tswal('{'\n" +
            "\t\t\ttitle: \"{1}\",\n" +
            "\t\t\ttext: \"{2}\",\n" +
            "\t\t\ticon: \"{3}\",\n" +
            "\t\t\tbuttons: '{'\n" +
            "\t\t\t\tconfirm: {4},\n" +
            "\t\t\t\tcancel: {5}\n" +
            "\t\t\t'}',\n" +
            "\t\t\tcloseOnClickOutside: {6},\n" +
            "\t\t\tcloseOnEsc: {7},\n" +
            "\t\t'}');\n" +
            "\t'}';\n" +
            "</script>";

    public static String EXPECTED_SCRIPT_WITHOUT_TEXT_FORMAT = "<button id=\"{0}\">My button</button>\n" +
            "<script type=\"text/javascript\">\n" +
            "\tdocument.querySelector(''#{0}'').onclick = function() '{'\n" +
            "\t\tswal('{'\n" +
            "\t\t\ttitle: \"{1}\",\n" +
            "\t\t\ticon: \"{2}\",\n" +
            "\t\t\tbuttons: '{'\n" +
            "\t\t\t\tconfirm: {3},\n" +
            "\t\t\t\tcancel: {4}\n" +
            "\t\t\t'}',\n" +
            "\t\t\tcloseOnClickOutside: {5},\n" +
            "\t\t\tcloseOnEsc: {6},\n" +
            "\t\t'}');\n" +
            "\t'}';\n" +
            "</script>";

    public static AlertContentExtension extension;
    public static String GENERATED_ID = "swal-btn-" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        extension = spy(new AlertContentExtension());
        when(extension.generateID()).thenReturn(GENERATED_ID);
    }

    @BeforeEach
    public void before() {
        final AlertContentExtensionController controller = spy(new AlertContentExtensionController());
        extension.controller = controller;
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

        doReturn(cancelButtonVisible).when(extension.controller).isCancelButtonVisible();
        doReturn(clickOutsideAllowed).when(extension.controller).isClickOutsideAllowed();
        doReturn(buttonsText).when(extension.controller).getButtonText();
        doReturn(alertsText).when(extension.controller).getText();
        doReturn(title).when(extension.controller).getTitle();
        doReturn(alertsType).when(extension.controller).getType();

        final String expected = MessageFormat.format(EXPECTED_SCRIPT_WITH_TEXT_FORMAT, GENERATED_ID, title, alertsText, alertsType, confirmButtonVisible, cancelButtonVisible, clickOutsideAllowed, closeOnEsc);
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

        doReturn(cancelButtonVisible).when(extension.controller).isCancelButtonVisible();
        doReturn(clickOutsideAllowed).when(extension.controller).isClickOutsideAllowed();
        doReturn(buttonsText).when(extension.controller).getButtonText();
        doReturn("").when(extension.controller).getText();
        doReturn(title).when(extension.controller).getTitle();
        doReturn(alertsType).when(extension.controller).getType();

        final String expected = MessageFormat.format(EXPECTED_SCRIPT_WITHOUT_TEXT_FORMAT, GENERATED_ID, title, alertsType, confirmButtonVisible, cancelButtonVisible, clickOutsideAllowed, closeOnEsc);
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

        doReturn(cancelButtonVisible).when(extension.controller).isCancelButtonVisible();
        doReturn(clickOutsideAllowed).when(extension.controller).isClickOutsideAllowed();
        doReturn(buttonsText).when(extension.controller).getButtonText();
        doReturn(alertsText).when(extension.controller).getText();
        doReturn(title).when(extension.controller).getTitle();
        doReturn(alertsType).when(extension.controller).getType();

        final String expected = MessageFormat.format(EXPECTED_SCRIPT_WITH_TEXT_FORMAT, GENERATED_ID, title, alertsText, alertsType, confirmButtonVisible, cancelButtonVisible, clickOutsideAllowed, closeOnEsc);
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

        doReturn(cancelButtonVisible).when(extension.controller).isCancelButtonVisible();
        doReturn(clickOutsideAllowed).when(extension.controller).isClickOutsideAllowed();
        doReturn(buttonsText).when(extension.controller).getButtonText();
        doReturn(alertsText).when(extension.controller).getText();
        doReturn(title).when(extension.controller).getTitle();
        doReturn(alertsType).when(extension.controller).getType();

        final String expected = MessageFormat.format(EXPECTED_SCRIPT_WITH_TEXT_FORMAT, GENERATED_ID, title, alertsText, alertsType, confirmButtonVisible, cancelButtonVisible, clickOutsideAllowed, closeOnEsc);
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

        doReturn(cancelButtonVisible).when(extension.controller).isCancelButtonVisible();
        doReturn(clickOutsideAllowed).when(extension.controller).isClickOutsideAllowed();
        doReturn(buttonsText).when(extension.controller).getButtonText();
        doReturn(alertsText).when(extension.controller).getText();
        doReturn(title).when(extension.controller).getTitle();
        doReturn(alertsType).when(extension.controller).getType();

        final String expected = MessageFormat.format(EXPECTED_SCRIPT_WITH_TEXT_FORMAT, GENERATED_ID, title, alertsText, alertsType, confirmButtonVisible, cancelButtonVisible, clickOutsideAllowed, closeOnEsc);
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

        doReturn(cancelButtonVisible).when(extension.controller).isCancelButtonVisible();
        doReturn(clickOutsideAllowed).when(extension.controller).isClickOutsideAllowed();
        doReturn(buttonsText).when(extension.controller).getButtonText();
        doReturn(alertsText).when(extension.controller).getText();
        doReturn(title).when(extension.controller).getTitle();
        doReturn(alertsType).when(extension.controller).getType();

        final String expected = MessageFormat.format(EXPECTED_SCRIPT_WITH_TEXT_FORMAT, GENERATED_ID, title, alertsText, alertsType, confirmButtonVisible, cancelButtonVisible, clickOutsideAllowed, closeOnEsc);
        final String content = extension.buildDefaultContentString();

        assertEquals(expected, content);
    }
}

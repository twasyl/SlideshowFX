package com.twasyl.slideshowfx.utils;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.style.Styles;
import com.twasyl.slideshowfx.style.theme.Themes;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides helper methods to build {@link Dialog} for SlideshowFX.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
public class DialogHelper {
    private static final Logger LOGGER = Logger.getLogger(DialogHelper.class.getName());

    private DialogHelper() {
    }

    /**
     * Show a confirmation alert with the given {@code title} and {@code text}. The method returns the answer of the
     * user. The answer can either be {@link ButtonType#NO} or {@link ButtonType#YES}.
     *
     * @param title The title of the alert.
     * @param text  The text of the alert.
     * @return The answer of the user or {@code null} if no answer has been made.
     */
    public static ButtonType showConfirmationAlert(final String title, final String text) {
        return displayDialog(buildAlert(Alert.AlertType.CONFIRMATION, title, text, ButtonType.NO, ButtonType.YES));
    }

    /**
     * Show an alert with the given {@code title} and {@code text}. The method returns the answer of the user which can
     * only be {@link ButtonType#OK}.
     *
     * @param title The title of the alert.
     * @param text  The text of the alert.
     * @return The answer of the user which can only be {@link ButtonType#OK}.
     */
    public static ButtonType showAlert(final String title, final String text) {
        return displayDialog(buildAlert(Alert.AlertType.INFORMATION, title, text, ButtonType.OK));
    }

    /**
     * Show an error alert with the given {@code title} and {@code text}. The method returns the answer of the user
     * which can only be {@link ButtonType#OK}.
     *
     * @param title The title of the error.
     * @param text  The text of the error.
     * @return The answer of the user or {@code null} if no answer has been made.
     */
    public static ButtonType showError(final String title, final String text) {
        return displayDialog(buildAlert(Alert.AlertType.ERROR, title, text, ButtonType.OK));
    }

    /**
     * Show an error alert with the given {@code title} and {@code content}. The method returns the answer of the user
     * which can only be {@link ButtonType#OK}.
     *
     * @param title   The title of the error.
     * @param content The content of the error.
     * @return The answer of the user or {@code null} if no answer has been made.
     */
    public static ButtonType showError(final String title, final Node content) {
        return displayDialog(buildAlert(Alert.AlertType.ERROR, title, content, ButtonType.OK));
    }

    /**
     * Show a dialog that can be cancelled with the given {@code title} and {@code content}. The method returns the
     * answer of the user which can be {@link ButtonType#CANCEL} or
     * {@link ButtonType#OK}.
     *
     * @param title   The title of the dialog.
     * @param content The content of the dialog.
     * @return The answer of the user or {@code null} if no answer has been made.
     */
    public static ButtonType showCancellableDialog(final String title, final Node content) {
        return showCancellableDialog(title, content, null);
    }

    /**
     * Show a dialog that can be cancelled with the given {@code title} and {@code content}. The method returns the
     * answer of the user which can be {@link ButtonType#CANCEL} or {@link ButtonType#OK}. The {@code validationProperty}
     * allows to disable the OK button of the dialog if there is one.
     *
     * @param title              The title of the dialog.
     * @param content            The content of the dialog.
     * @param validationProperty The property used to validate the dialog or not. Can be {@code null}.
     * @return The answer of the user or {@code null} if no answer has been made.
     */
    public static ButtonType showCancellableDialog(final String title, final Node content, final ReadOnlyBooleanProperty validationProperty) {
        final Dialog dialog = buildDialog(title, content, ButtonType.CANCEL, ButtonType.OK);

        if (dialog != null && validationProperty != null) {
            final DialogPane pane = dialog.getDialogPane();
            if (pane != null) {
                final Button okButton = (Button) pane.lookupButton(ButtonType.OK);
                okButton.disableProperty().bind(validationProperty.not());
            }
        }

        return displayDialog(dialog);
    }

    /**
     * Show a dialog that contains the given {@code title}, {@code content} and with the given {@code buttons}.
     *
     * @param title   The title of the dialog.
     * @param content The content of the dialog.
     * @param buttons The buttons of the dialog.
     * @return The answer of the user or {@code null} if no answer has been made.
     */
    public static ButtonType showDialog(final String title, final Node content, ButtonType... buttons) {
        return displayDialog(buildDialog(title, content, buttons));
    }

    /**
     * Build an {@link Alert Alert} object. This method ensures the alert is created in a JavaFX
     * application thread. If the alert can not be created then {@code null} is returned.
     *
     * @param type    The type of alert to create.
     * @param title   The title of the alert.
     * @param text    The text of this alert.
     * @param buttons The buttons the alert will contain.
     * @return A well created Alert or {@code null} if an error occurred during the creation of the alert.
     */
    private static Alert buildAlert(final Alert.AlertType type, final String title, final String text, final ButtonType... buttons) {
        return buildAlert(type, title, text, null, buttons);
    }

    /**
     * Build an {@link Alert Alert} object. This method ensures the alert is created in a JavaFX
     * application thread. If the alert can not be created then {@code null} is returned.
     *
     * @param type    The type of alert to create.
     * @param title   The title of the alert.
     * @param content The content of this alert.
     * @param buttons The buttons the alert will contain.
     * @return A well created Alert or {@code null} if an error occurred during the creation of the alert.
     */
    private static Alert buildAlert(final Alert.AlertType type, final String title, final Node content, final ButtonType... buttons) {
        return buildAlert(type, title, null, content, buttons);
    }

    /**
     * Build an {@link Alert Alert} object. This method ensures the alert is created in a JavaFX
     * application thread. If the alert can not be created then {@code null} is returned.
     *
     * @param type        The type of alert to create.
     * @param title       The title of the alert.
     * @param contentText The text of this alert.
     * @param content     The content of this alert.
     * @param buttons     The buttons the alert will contain.
     * @return A well created Alert or {@code null} if an error occurred during the creation of the alert.
     */
    private static Alert buildAlert(final Alert.AlertType type, final String title, final String contentText, final Node content, final ButtonType... buttons) {
        final FutureTask<Alert> future = new FutureTask<>(() -> {
            final Alert alert = new Alert(type, contentText, buttons);
            alert.setGraphic(null);
            alert.setHeaderText(null);
            alert.setTitle(title);
            if (content != null) {
                alert.getDialogPane().setContent(content);
            }
            styleDialog(alert);

            return alert;
        });

        PlatformHelper.run(future);

        Alert alert = null;
        try {
            alert = future.get();
        } catch (ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Can not build an alert", e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Can not build an alert", e);
            Thread.currentThread().interrupt();
        }

        return alert;
    }

    /**
     * Build a {@link Dialog Dialog} object. This method ensures the dialog is created in a JavaFX
     * application thread. If the dialog can not be created then {@code null} is returned.
     *
     * @param title   The title of the Dialog
     * @param content The content of the Dialog
     * @param buttons The type of buttons the Dialog will contain.
     * @return A well created Dialog or {@code null} if an error occurred during the creation of the Dialog.
     */
    private static Dialog buildDialog(final String title, final Node content, final ButtonType... buttons) {
        final FutureTask<Dialog> future = new FutureTask<>(() -> {
            final Dialog dialog = new Dialog();
            dialog.setGraphic(null);
            dialog.setHeaderText(null);
            dialog.setTitle(title);
            dialog.getDialogPane().getButtonTypes().addAll(buttons);
            dialog.getDialogPane().setContent(content);
            styleDialog(dialog);

            return dialog;
        });

        PlatformHelper.run(future);

        Dialog dialog = null;
        try {
            dialog = future.get();
        } catch (ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Can not build a Dialog", e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Can not build a Dialog", e);
            Thread.currentThread().interrupt();
        }

        return dialog;
    }

    /**
     * Apply the style and theme to the given {@link Dialog}.
     *
     * @param dialog The dialog to style.
     */
    private static void styleDialog(final Dialog dialog) {
        final URL applicationStyle = Styles.getApplicationStyle();
        if (dialog != null && applicationStyle != null) {
            dialog.getDialogPane().getStylesheets().add(applicationStyle.toExternalForm());
            Themes.applyTheme(dialog.getDialogPane(), GlobalConfiguration.getThemeName());
        }
    }

    /**
     * Show and wait for the response of the given {@code dialog}. This method ensures the dialog is displayed in the
     * JavaFX application thread.
     *
     * @param dialog The dialog to show.
     * @return The answer of the user or {@code null} if no answer has been made.
     */
    private static ButtonType displayDialog(Dialog<ButtonType> dialog) {
        Optional<ButtonType> response = Optional.empty();

        if (dialog != null) {
            final FutureTask<Optional<ButtonType>> future = new FutureTask<>(dialog::showAndWait);
            PlatformHelper.run(future);
            try {
                response = future.get();
            } catch (ExecutionException e) {
                LOGGER.log(Level.SEVERE, "Can not show dialog", e);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Can not show dialog", e);
                Thread.currentThread().interrupt();
            }
        }

        return response.orElse(null);
    }
}

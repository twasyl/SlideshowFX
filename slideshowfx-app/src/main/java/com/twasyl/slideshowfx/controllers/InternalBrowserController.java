package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.style.Styles;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * The controller class for the internal browser view.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class InternalBrowserController implements ThemeAwareController {
    private static final Logger LOGGER = Logger.getLogger(InternalBrowserController.class.getName());

    @FXML
    private BorderPane root;
    @FXML
    private TextField addressBar;
    @FXML
    private WebView browser;
    @FXML
    private HBox addressPanel;
    @FXML
    private Button previousPage;
    @FXML
    private Button nextPage;

    private final ContextMenu browsingHistoryContextMenu = new ContextMenu();

    /**
     * This method is called when a key is pressed within the address bar.
     *
     * @param event The event source.
     */
    @FXML
    private void manageKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            this.browsingHistoryContextMenu.hide();
            this.loadPage(this.addressBar.getText());
        }
    }

    @FXML
    private void navigateToPreviousPage(ActionEvent event) {
        this.goToPage(-1);
    }

    @FXML
    private void navigateToNextPage(ActionEvent event) {
        this.goToPage(1);
    }

    /**
     * Loads the given {@code address} in the browser. If the address to load doesn't start with {@code http://} or
     * {@code https://}, {@code http://} will be appended at the beginning of the address to load.
     * If the address is {@code null} or empty, nothing will be thrown and an error will not be raised.
     *
     * @param address The address to load.
     */
    private void loadPage(final String address) {
        if (address != null && !address.isEmpty()) {
            final StringBuilder builder = new StringBuilder();

            if (!address.startsWith("http://") && !address.startsWith("https://") && !address.startsWith("file://")) {
                builder.append("http://");
            }

            builder.append(address);

            this.browser.getEngine().load(builder.toString());

        }
    }

    /**
     * Navigate to the browsing history. This method calls {@link WebHistory#go(int)} with the {@code offset} passed in
     * parameter.
     *
     * @param offset The offset to navigate to the page.
     */
    private void goToPage(int offset) {
        this.browser.getEngine().getHistory().go(offset);
        this.addressBar.setText(this.browser.getEngine().getLocation());
        this.browsingHistoryContextMenu.hide();
    }

    /**
     * Displays the context menu for the address bar which contains the URL starting with {@code partialAddress} in the
     * browsing history.
     * If the {@code partialAddress} is {@code null} or empty, nothing is displayed.
     *
     * @param partialAddress The partial address to look in the browsing history.
     */
    private void displayBrowsingHistorySuggestion(final String partialAddress) {
        this.browsingHistoryContextMenu.getItems().clear();

        if (partialAddress != null && !partialAddress.isEmpty()) {
            this.browser.getEngine().getHistory().getEntries().forEach(entry -> {
                try {
                    final URL url = new URL(entry.getUrl());
                    final String lowerCasePartialAddress = partialAddress.toLowerCase();

                    if (entry.getUrl().toLowerCase().startsWith(lowerCasePartialAddress) ||
                            (url.getAuthority() != null && (url.getAuthority().startsWith(lowerCasePartialAddress) ||
                                    (url.getAuthority().startsWith("www.") && url.getAuthority().startsWith(lowerCasePartialAddress, 4)))) ||
                            (entry.getTitle() != null && entry.getTitle().toLowerCase().startsWith(lowerCasePartialAddress))) {
                        final MenuItem menuItem = new Menu(entry.getUrl());
                        menuItem.setOnAction(event -> {
                            this.addressBar.setText(entry.getUrl());
                            this.browsingHistoryContextMenu.hide();
                            this.loadPage(entry.getUrl());
                        });

                        this.browsingHistoryContextMenu.getItems().add(menuItem);
                    }
                } catch (MalformedURLException e) {
                    LOGGER.log(WARNING, "Invalid URL", e);
                }

            });

            if (!this.browsingHistoryContextMenu.getItems().isEmpty()) {
                this.browsingHistoryContextMenu.show(this.addressBar, Side.BOTTOM, 0, 0);
            }
        } else {
            this.browsingHistoryContextMenu.hide();
        }
    }

    /**
     * Style the empty-webview page to inject the CSS according the current {@link com.twasyl.slideshowfx.style.theme.Theme}.
     */
    private void styleEmptyWebView() {
        final Document document = this.browser.getEngine().getDocument();
        final Element style = document.createElement("link");
        style.setAttribute("rel", "stylesheet");
        style.setAttribute("type", "text/css");
        style.setAttribute("href", Styles.getEmptyWebViewStyle().toExternalForm());

        final Node head = document.getElementsByTagName("head").item(0);
        head.appendChild(style);

        final Node body = document.getElementsByTagName("body").item(0);
        Attr classAttr = (Attr) body.getAttributes().getNamedItem("class");

        if (classAttr == null) {
            classAttr = document.createAttribute("class");
        }

        classAttr.setValue(GlobalConfiguration.getThemeName().toLowerCase());
        body.getAttributes().setNamedItem(classAttr);
    }

    @Override
    public Parent getRoot() {
        return this.root;
    }

    @Override
    public void postInitialize(URL location, ResourceBundle resources) {
        this.browsingHistoryContextMenu.setAutoFix(true);
        this.browsingHistoryContextMenu.setHideOnEscape(true);

        this.addressBar.textProperty().addListener((textValue, oldText, newText) -> this.displayBrowsingHistorySuggestion(newText));

        this.browser.getEngine().getLoadWorker().stateProperty().addListener((value, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED && "SlideshowFX internal browser".equals(this.browser.getEngine().getTitle())) {
                this.styleEmptyWebView();
            }
        });

        this.browser.getEngine().load(InternalBrowserController.class.getResource("/com/twasyl/slideshowfx/html/empty-webview.html").toExternalForm());

        final WebHistory webHistory = this.browser.getEngine().getHistory();
        BooleanBinding isHistoryEmpty = Bindings.isEmpty(webHistory.getEntries());
        BooleanBinding isFirstPageDisplayed = isHistoryEmpty.or(webHistory.currentIndexProperty().isEqualTo(0));
        BooleanBinding isLastPageDisplayed = isHistoryEmpty.or(webHistory.currentIndexProperty().isEqualTo(Bindings.size(webHistory.getEntries()).subtract(1)));

        this.previousPage.disableProperty().bind(isFirstPageDisplayed);
        this.nextPage.disableProperty().bind(isLastPageDisplayed);
    }
}

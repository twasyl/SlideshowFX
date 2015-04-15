/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller class for the internal browser view.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class InternalBrowserController implements Initializable {

    @FXML private TextField addressBar;
    @FXML private WebView browser;

    private final ContextMenu browsingHistoryContextMenu = new ContextMenu();

    /**
     * This method is called when a key is pressed within the address bar.
     * @param event The event source.
     */
    @FXML private void manageKeyPressed(KeyEvent event) {

        if(event.getCode() == KeyCode.ENTER) {
            this.browsingHistoryContextMenu.hide();
            this.loadPage(this.addressBar.getText());
        }
    }


    /**
     * Loads the given {@code address} in the browser. If the address to load doesn't start with {@code http://} or
     * {@code https://}, {@code http://} will be appended at the beginning of the address to load.
     * If the address is {@code null} or empty, nothing will be thrown and an error will not be raised.
     * @param address The address to load.
     */
    private void loadPage(final String address) {
        if(address != null && !address.isEmpty()) {
            final StringBuilder builder = new StringBuilder();

            if(!address.startsWith("http://") && !address.startsWith("https://")) builder.append("http://");

            builder.append(address);

            this.browser.getEngine().load(builder.toString());

        }
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

        if(partialAddress != null && !partialAddress.isEmpty()) {
            this.browser.getEngine().getHistory().getEntries().forEach(entry -> {
                try {
                    final URL url = new URL(entry.getUrl());
                    final String lowerCasePartialAddress = partialAddress.toLowerCase();

                    if(entry.getUrl().toLowerCase().startsWith(lowerCasePartialAddress) ||
                            (url.getAuthority() != null && (url.getAuthority().startsWith(lowerCasePartialAddress) ||
                            (url.getAuthority().startsWith("www.") && url.getAuthority().substring(4).startsWith(lowerCasePartialAddress)))) ||
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
                    e.printStackTrace();
                }

            });

            if(!this.browsingHistoryContextMenu.getItems().isEmpty()) {
                this.browsingHistoryContextMenu.show(this.addressBar, Side.BOTTOM, 0, 0);
            }
        } else {
            this.browsingHistoryContextMenu.hide();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.browsingHistoryContextMenu.setAutoFix(true);
        this.browsingHistoryContextMenu.setHideOnEscape(true);

        this.addressBar.textProperty().addListener((textValue, oldText, newText) -> {
            this.displayBrowsingHistorySuggestion(newText);
        });

        this.browser.getEngine().load(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/html/empty-webview.html"));
    }
}

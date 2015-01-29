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

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the controller for the view {@see OptionsView.fxml}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class OptionsViewController implements Initializable {
    private static Logger LOGGER = Logger.getLogger(OptionsViewController.class.getName());

    @FXML private Tab snippetExecutorTab;

    private List<TextField> snippetExecutorsSdkHome = new ArrayList<>();

    /**
     * This methods saves the options displayed in the view and make them persistent.
     */
    public void saveOptions() {
        this.snippetExecutorsSdkHome.forEach(tf -> {
            final ISnippetExecutor executor = (ISnippetExecutor) tf.getUserData();

            try {
                executor.saveSdkHome(new File(tf.getText()));
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.WARNING, "Can not save SDK home for snippet executor ".concat(executor.getCode()), e);
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        final VBox snippetExecutors = (VBox) this.snippetExecutorTab.getContent();

        /**
         * Create a label and a TextField for each instance registered as a {@link com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor}
         * in order to define the SDK home for each ISnippetExecutor
         */
        OSGiManager.getInstalledServices(ISnippetExecutor.class)
                .forEach(snippet -> {
                    final Label label = new Label(snippet.getLanguage().concat(":"));

                    final TextField sdkHome = new TextField(snippet.getSdkHome() != null ?
                            snippet.getSdkHome().getAbsolutePath().replaceAll("\\\\", "/")
                            : "");
                    sdkHome.setPrefColumnCount(20);
                    sdkHome.setUserData(snippet);

                    final Button browse = new Button("...");
                    browse.setOnAction(event -> {
                        final DirectoryChooser chooser = new DirectoryChooser();
                        final File sdkHomeDir = chooser.showDialog(SlideshowFX.getStage());
                        if(sdkHomeDir != null) {
                            sdkHome.setText(sdkHomeDir.getAbsolutePath().replaceAll("\\\\", "/"));
                        }

                    });

                    final HBox box = new HBox(5);
                    box.getChildren().addAll(label, sdkHome, browse);

                    snippetExecutors.getChildren().add(box);
                    this.snippetExecutorsSdkHome.add(sdkHome);
                });
    }
}

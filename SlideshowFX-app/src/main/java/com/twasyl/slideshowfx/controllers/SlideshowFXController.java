/*
 * Copyright 2014 Thierry Wasylczenko
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
import com.twasyl.slideshowfx.builder.PresentationBuilder;
import com.twasyl.slideshowfx.builder.Slide;
import com.twasyl.slideshowfx.builder.template.SlideTemplate;
import com.twasyl.slideshowfx.chat.Chat;
import com.twasyl.slideshowfx.controls.SlideMenuItem;
import com.twasyl.slideshowfx.controls.SlideShowScene;
import com.twasyl.slideshowfx.exceptions.InvalidPresentationConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateException;
import com.twasyl.slideshowfx.exceptions.PresentationException;
import com.twasyl.slideshowfx.io.SlideshowFXExtensionFilter;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.MarkupManager;
import com.twasyl.slideshowfx.utils.DOMUtils;
import com.twasyl.slideshowfx.utils.NetworkUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import netscape.javascript.JSObject;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SlideshowFXController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(SlideshowFXController.class.getName());

    private final PresentationBuilder builder = new PresentationBuilder();

    private final EventHandler<ActionEvent> addSlideActionEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            try {

                Object userData = ((MenuItem) actionEvent.getSource()).getUserData();
                if(userData instanceof SlideTemplate) {
                    final String slideId = (String) SlideshowFXController.this.browser.getEngine().executeScript(SlideshowFXController.this.builder.getTemplate().getGetCurrentSlideMethod() + "();");
                    String slideNumber = null;

                    if(slideId != null && !slideId.isEmpty()) {
                        slideNumber = slideId.substring(SlideshowFXController.this.builder.getTemplate().getSlideIdPrefix().length());
                    }

                    Slide addedSlide = SlideshowFXController.this.builder.addSlide((SlideTemplate) userData, slideNumber);
                    SlideshowFXController.this.browser.getEngine().reload();

                    SlideshowFXController.this.updateSlideSplitMenu();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
    };

    private final EventHandler<ActionEvent> moveSlideActionEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            final SlideMenuItem menunItem = (SlideMenuItem) actionEvent.getSource();

            final String slideId = (String) SlideshowFXController.this.browser.getEngine().executeScript(SlideshowFXController.this.builder.getTemplate().getGetCurrentSlideMethod() + "();");
            final String slideNumber = slideId.substring(SlideshowFXController.this.builder.getTemplate().getSlideIdPrefix().length());

            Slide slideToMove = SlideshowFXController.this.builder.getPresentation().getSlide(slideNumber);

            SlideshowFXController.this.builder.getPresentation().getSlides().remove(slideToMove);

            int index = -1;
            for(int i = 0; i < SlideshowFXController.this.builder.getPresentation().getSlides().size(); i++) {
                if(SlideshowFXController.this.builder.getPresentation().getSlides().get(i).getSlideNumber().equals(menunItem.getSlide().getSlideNumber())) {
                    index = i;
                    break;
                }
            }

            SlideshowFXController.this.builder.getPresentation().getSlides().add(index, slideToMove);

            try {
                SlideshowFXController.this.builder.saveTemporaryPresentation();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            SlideshowFXController.this.browser.getEngine().reload();
            SlideshowFXController.this.updateSlideSplitMenu();
        }
    };

    @FXML private WebView browser;
    @FXML private SplitMenuButton saveButton;
    @FXML private SplitMenuButton addSlideButton;
    @FXML private SplitMenuButton moveSlideButton;
    @FXML private TextField slideNumber;
    @FXML private TextField fieldName;
    @FXML private HBox markupContentTypeBox;
    @FXML private ToggleGroup markupContentType;
    @FXML private TextArea fieldValueText;
    @FXML private TextField chatIpAddress;
    @FXML private TextField chatPort;
    @FXML private TextField twitterHashtag;
    @FXML private CheckBox leapMotionEnabled;

    @FXML private void loadTemplate(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.TEMPLATE_FILTER);
        File templateFile = chooser.showOpenDialog(null);

        if(templateFile != null) {
            try {
                this.builder.loadTemplate(templateFile);
                this.browser.getEngine().load(this.builder.getPresentation().getPresentationFile().toURI().toASCIIString());

                this.updateSlideTemplatesSplitMenu();
            } catch (InvalidTemplateException e) {
                e.printStackTrace();
            } catch (InvalidTemplateConfigurationException e) {
                e.printStackTrace();
            } catch (PresentationException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void openPresentation(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PRESENTATION_FILES);
        File file = chooser.showOpenDialog(null);

        if(file != null) {
            try {
                this.builder.openPresentation(file);
                this.browser.getEngine().load(this.builder.getPresentation().getPresentationFile().toURI().toASCIIString());

                this.updateSlideTemplatesSplitMenu();
                this.updateSlideSplitMenu();
            } catch (InvalidTemplateConfigurationException e) {
                e.printStackTrace();
            } catch (InvalidTemplateException e) {
                e.printStackTrace();
            } catch (PresentationException e) {
                e.printStackTrace();
            } catch (InvalidPresentationConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void updateSlideWithText(ActionEvent event) throws TransformerException, IOException, ParserConfigurationException, SAXException {
        RadioButton selectedMarkup = (RadioButton) this.markupContentType.getSelectedToggle();
        this.updateSlide(MarkupManager.convertToHtml((String) selectedMarkup.getUserData(), this.fieldValueText.getText()));
    }

    private void updateSlide(String content) throws TransformerException, IOException, ParserConfigurationException, SAXException {
        String clearedContent = Base64.getEncoder().encodeToString(content.getBytes("UTF8"));
        String jsCommand = String.format("%1$s(%2$s, \"%3$s\", '%4$s');",
                this.builder.getTemplate().getContentDefinerMethod(),
                this.slideNumber.getText(),
                this.fieldName.getText(),
               clearedContent);

        this.browser.getEngine().executeScript(jsCommand);
        Element slideElement = this.browser.getEngine().getDocument().getElementById(this.builder.getTemplate().getSlideIdPrefix() + this.slideNumber.getText());

        this.builder.getPresentation().updateSlideText(this.slideNumber.getText(), DOMUtils.convertNodeToText(slideElement));
        this.builder.saveTemporaryPresentation();

        // Take a thumbnail of the slide
        WritableImage thumbnail = this.browser.snapshot(null, null);
        this.builder.getPresentation().updateSlideThumbnail(this.slideNumber.getText(), thumbnail);

        updateSlideSplitMenu();
    }

    @FXML private void copySlide(ActionEvent event) {
        final String slideId = (String) this.browser.getEngine().executeScript(this.builder.getTemplate().getGetCurrentSlideMethod() + "();");
        final String slideNumber = slideId.substring(this.builder.getTemplate().getSlideIdPrefix().length());

        Slide slideToCopy = this.builder.getPresentation().getSlide(slideNumber);
        Slide copy = this.builder.duplicateSlide(slideToCopy);

        int index = this.builder.getPresentation().getSlides().indexOf(slideToCopy);
        if(index != -1) {
            if(index == this.builder.getPresentation().getSlides().size() - 1) {
                this.builder.getPresentation().getSlides().add(copy);
            } else {
                this.builder.getPresentation().getSlides().add(index + 1, copy);
            }
        }

        try {
            this.builder.saveTemporaryPresentation();
            this.updateSlideSplitMenu();

            this.browser.getEngine().reload();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void deleteSlide(ActionEvent event) {
        String slideId = this.browser.getEngine().executeScript(this.builder.getTemplate().getGetCurrentSlideMethod() + "();").toString();

        if(slideId != null && !slideId.isEmpty()) {
            String slideNumber = slideId.substring(this.builder.getTemplate().getSlideIdPrefix().length());

            try {
                this.builder.deleteSlide(slideNumber);
                SlideshowFXController.this.browser.getEngine().reload();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void reload(ActionEvent event) {
        this.browser.getEngine().reload();
    }

    @FXML
    private void save(ActionEvent event) {
        File presentationArchive = null;
        if(this.builder.getPresentationArchiveFile() == null) {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PRESENTATION_FILES);
            presentationArchive = chooser.showSaveDialog(null);
        } else {
            presentationArchive = this.builder.getPresentationArchiveFile();
        }

        try {
            this.builder.savePresentation(presentationArchive);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveAs(ActionEvent event) {
        File presentationArchive = null;
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PRESENTATION_FILES);
        presentationArchive = chooser.showSaveDialog(null);

        try {
            this.builder.setPresentationArchiveFile(presentationArchive);
            this.builder.savePresentation(presentationArchive);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void print(ActionEvent event) {
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null) {
            if(job.showPrintDialog(null)) {
                this.browser.getEngine().print(job);
                job.endJob();
            } else {
                job.cancelJob();
            }
        }
    }

    @FXML private void slideShow(ActionEvent event) {
        if(this.builder.getPresentation() != null
                && this.builder.getPresentation().getPresentationFile() != null
                && this.builder.getPresentation().getPresentationFile().exists()) {
            final WebView slideShowBrowser = new WebView();
            slideShowBrowser.getEngine().load(this.builder.getPresentation().getPresentationFile().toURI().toASCIIString());

            final SlideShowScene subScene = new SlideShowScene(slideShowBrowser);

            SlideshowFX.setSlideShowScene(subScene);
        }
    }

    @FXML private void startChat(ActionEvent event) {
        Image icon;

        if(Chat.isOpened()) {
            Chat.close();

            icon = new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/start.png"));
        } else {
            String ip = this.chatIpAddress.getText();
            if(ip == null || ip.isEmpty()) {
                ip = NetworkUtils.getIP();
            }

            int port = 80;
            if(this.chatPort.getText() != null && !this.chatPort.getText().isEmpty()) {
                port = Integer.parseInt(this.chatPort.getText());
            }

            this.chatIpAddress.setText(ip);
            this.chatPort.setText(port + "");

            Chat.create(ip, port, this.twitterHashtag.getText());

            icon = new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/shutdown.png"));
        }

        ((ImageView) ((Button) event.getSource()).getGraphic()).setImage(icon);
    }

    @FXML private void insertImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File imageFile = chooser.showOpenDialog(null);

        if(imageFile != null) {
            File targetFile;
            if(imageFile.exists()) {
                // If the file exists, add a timestamp to the source
                targetFile = new File(this.builder.getTemplate().getResourcesDirectory(), System.currentTimeMillis() + imageFile.getName());
            } else {
                targetFile = new File(this.builder.getTemplate().getResourcesDirectory(), imageFile.getName());
            }

            try {
                Files.copy(imageFile.toPath(), targetFile.toPath());

                // Get the relative path of the resources folder from the template directory
                final Path relativePath = this.builder.getTemplate().getFolder().toPath().relativize(this.builder.getTemplate().getResourcesDirectory().toPath());

                final String imgMarkup = String.format("<img src=\"%1$s/%2$s\" />", relativePath.toString(), targetFile.getName());

                this.insertText(this.fieldValueText, imgMarkup, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void insertQuote(ActionEvent event) {
        String quoteMarkup = "<blockquote></blockquote>";

        this.insertText(this.fieldValueText, quoteMarkup, 12);

        this.fieldValueText.requestFocus();
    }

    @FXML private void insertChatQRCode(ActionEvent event) {
        final String qrCode = String.format("<img src=\"http://%1$s:%2$s/images/chatQRCode.png\" />",
                Chat.getIp(), Chat.getPort());


        this.insertText(this.fieldValueText, qrCode, null);

        this.fieldValueText.requestFocus();
    }

    private void insertText(TextInputControl input, String textToInsert, Integer moveCaretPosition) {
        if(!input.getText().isEmpty()) {
            final int currentCarret = input.getCaretPosition();
            final String firstPart = input.getText().substring(0, currentCarret);
            final String secondPart = input.getText().substring(currentCarret);

            input.setText(String.format("%1$s%2$s%3$s", firstPart, textToInsert, secondPart));

            if(moveCaretPosition != null) {
                input.positionCaret(currentCarret + moveCaretPosition);
            } else {
                input.positionCaret(currentCarret + textToInsert.length());
            }
        } else {
            input.setText(textToInsert);

            if(moveCaretPosition != null) {
                input.positionCaret(input.getCaretPosition() + moveCaretPosition);
            } else {
                input.positionCaret(input.getCaretPosition() + textToInsert.length());
            }
        }
    }

    private void updateSlideTemplatesSplitMenu() {
        this.addSlideButton.getItems().clear();
        if(this.builder.getTemplate() != null) {
            MenuItem item;

            for(SlideTemplate slideTemplate : this.builder.getTemplate().getSlideTemplates()) {
                item = new MenuItem();
                item.setText(slideTemplate.getName());
                item.setUserData(slideTemplate);
                item.setOnAction(addSlideActionEvent);
                this.addSlideButton.getItems().add(item);
            }
        }
    }

    private void updateSlideSplitMenu() {
        SlideshowFXController.this.moveSlideButton.getItems().clear();
        SlideMenuItem menuItem;
        for(Slide slide : SlideshowFXController.this.builder.getPresentation().getSlides()) {
            menuItem = new SlideMenuItem(slide);
            menuItem.setOnAction(SlideshowFXController.this.moveSlideActionEvent);
            SlideshowFXController.this.moveSlideButton.getItems().add(menuItem);
        }
    }

    public void prefillContentDefinition(String slideNumber, String field, String value) {
        this.slideNumber.setText(slideNumber);
        this.fieldName.setText(field);
        try {
            this.fieldValueText.setText(new String(Base64.getDecoder().decode(value), "UTF8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING, "Can not prefill content definition with the given value", e);
        }

        this.fieldValueText.requestFocus();
        this.fieldValueText.selectAll();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Make this controller available to JavaScript
        this.browser.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State state, Worker.State state2) {
                if(state2 == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) browser.getEngine().executeScript("window");
                    window.setMember(SlideshowFXController.this.builder.getTemplate().getJsObject(), SlideshowFXController.this);
                }
            }
        });

        this.browser.getEngine().setJavaScriptEnabled(true);

        this.saveButton.setGraphic(
                new ImageView(
                        new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/save.png"), 20d, 20d, true, true)
                )
        );

        this.addSlideButton.setGraphic(
                new ImageView(
                        new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/add.png"), 20d, 20d, true, true)
                )
        );

        this.moveSlideButton.setGraphic(
                new ImageView(
                        new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/move.png"), 20d, 20d, true, true)
                )
        );

        SlideshowFX.leapMotionAllowedProperty().bind(this.leapMotionEnabled.selectedProperty());
        this.leapMotionEnabled.setSelected(true);

        // Creating RadioButtons for each markup bundle installed
        MarkupManager.getInstalledMarkupSyntax()
                .stream().sorted((markup1, markup2) -> {
                            return markup1.getName().compareTo(markup2.getName());
                        })
                .forEach(markup -> {
                            RadioButton button = new RadioButton(markup.getName());
                            button.setUserData(markup.getCode());
                            markupContentType.getToggles().add(button);
                            markupContentTypeBox.getChildren().add(button);
                });
    }
}
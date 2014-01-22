package com.twasyl.slideshowfx.controllers;

import com.github.rjeschke.txtmark.Processor;
import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.chat.Chat;
import com.twasyl.slideshowfx.controls.SlideMenuItem;
import com.twasyl.slideshowfx.controls.SlideShowScene;
import com.twasyl.slideshowfx.exceptions.InvalidPresentationConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateConfigurationException;
import com.twasyl.slideshowfx.exceptions.InvalidTemplateException;
import com.twasyl.slideshowfx.exceptions.PresentationException;
import com.twasyl.slideshowfx.io.SlideshowFXExtensionFilter;
import com.twasyl.slideshowfx.utils.DOMUtils;
import com.twasyl.slideshowfx.utils.NetworkUtils;
import com.twasyl.slideshowfx.utils.PresentationBuilder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import netscape.javascript.JSObject;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class SlideshowFXController implements Initializable {

    private final PresentationBuilder builder = new PresentationBuilder();

    private final EventHandler<ActionEvent> addSlideActionEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            try {

                Object userData = ((MenuItem) actionEvent.getSource()).getUserData();
                if(userData instanceof PresentationBuilder.SlideTemplate) {
                    final String slideId = (String) SlideshowFXController.this.browser.getEngine().executeScript(SlideshowFXController.this.builder.getTemplate().getGetCurrentSlideMethod() + "();");
                    String slideNumber = null;

                    if(slideId != null && !slideId.isEmpty()) {
                        slideNumber = slideId.substring(SlideshowFXController.this.builder.getTemplate().getSlideIdPrefix().length());
                    }

                    PresentationBuilder.Slide addedSlide = SlideshowFXController.this.builder.addSlide((PresentationBuilder.SlideTemplate) userData, slideNumber);
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

            PresentationBuilder.Slide slideToMove = SlideshowFXController.this.builder.getPresentation().getSlide(slideNumber);

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
    @FXML private SplitMenuButton addSlideButton;
    @FXML private SplitMenuButton moveSlideButton;
    @FXML private TextField slideNumber;
    @FXML private TextField fieldName;
    @FXML private RadioButton htmlContent;
    @FXML private RadioButton markdownContent;
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
        if(this.markdownContent.isSelected()) {
            this.updateSlide(Processor.process("[$PROFILE$]: extended\n" + this.fieldValueText.getText()));
        } else {
            this.updateSlide(this.fieldValueText.getText());
        }
    }

    private void updateSlide(String content) throws TransformerException, IOException, ParserConfigurationException, SAXException {

        String clearedContent = content.replaceAll("\\n", "&#10;")
                .replaceAll("\\\\", "&#92;")
                .replaceAll("\'", "&#39;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");

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

        PresentationBuilder.Slide slideToCopy = this.builder.getPresentation().getSlide(slideNumber);
        PresentationBuilder.Slide copy = this.builder.duplicateSlide(slideToCopy);

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

                this.fieldValueText.setText(this.fieldValueText.getText() + imgMarkup);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void insertQuote(ActionEvent event) {
        String quoteMarkup = "<blockquote></blockquote>";
        this.fieldValueText.setText(this.fieldValueText.getText() + quoteMarkup);

        this.fieldValueText.requestFocus();
        this.fieldValueText.positionCaret(this.fieldValueText.getText().length() - 13);
    }

    private void updateSlideTemplatesSplitMenu() {
        this.addSlideButton.getItems().clear();
        if(this.builder.getTemplate() != null) {
            MenuItem item;

            for(PresentationBuilder.SlideTemplate slideTemplate : this.builder.getTemplate().getSlideTemplates()) {
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
        for(PresentationBuilder.Slide slide : SlideshowFXController.this.builder.getPresentation().getSlides()) {
            menuItem = new SlideMenuItem(slide);
            menuItem.setOnAction(SlideshowFXController.this.moveSlideActionEvent);
            SlideshowFXController.this.moveSlideButton.getItems().add(menuItem);
        }
    }

    public void prefillContentDefinition(String slideNumber, String field, String value) {
        this.slideNumber.setText(slideNumber);
        this.fieldName.setText(field);
        this.fieldValueText.setText(value);

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

        this.leapMotionEnabled.selectedProperty().bindBidirectional(SlideshowFX.leapMotionAllowedProperty());
        this.leapMotionEnabled.setSelected(true);
    }
}

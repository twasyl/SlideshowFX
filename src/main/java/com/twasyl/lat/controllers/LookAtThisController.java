package com.twasyl.lat.controllers;

import com.twasyl.lat.utils.DOMUtils;
import com.twasyl.lat.utils.PresentationBuilder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import netscape.javascript.JSObject;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LookAtThisController implements Initializable {

    private final PresentationBuilder builder = new PresentationBuilder();

    private final EventHandler<ActionEvent> addSlideActionEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            try {

                Object userData = ((MenuItem) actionEvent.getSource()).getUserData();
                if(userData instanceof PresentationBuilder.Slide) {
                    LookAtThisController.this.builder.addSlide((PresentationBuilder.Slide) userData, null);
                    LookAtThisController.this.browser.getEngine().reload();
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

    @FXML private WebView browser;
    @FXML private SplitMenuButton addSlideButton;
    @FXML private TextField slideNumber;
    @FXML private TextField fieldName;
    @FXML private TextArea fieldValue;

    @FXML private void loadTemplate(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File templateFile = chooser.showOpenDialog(null);

        try {
            this.builder.loadTemplate(templateFile);
            this.browser.getEngine().load(this.builder.getPresentation().getPresentationFile().toURI().toASCIIString());

            this.addSlideButton.getItems().clear();
            if(this.builder.getTemplate() != null) {
                MenuItem item;

                for(PresentationBuilder.Slide slide : this.builder.getTemplate().getSlides()) {
                    item = new MenuItem();
                    item.setText(slide.getName());
                    item.setUserData(slide);
                    item.setOnAction(addSlideActionEvent);
                    this.addSlideButton.getItems().add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void openPresentation(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(null);
        try {
            this.builder.openPresentation(file);
            this.browser.getEngine().load(this.builder.getPresentation().getPresentationFile().toURI().toASCIIString());

            this.addSlideButton.getItems().clear();
            if(this.builder.getTemplate() != null) {
                MenuItem item;

                for(PresentationBuilder.Slide slide : this.builder.getTemplate().getSlides()) {
                    item = new MenuItem();
                    item.setText(slide.getName());
                    item.setUserData(slide);
                    item.setOnAction(addSlideActionEvent);
                    this.addSlideButton.getItems().add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
    @FXML private void updateSlide(ActionEvent event) throws TransformerException, IOException, ParserConfigurationException, SAXException {

        String jsCommand = String.format("%1$s(%2$s, \"%3$s\", \"%4$s\");",
                this.builder.getTemplate().getContentDefinerMethod(),
                this.slideNumber.getText(),
                this.fieldName.getText(),
                this.fieldValue.getText());

        this.browser.getEngine().executeScript(jsCommand);
        Element slideElement = this.browser.getEngine().getDocument().getElementById("slide-" + this.slideNumber.getText());

        this.builder.getPresentation().updateSlideText(this.slideNumber.getText(), DOMUtils.convertNodeToText(slideElement));
        this.builder.saveTemporaryPresentation();
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

    @FXML private void slideShow(ActionEvent event) {
        final WebView slideShowBrowser = new WebView();
        slideShowBrowser.getEngine().load(this.builder.getPresentation().getPresentationFile().toURI().toASCIIString());

        final Scene subScene = new Scene(slideShowBrowser);

        final Stage stage = new Stage();
        stage.setScene(subScene);
        stage.setFullScreen(true);
        stage.show();
        stage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                WindowEvent event = new WindowEvent(stage.getOwner(), WindowEvent.WINDOW_CLOSE_REQUEST);
                stage.fireEvent(event);
            }
        });
    }

    public void prefillContentDefinition(String slideNumber, String field, String value) {
        this.slideNumber.setText(slideNumber);
        this.fieldName.setText(field);
        this.fieldValue.setText(value);

        this.fieldValue.requestFocus();
        this.fieldValue.selectAll();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Make this controller available to JavaScript
        this.browser.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State state, Worker.State state2) {
                if(state2 == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) browser.getEngine().executeScript("window");
                    window.setMember(LookAtThisController.this.builder.getTemplate().getJsObject(), LookAtThisController.this);
                }
            }
        });
    }
}

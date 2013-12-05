package com.twasyl.lat.controllers;

import com.twasyl.lat.utils.DOMUtils;
import com.twasyl.lat.utils.PresentationBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LookAtThisController implements Initializable {

    private final PresentationBuilder builder = new PresentationBuilder();

    @FXML private WebView browser;
    @FXML private TextArea commands;
    @FXML private TextField slideNumber;
    @FXML private TextField fieldName;
    @FXML private TextField fieldValue;

    @FXML private void loadTemplate(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File templateFile = chooser.showOpenDialog(null);

        try {
            this.builder.loadTemplate(templateFile);
            this.browser.getEngine().load(this.builder.getPresentation().getPresentationFile().toURI().toASCIIString());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @FXML private void updateSlide(ActionEvent event) throws TransformerException, IOException {
        int slideNumberInt = Integer.parseInt(this.slideNumber.getText());

        String jsCommand = String.format("setField(%1$s, \"%2$s\", \"%3$s\");",
                slideNumberInt,
                this.fieldName.getText(),
                this.fieldValue.getText());

        this.browser.getEngine().executeScript(jsCommand);
        Element slideElement = this.browser.getEngine().getDocument().getElementById("slide-" + slideNumberInt);

        this.builder.getPresentation().getSlides().get(slideNumberInt-1).setText(DOMUtils.convertNodeToText(slideElement));
    }

    @FXML
    private void executeJS(ActionEvent event) {
        this.browser.getEngine().executeScript(commands.getText());
    }

    @FXML
    private void reload(ActionEvent event) {
        this.browser.getEngine().reload();
    }

    @FXML
    private void save(ActionEvent event) {
        /*if(this.presentationFile == null) throw new IllegalArgumentException("The presentation file can not be null");

        OutputStream out = null;

        try {
            this.browser.getEngine().getDocument().normalize();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMSource source = new DOMSource(this.browser.getEngine().getDocument());

            out = new FileOutputStream(this.presentationFile);
            StreamResult result = new StreamResult(out);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            transformer.transform(source, result);

            result.getOutputStream().flush();
            result.getOutputStream().close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TransformerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/

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

    @FXML
    private void addSlide(ActionEvent event) {
        try {
            this.builder.addSlide(this.builder.getTemplate().getSlides().get(0), null);
            this.browser.getEngine().reload();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    @FXML private void addSubslide(ActionEvent event) {
        try {
            this.builder.addSlide(this.builder.getTemplate().getSlides().get(1), this.builder.getPresentation().getSlides().get(0));
            this.browser.getEngine().reload();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}

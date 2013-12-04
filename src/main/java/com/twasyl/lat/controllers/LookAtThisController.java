package com.twasyl.lat.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LookAtThisController implements Initializable {

    private int numberOfSlides = 0;
    private List<String> slides = new ArrayList<>();

    private File templateFile;
    private File presentationFile = new File("./src/test/resources/template/result.html");

    @FXML
    private WebView browser;
    @FXML
    private javafx.scene.control.TextArea commands;

    @FXML private void loadTemplate(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        this.templateFile = chooser.showOpenDialog(null);

        try {
            loadPresentation();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
        if(this.presentationFile == null) throw new IllegalArgumentException("The presentation file can not be null");

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
        }
    }

    @FXML
    private void addSlide(ActionEvent event) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(out);
        Reader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./src/test/resources/template/slides/template/title.html"))));

            VelocityContext context = new VelocityContext();
            context.put("slideNumber", ++numberOfSlides);

            Velocity.evaluate(context, writer, "", reader);
            writer.flush();

            slides.add(new String(out.toByteArray()));

            loadPresentation();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                writer.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Velocity.init();
    }


    private void loadPresentation() throws IOException {
        if(this.templateFile == null) throw new IllegalArgumentException("The template file can not be null");
        if(!this.templateFile.exists()) throw new IllegalArgumentException("The template file does not exist");

        StringBuffer slidesHTML = new StringBuffer();

        for(String slideContent : slides) {
            slidesHTML.append(slideContent);
            slidesHTML.append("\n");
        }

        VelocityContext context = new VelocityContext();
        context.put("slides", slidesHTML.toString());

        Reader reader = new InputStreamReader(new FileInputStream(templateFile));
        Writer writer = new OutputStreamWriter(new FileOutputStream(presentationFile));

        Velocity.evaluate(context, writer, "", reader);

        writer.flush();
        writer.close();

        this.browser.getEngine().load(this.presentationFile.toURI().toASCIIString());
    }
}

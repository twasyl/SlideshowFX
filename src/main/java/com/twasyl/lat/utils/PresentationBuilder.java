package com.twasyl.lat.utils;

import java.io.File;

public class PresentationBuilder {

    private int numberOfSlides = 0;
    private File template;
    private File presentationFile;

    public PresentationBuilder() {
    }

    public PresentationBuilder(File template, File presentationFile) {
        this.template = template;
        this.presentationFile = presentationFile;
    }

    public File getTemplate() { return this.template; }
    public void setTemplate(File template) { this.template = template; }

    public File getPresentationFile() { return this.presentationFile; }
    public void setPresentationFile(File presentationFile) { this.presentationFile = presentationFile; }

    public void buildPresentation() {
        if(this.presentationFile == null) throw new IllegalArgumentException("The presentation file can not be null");
    }
}

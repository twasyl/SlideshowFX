package com.twasyl.lat.scene.controls.layout;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.AnchorPane;

public class Layout extends AnchorPane {

    private final StringProperty layoutName = new SimpleStringProperty();

    public StringProperty layoutNameProperty() { return this.layoutName; }
    public String getLayoutName() { return this.layoutNameProperty().get(); }
    public void setLayoutName(String layoutName) { this.layoutNameProperty().set(layoutName); }
}

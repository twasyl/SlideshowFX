package com.twasyl.lat.scene.controls.layout;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;

@DefaultProperty("elements")
public class Layout extends AnchorPane {

    private final StringProperty layoutName = new SimpleStringProperty();
    private final ListProperty<LayoutElement> elements = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<LayoutElement>()));

    public StringProperty layoutNameProperty() { return this.layoutName; }
    public String getLayoutName() { return this.layoutNameProperty().get(); }
    public void setLayoutName(String layoutName) { this.layoutNameProperty().set(layoutName); }

    public ListProperty<LayoutElement> elementsProperty() { return this.elements; }
    public ObservableList<LayoutElement> getElements() { return this.elementsProperty().get(); }
    public void setElements(ObservableList<LayoutElement> elements) { this.elementsProperty().set(elements); }
}

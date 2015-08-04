/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.engine.presentation.configuration.PresentationConfiguration;
import com.twasyl.slideshowfx.utils.beans.Pair;
import de.jensd.fx.glyphs.GlyphsStack;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Thierry Wasylczenko
 */
public class PresentationVariablesPanel extends BorderPane {

    private PresentationConfiguration configuration;
    private final Map<String, String> variables = new HashMap<>();

    private final VBox variablesBox = new VBox(5);
    private final ScrollPane variablesScrollPane = new ScrollPane(this.variablesBox);
    private final ToggleGroup variableGroup = new ToggleGroup();

    public PresentationVariablesPanel(final PresentationConfiguration configuration) {
        this.configuration = configuration;

        this.variablesScrollPane.setPrefSize(500, 500);

        final FontAwesomeIconView backgroundIcon = new FontAwesomeIconView(FontAwesomeIcon.PLUS_SQUARE);
        backgroundIcon.setGlyphSize(20);
        backgroundIcon.setGlyphStyle("-fx-fill: white");

        final FontAwesomeIconView plusIcon = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        plusIcon.setGlyphSize(15);
        plusIcon.setGlyphStyle("-fx-fill: app-color-orange");

        final GlyphsStack stack = new GlyphsStack();
        stack.add(backgroundIcon).add(plusIcon);

        final Button addButton = new Button();
        addButton.getStyleClass().add("image");
        addButton.setGraphic(stack);
        addButton.setTooltip(new Tooltip("Add a variable"));
        addButton.setOnAction(event -> this.addVariable(null, null));

        final ToolBar toolbar = new ToolBar();
        toolbar.getItems().add(addButton);

        this.setTop(toolbar);
        this.setCenter(this.variablesScrollPane);

        this.configuration.getVariables().forEach(variable -> this.addVariable(variable.getKey(), variable.getValue()));
    }

    /**
     * This methods add the graphical components to this panel that allows to define a new variable to the configuration.
     */
    private void addVariable(String name, String value) {
        final Pair<String, String> variable = new Pair<>();
        if(name != null) variable.setKey(name);
        if(value != null) variable.setValue(value);

        final RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(this.variableGroup);
        radioButton.setSelected(true);
        radioButton.setUserData(variable);

        final TextField variableName = new TextField();
        variableName.setPromptText("Variable's name");
        variableName.setPrefColumnCount(10);
        variableName.textProperty().bindBidirectional(variable.keyProperty());

        final TextField variableValue = new TextField();
        variableValue.setPromptText("Variable's value");
        variableValue.setPrefColumnCount(15);
        variableValue.textProperty().bindBidirectional(variable.valueProperty());

        final FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.TIMES_CIRCLE);
        icon.setGlyphSize(20);
        icon.setGlyphStyle("-fx-fill: app-color-orange");

        final Button delete = new Button();
        delete.setGraphic(icon);
        delete.setTooltip(new Tooltip("Delete this variable"));

        final HBox box = new HBox(5);
        box.getChildren().addAll(radioButton, variableName, variableValue, delete);

        delete.setOnAction(event -> {
            this.variablesBox.getChildren().remove(box);
            this.variableGroup.getToggles().remove(radioButton);
        });

        this.variablesBox.getChildren().add(box);
    }

    /**
     * Get the property that has been selected in this panel. If no selection has been made, {@code null}
     * is returned.
     * Moreover, if the name of the property is null or empty, {@code null} will also be returned.
     * @return The selected property.
     */
    public Pair<String, String> getSelectedVariable() {
        final RadioButton selection = (RadioButton) this.variableGroup.getSelectedToggle();

        if(selection != null) {
            final Pair<String, String> variable = (Pair<String, String>) selection.getUserData();

            if(variable.getKey() == null || variable.getKey().trim().isEmpty()) return null;
            else return variable;
        }
        else return null;
    }

    /**
     * Get the list of valid defined variables. A valid variable is when the name of this variable is
     * neither {@code null} nor empty.
     * The list is obtained by all pairs displayed in the component.
     *
     * @return The list of valid defined variables or an empty list if no variables are defined or valid.
     */
    public List<Pair<String, String>> getVariables() {
        final List<Pair<String, String>> variables = new ArrayList<>();

        variables.addAll(this.variableGroup.getToggles().stream()
                .map(toggle -> (Pair<String, String>) toggle.getUserData())
                .filter(data -> data.getKey() != null && !data.getKey().trim().isEmpty())
                .collect(Collectors.toList()));

        return variables;
    }
}

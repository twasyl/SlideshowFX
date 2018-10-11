package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.engine.presentation.configuration.PresentationConfiguration;
import com.twasyl.slideshowfx.icons.FontAwesome;
import com.twasyl.slideshowfx.icons.Icon;
import com.twasyl.slideshowfx.icons.IconStack;
import com.twasyl.slideshowfx.utils.DialogHelper;
import com.twasyl.slideshowfx.utils.beans.Pair;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a panel used to insert and define variables that can be used within a presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class PresentationVariablesPanel extends BorderPane {

    private PresentationConfiguration configuration;

    private final VBox variablesBox = new VBox(5);
    private final ScrollPane variablesScrollPane = new ScrollPane(this.variablesBox);
    private final ToggleGroup variableGroup = new ToggleGroup();

    public PresentationVariablesPanel(final PresentationConfiguration configuration) {
        this.configuration = configuration;

        this.variablesScrollPane.setPrefSize(500, 500);

        final FontAwesome backgroundIcon = new FontAwesome(Icon.PLUS_SQUARE);
        backgroundIcon.setIconSize(20d);
        backgroundIcon.setIconColor(Color.WHITE);

        final FontAwesome plusIcon = new FontAwesome(Icon.PLUS);
        plusIcon.setIconSize(15d);
        plusIcon.setIconColor(Paint.valueOf("app-color-orange"));

        final IconStack stack = new IconStack();
        stack.getChildren().addAll(backgroundIcon, plusIcon);

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
        if (name != null) variable.setKey(name);
        if (value != null) variable.setValue(value);

        final RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(this.variableGroup);
        radioButton.setSelected(true);
        radioButton.setUserData(variable);

        final EventHandler<KeyEvent> addVariableByKeyboard = event -> {
            if (event.isShortcutDown() && KeyCode.ENTER.equals(event.getCode())) {
                event.consume();
                this.addVariable(null, null);
            }
        };

        final TextField variableName = new TextField();
        variableName.setPromptText("Variable's name");
        variableName.setPrefColumnCount(10);
        variableName.textProperty().bindBidirectional(variable.keyProperty());
        variableName.setOnKeyPressed(addVariableByKeyboard);

        final TextField variableValue = new TextField();
        variableValue.setPromptText("Variable's value");
        variableValue.setPrefColumnCount(15);
        variableValue.textProperty().bindBidirectional(variable.valueProperty());
        variableValue.setOnKeyPressed(addVariableByKeyboard);

        final FontAwesome icon = new FontAwesome(Icon.TIMES_CIRCLE);
        icon.setIconSize(20d);
        icon.setIconColor(Paint.valueOf("app-color-orange"));

        final Button delete = new Button();
        delete.setGraphic(icon);
        delete.setTooltip(new Tooltip("Delete this variable"));

        final HBox box = new HBox(5);
        box.getChildren().addAll(radioButton, variableName, variableValue, delete);

        delete.setOnAction(event -> {
            final ButtonType answer = DialogHelper.showConfirmationAlert("Delete variable", "Are you sure you want to delete the \"" + variableName.getText() + "\" variable ?");

            if (answer == ButtonType.YES) {
                this.variablesBox.getChildren().remove(box);
                this.variableGroup.getToggles().remove(radioButton);
            }
        });

        this.variablesBox.getChildren().add(box);
    }

    /**
     * Get the property that has been selected in this panel. If no selection has been made, {@code null}
     * is returned.
     * Moreover, if the name of the property is null or empty, {@code null} will also be returned.
     *
     * @return The selected property.
     */
    public Pair<String, String> getSelectedVariable() {
        final RadioButton selection = (RadioButton) this.variableGroup.getSelectedToggle();

        if (selection != null) {
            final Pair<String, String> variable = (Pair<String, String>) selection.getUserData();

            if (variable.getKey() == null || variable.getKey().trim().isEmpty()) return null;
            else {
                this.normalizeVariable(variable);
                return variable;
            }
        } else return null;
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

        variables.forEach(this::normalizeVariable);

        return variables;
    }

    /**
     * Normalize a variable, meaning that if it's value is {@code null} set it to an empty {@link String}.
     *
     * @param variable The variable to normalize.
     */
    private void normalizeVariable(final Pair<String, String> variable) {
        if (variable.getValue() == null) {
            variable.setValue("");
        }
    }
}

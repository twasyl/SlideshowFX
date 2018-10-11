package com.twasyl.slideshowfx.ui.controls;

import com.twasyl.slideshowfx.ui.controls.validators.IValidator;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.beans.property.*;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Set;

/**
 * A control displaying the label above a text field and under certain circumstances. <br />
 * If the text field is empty, only the prompt text is displayed. <br />
 * If the text field is focused, the label is displayed above the text field and has the {@code focused} pseudo class
 * state. </br />
 * If the text field is filled, the label is still displayed above the text field, without the pseudo class state
 * {@code focused}.
 * <p>
 * The control also allows to set {@link IValidator} on it in order to validate the value of the text field. If the
 * value of the text field is invalid, the text field will have the {@code error} pseudo class state.
 * <p>
 * The control allows to set the text field as mandatory. If the text field looses the focus and is empty, the
 * {@code error} pseudo class state is set on the text field.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.3
 */
public class ExtendedTextField extends VBox {
    private static final PseudoClass FOCUSED = PseudoClass.getPseudoClass("focused");
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private Label uiLabel = new Label();
    private TextField textField = new TextField();
    private FadeTransition uiLabelAnimation = new FadeTransition(Duration.millis(100), this.uiLabel);

    private StringProperty label = new SimpleStringProperty();
    private BooleanProperty mandatory = new SimpleBooleanProperty(false);
    private ReadOnlyBooleanProperty valid = new SimpleBooleanProperty();

    private IValidator<String> validator;

    public ExtendedTextField() {
        super(0);

        initializeUILabel();
        initializeUITextField();

        this.getStyleClass().add("extended-text-field");

        this.initializeFocusEvents();
        this.getChildren().addAll(this.uiLabel, this.textField);
    }

    public ExtendedTextField(final String label) {
        this(label, false);
    }

    public ExtendedTextField(final String label, final boolean mandatory) {
        this();
        this.label.set(label);
        this.setMandatory(mandatory);
    }

    public ExtendedTextField(final String label, final boolean mandatory, final int prefColumnCount) {
        this(label, mandatory);
        this.setPrefColumnCount(prefColumnCount);
    }

    private void initializeUILabel() {
        this.uiLabel.textProperty().bind(this.label);
        this.uiLabel.setOpacity(0);
    }

    protected void initializeUITextField() {
        this.textField.promptTextProperty().bind(this.label);

        this.textField.textProperty().addListener((textValue, oldText, newText) -> {
            Boolean validValue = null;
            if (this.validator != null) {
                validValue = this.isValid();
            }
            ((SimpleBooleanProperty) this.valid).setValue(validValue);

            if (newText != null && !newText.isEmpty()) {
                this.uiLabel.setOpacity(1);
            }
        });
    }

    private void initializeFocusEvents() {
        this.textField.focusedProperty().addListener((focusedValue, oldFocus, newFocus) -> {
            if (newFocus || (!newFocus && isTextEmpty())) {
                this.animateLabel(newFocus);

                if (!newFocus) {
                    this.textField.pseudoClassStateChanged(ERROR, isMandatory());
                    this.showPromptText();
                } else {
                    this.textField.pseudoClassStateChanged(ERROR, false);
                    this.hidePromptText();
                }
            } else if (!newFocus && !isTextEmpty()) {
                if (this.isMandatory() && this.validator != null && !isValid()) {
                    this.textField.pseudoClassStateChanged(ERROR, true);
                }

                this.uiLabel.pseudoClassStateChanged(FOCUSED, newFocus);
            }
        });
    }

    /**
     * Indicates if the text is {@code null} or empty.
     *
     * @return {@code true} if the text is {@code null} or empty, {@code false} otherwise.
     */
    private boolean isTextEmpty() {
        return this.textField.getText() == null || this.textField.getText().isEmpty();
    }

    /**
     * Animate the label above the text field. The label may be displayed or hidden.
     *
     * @param show {@code true} to display the label, {@code false} to hide it.
     */
    private void animateLabel(boolean show) {
        if (this.uiLabelAnimation.getStatus() != Animation.Status.STOPPED) {
            this.uiLabelAnimation.pause();
        }

        if (show) {
            this.uiLabel.pseudoClassStateChanged(FOCUSED, show);
            this.uiLabelAnimation.setToValue(1);
        } else {
            this.uiLabelAnimation.setToValue(0);
        }

        this.uiLabelAnimation.setOnFinished(event -> {
            this.uiLabel.pseudoClassStateChanged(FOCUSED, show);
        });

        this.uiLabelAnimation.playFromStart();
    }

    /**
     * Hide the prompt text of the underlying text field.
     */
    private void hidePromptText() {
        final Node promptText = getPromptText();

        if (promptText != null) {
            promptText.setVisible(false);
        }
    }

    /**
     * Show the prompt text of the underlying text field.
     */
    private void showPromptText() {
        final Node promptText = getPromptText();

        if (promptText != null) {
            promptText.setVisible(true);
        }
    }

    /**
     * Try to get the prompt text {@link Node} of the underlying text field. If it is found, then the
     * {@link TextField#visibleProperty()} is unbind in order to modify it.
     *
     * @return The prompt text {@link Node} if it is found.
     */
    private Node getPromptText() {
        final Set<Node> nodes = this.textField.lookupAll(".text");

        final Node promptText = nodes.stream()
                .filter(node -> node instanceof Text && this.label.get().equals(((Text) node).getText()))
                .findFirst()
                .orElse(null);

        if (promptText != null && promptText.visibleProperty().isBound()) {
            promptText.visibleProperty().unbind();
        }

        return promptText;
    }

    public IntegerProperty prefColumnCountProperty() {
        return textField.prefColumnCountProperty();
    }

    public int getPrefColumnCount() {
        return textField.getPrefColumnCount();
    }

    public void setPrefColumnCount(int value) {
        textField.setPrefColumnCount(value);
    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String value) {
        textField.setText(value);
    }

    public StringProperty textProperty() {
        return textField.textProperty();
    }

    public StringProperty labelProperty() {
        return label;
    }

    public String getLabel() {
        return label.get();
    }

    public void setLabel(String label) {
        this.label.set(label);
    }

    public boolean isMandatory() {
        return mandatory.get();
    }

    public BooleanProperty mandatoryProperty() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory.set(mandatory);
    }

    public IValidator<String> getValidator() {
        return validator;
    }

    public void setValidator(IValidator<String> validator) {
        this.validator = validator;
    }

    public boolean isValid() {
        if (this.getValidator() == null) {
            throw new IllegalArgumentException("No validator defined for the control");
        }

        final boolean valid = this.validator.isValid(this.textField.getText());
        if (!valid) {
            this.textField.pseudoClassStateChanged(ERROR, true);
        }

        return valid;
    }

    public ReadOnlyBooleanProperty validProperty() {
        return this.valid;
    }
}

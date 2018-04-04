package com.twasyl.slideshowfx.setup.controllers;

import com.twasyl.slideshowfx.setup.enums.SetupStatus;
import com.twasyl.slideshowfx.setup.exceptions.SetupStepException;
import com.twasyl.slideshowfx.setup.step.ISetupStep;
import com.twasyl.slideshowfx.utils.DialogHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;


/**
 * Controller for the {SetupView.xml} file.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class SetupViewController implements Initializable {

    @FunctionalInterface
    static interface Operation {
        void execute() throws SetupStepException;
    }

    private static final Logger LOGGER = Logger.getLogger(SetupViewController.class.getName());

    @FXML private ObservableList<Button> buttonBarActions;
    @FXML private BorderPane root;
    @FXML private ScrollPane content;
    @FXML private VBox stepsContainer;
    @FXML private HBox buttonBar;
    @FXML private Button cancel;
    @FXML private Button previous;
    @FXML private Button next;
    @FXML private Button finish;

    private ObservableList<ISetupStep> steps = FXCollections.observableArrayList();
    private ObjectProperty<ISetupStep> currentStep = new SimpleObjectProperty<>();
    private SetupStatus setupStatus = SetupStatus.IN_PROGRESS;

    @FXML
    private void nextStep(final ActionEvent event) throws Exception {
        this.executeSafely(() -> {
            if(currentStep.get().next() != null) {
                this.currentStep.get().execute();
                this.currentStep.set(this.currentStep.get().next());
            }
        });
    }

    @FXML
    private void previousStep(final ActionEvent event) throws Exception {
        this.executeSafely(() -> {
            if(this.currentStep.get().previous() != null) {
                this.currentStep.get().rollback();
                this.currentStep.set(this.currentStep.get().previous());
            }
        });
    }

    @FXML
    private void cancelSetup(final ActionEvent event) throws Exception {
        this.cancelSetup();
        if(this.setupStatus == SetupStatus.ABORTED) {
            this.requestClose();
        }
    }

    @FXML
    private void finishSetup(final ActionEvent event) throws Exception {
        this.finishSetup();
        this.requestClose();
    }

    protected void executeSafely(final Operation operation) {
        try {
            operation.execute();
        } catch(SetupStepException ex) {
            LOGGER.log(Level.SEVERE, "Error when executing the operation", ex);
            DialogHelper.showError("Error", ex.getMessage());
        } catch(Exception ex) {
            LOGGER.log(Level.SEVERE, "Unmanaged error when executing the operation", ex);
            DialogHelper.showError("Unmanaged error", ex.getMessage());
        }
    }

    public SetupStatus getSetupStatus() { return this.setupStatus; }

    public void cancelSetup() {
        final ButtonType answer = DialogHelper.showConfirmationAlert("Cancelling the installation", "Are you sure you want to cancel the installation?");

        if(answer == ButtonType.YES) {
            this.rollbackAllSteps();
            this.setupStatus = SetupStatus.ABORTED;
        }
    }

    public void finishSetup() {
        this.executeSafely(() -> {
            this.currentStep.get().execute();
            this.setupStatus = SetupStatus.SUCCESSFUL;
        });
    }

    public void rollbackAllSteps() {
        this.executeSafely(() -> {
            ISetupStep stepToRollback = this.currentStep.get().previous();

            while(stepToRollback != null) {
                stepToRollback.rollback();
                stepToRollback = stepToRollback.previous();
            }
        });
    }

    public void requestClose() {
        final WindowEvent closeEvent = new WindowEvent(this.root.getScene().getWindow(), WINDOW_CLOSE_REQUEST);
        this.root.getScene().getWindow().fireEvent(closeEvent);
    }

    public SetupViewController addStep(final ISetupStep step) {
        if(step != null) {
            if(!steps.isEmpty()) {
                final ISetupStep lastStep = steps.get(steps.size() - 1);
                lastStep.next(step);
                step.previous(lastStep);
            } else {
                this.currentStep.set(step);
            }

            this.steps.add(step);
        }

        return this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.root.sceneProperty().addListener((sceneValue, oldScene, newScene) -> {
            newScene.windowProperty().addListener((windowValue, oldWindow, newWindow) -> {
                if(newWindow != null && newWindow instanceof Stage) {
                    ((Stage) newWindow).setTitle(this.currentStep.get().title());
                    manageCurrentStepText();
                }
            });
        });

        this.steps.addListener((ListChangeListener) change -> {
            this.stepsContainer.getChildren().clear();

            this.steps.forEach(step -> {
                final Text text = new Text(step.title());
                text.setWrappingWidth(256);
                this.stepsContainer.getChildren().add(text);
            });
        });

        this.currentStep.addListener((value, oldStep, newStep) -> {
            if(newStep != null) {
                final Node view = newStep.getView();
                view.maxWidth(800);

                this.content.setContent(view);

                if(this.root.getScene() != null) {
                    ((Stage) this.root.getScene().getWindow()).setTitle(newStep.title());
                }

                manageNextButtonState(newStep);
                managePreviousButtonState(newStep);
                manageFinishButtonState(newStep);
                manageButtonBar();
                manageCurrentStepText();
            }
        });

        this.buttonBarActions.forEach(button -> {
            button.visibleProperty().addListener((value, oldVisible, newVisible) -> {
                this.manageButtonBar();
            });
        });
    }

    protected void manageNextButtonState(final ISetupStep withStep) {
        if(this.next.visibleProperty().isBound()) {
            this.next.visibleProperty().unbind();
        }

        if(this.next.disableProperty().isBound()) {
            this.next.disableProperty().unbind();
        }

        this.next.visibleProperty().bind(withStep.nextProperty().isNotNull());
        this.next.disableProperty().bind(withStep.validProperty().not());
    }

    protected void managePreviousButtonState(final ISetupStep withStep) {
        if(this.previous.visibleProperty().isBound()) {
            this.previous.visibleProperty().unbind();
        }

        this.previous.visibleProperty().bind(withStep.previousProperty().isNotNull());
    }

    protected void manageFinishButtonState(final ISetupStep withStep) {
        if(this.finish.visibleProperty().isBound()) {
            this.finish.visibleProperty().unbind();
        }

        if(this.finish.disableProperty().isBound()) {
            this.finish.disableProperty().unbind();
        }

        this.finish.visibleProperty().bind(withStep.nextProperty().isNull());
        this.finish.disableProperty().bind(withStep.validProperty().not());
    }

    protected void manageButtonBar() {
        this.buttonBar.getChildren().clear();

        this.buttonBarActions.forEach(button -> {
            if(button.isVisible())this.buttonBar.getChildren().add(button);
        });
    }

    protected void manageCurrentStepText() {
        this.stepsContainer.getChildren()
                .stream()
                .filter(child -> child instanceof Text)
                .map(child -> (Text) child)
                .forEach(text -> {
                    final Font currentFont = text.getFont();
                    FontWeight weight = FontWeight.NORMAL;

                    if(text.getText().equals(this.currentStep.get().title())) {
                        weight = FontWeight.BOLD;
                    }

                    text.setFont(Font.font(currentFont.getFamily(), weight, currentFont.getSize()));
                });
    }
}

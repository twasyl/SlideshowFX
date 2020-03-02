package com.twasyl.slideshowfx.setup.step;

import javafx.beans.property.*;
import javafx.fxml.Initializable;
import javafx.scene.Node;

/**
 * Abstract implementation of a {@link ISetupStep}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.1-SNAPSHOT
 */
public abstract class AbstractSetupStep<I extends Initializable> implements ISetupStep {
    protected Node view;
    protected I controller;

    private final StringProperty title = new SimpleStringProperty();
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final ObjectProperty<ISetupStep> previous = new SimpleObjectProperty<>(null);
    private final ObjectProperty<ISetupStep> next = new SimpleObjectProperty<>(null);

    @Override
    public StringProperty titleProperty() {
        return this.title;
    }

    @Override
    public String title() {
        return this.titleProperty().get();
    }

    @Override
    public <T extends ISetupStep> T title(String title) {
        this.titleProperty().set(title);
        return (T) this;
    }

    @Override
    public ObjectProperty<ISetupStep> previousProperty() {
        return this.previous;
    }

    @Override
    public ISetupStep previous() {
        return this.previous.get();
    }

    @Override
    public <T extends ISetupStep> T previous(ISetupStep step) {
        this.previous.set(step);
        return (T) this;
    }

    @Override
    public ObjectProperty<ISetupStep> nextProperty() {
        return this.next;
    }

    @Override
    public ISetupStep next() {
        return this.next.get();
    }

    @Override
    public <T extends ISetupStep> T next(ISetupStep step) {
        this.next.set(step);
        return (T) this;
    }

    @Override
    public <T extends ISetupStep> T find(Class<T> stepType) {
        T result = null;
        ISetupStep previousStep = previous();

        while (result == null && previousStep != null) {
            if (stepType.isInstance(previousStep)) {
                result = stepType.cast(previousStep);
            } else {
                previousStep = previousStep.previous();
            }
        }

        return result;
    }

    @Override
    public BooleanProperty validProperty() {
        return this.valid;
    }

    @Override
    public boolean isValid() {
        return this.valid.get();
    }

    @Override
    public <T extends ISetupStep> T setValid(boolean valid) {
        this.valid.set(valid);
        return (T) this;
    }

    @Override
    public Node getView() {
        return this.view;
    }
}

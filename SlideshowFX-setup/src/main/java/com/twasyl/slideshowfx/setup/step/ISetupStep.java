package com.twasyl.slideshowfx.setup.step;

import com.twasyl.slideshowfx.setup.exceptions.SetupStepException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

/**
 * Represents a setup step.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public interface ISetupStep {

    /**
     * Get the property representing the title of the step.
     * @return The title property for this step.
     */
    StringProperty titleProperty();

    /**
     * Get the title of this step.
     * @return The title of this step.
     */
    String title();

    /**
     * Defines the title for this step.
     * @param title The title of the step.
     * @param <T> The type of this step.
     * @return This step.
     */
    <T extends ISetupStep> T title(final String title);

    /**
     * Get the previous step of this one.
     * @return The previous step or {@code null} if none.
     */
    ObjectProperty<ISetupStep> previousProperty();

    /**
     * Get the previous step of this one.
     * @return The previous step or {@code null} if none.
     */
    ISetupStep previous();

    /**
     * Set the previous step of this one.
     * @param step The previous step.
     * @param <T> The type of this step.
     * @return This step.
     */
    <T extends ISetupStep> T previous(final ISetupStep step);

    /**
     * Get the next step of this one.
     * @return The next step or {@code null} if none.
     */
    ObjectProperty<ISetupStep> nextProperty();

    /**
     * Get the next step of this one.
     * @return The next step or {@code null} if none.
     */
    ISetupStep next();

    /**
     * Set the next step of this one.
     * @param step The next step.
     * @param <T> The type of this step.
     * @return This step.
     */
    <T extends ISetupStep> T next(final ISetupStep step);

    /**
     * Indicates if all information provided inside the step are valid.
     * @return The {@link BooleanProperty} indicating if the step is valid.
     */
    BooleanProperty validProperty();

    /**
     * Indicates if all information provided inside the step are valid.
     * @return {@code true} if all information provided inside the step are valid, {@code false} otherwise.
     */
    boolean isValid();

    /**
     * Defines if all information provided inside the step are valid.
     * @param valid {@code true} if everything is valid, {@code false} otherwise.
     * @param <T> The type of step
     * @return This instance of the step.
     */
    <T extends ISetupStep> T setValid(final boolean valid);

    /**
     * Get the view of this step. The view contains all elements the user should fill.
     * @return The step view.
     */
    Node getView();

    /**
     * Execute the step by performing all operations this step defines (for instance, creating a file, copy a dir).
     * @throws SetupStepException If something went wrong.
     */
    void execute() throws SetupStepException;

    /**
     * Rollback all operations performed during this step.
     * @throws SetupStepException If something went wrong.
     */
    void rollback() throws SetupStepException;
}

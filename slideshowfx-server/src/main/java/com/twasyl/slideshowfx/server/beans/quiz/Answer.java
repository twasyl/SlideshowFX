package com.twasyl.slideshowfx.server.beans.quiz;

import javafx.beans.property.*;

/**
 * This class is a possible answer to a {@link Quiz}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class Answer {

    private LongProperty id = new SimpleLongProperty();
    private final StringProperty text = new SimpleStringProperty();
    private final BooleanProperty correct = new SimpleBooleanProperty(false);
    private final ObjectProperty<Quiz> quiz = new SimpleObjectProperty<>();

    /**
     * The property for the ID of this answer.
     * @return The property for the ID of this answer.
     */
    public LongProperty idProperty() { return id; }

    /**
     * The ID of this answer.
     * @return The ID of this answer.
     */
    public long getId() { return id.get(); }

    /**
     * Set the ID for this answer.
     * @param id The new ID for this answer.
     */
    public void setId(long id) { this.id.set(id); }

    /**
     * The text of this answer.
     * @return The property for the text of this answer.
     */
    public StringProperty textProperty() { return text; }

    /**
     * Get the text of this answer.
     * @return the text of this answer.
     */
    public String getText() { return text.get(); }

    /**
     * Set the text of this answer.
     * @param text The new text of this answer.
     */
    public void setText(String text) { this.text.set(text); }

    /**
     * The property indicating if this answer is correct for its quiz.
     * @return The property indicating if this answer is correct for its quiz.
     */
    public BooleanProperty correctProperty() {
        return correct;
    }

    /**
     * Indicates if this answer is a correct answer for its quiz.
     * @return {@code true} if this answer is correct for the quiz, {@code false} otherwise.
     */
    public boolean isCorrect() {
        return correct.get();
    }

    /**
     * Set if this answer is correct for its quiz.
     * @param correct {@code true} is the answer is correct, {@code false} otherwise.
     */
    public void setCorrect(boolean correct) {
        this.correct.set(correct);
    }

    /**
     * The property containing the quiz this answer is associated to.
     * @return The property containing the quiz this answer is associated to.
     */
    public ObjectProperty<Quiz> quizProperty() {
        return quiz;
    }

    /**
     * The quiz this answer is associated to.
     * @return The quiz this answer is associated to.
     */
    public Quiz getQuiz() {
        return quiz.get();
    }

    /**
     * Set the quiz this answer should be associated to.
     * @param quiz The quiz this answer is related to.
     */
    public void setQuiz(Quiz quiz) {
        this.quiz.set(quiz);
    }
}

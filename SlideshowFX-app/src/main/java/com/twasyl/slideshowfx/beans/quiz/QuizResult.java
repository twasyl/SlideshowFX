/*
 * Copyright 2016 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.beans.quiz;

import javafx.beans.property.*;

/**
 * This class represents the result of a given quiz.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class QuizResult {

    private final ObjectProperty<Quiz> quiz = new SimpleObjectProperty<>();
    private final ReadOnlyIntegerProperty totalAnswers = new SimpleIntegerProperty(0);
    private final IntegerProperty correctAnswers = new SimpleIntegerProperty(0);
    private final IntegerProperty wrongAnswers = new SimpleIntegerProperty(0);

    public QuizResult() {
        ((SimpleIntegerProperty) this.totalAnswers).bind(this.correctAnswers.add(this.wrongAnswers));
    }

    /**
     * Get the number of total answers get for the quiz. It correspond to the sum of correct and wrong answers.
     * @return The total number of answers get for the quiz.
     */
    public int getTotalAnswers() { return totalAnswers.get(); }

    /**
     * Get the number of total answers get for the quiz. It correspond to the sum of correct and wrong answers. This
     * property is bind to the {@link #correctAnswersProperty()} and {@link #wrongAnswersProperty()}
     * @return The total number of answers get for the quiz.
     */
    public ReadOnlyIntegerProperty totalAnswersProperty() { return totalAnswers; }

    /**
     * Get the number of correct answers to the quiz.
     * @return The number of correct answers to the quiz.
     */
    public IntegerProperty correctAnswersProperty() { return correctAnswers; }

    /**
     * Get the number of correct answers to the quiz.
     * @return The number of correct answers to the quiz.
     */
    public int getCorrectAnswers() { return correctAnswers.get(); }

    /**
     * Set the number of correct answers to the quiz.
     * @param correctAnswers The number of correct answers to the quiz.
     */
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers.set(correctAnswers); }

    /**
     * Get the number of wrong answers to the quiz.
     * @return The number of wrong answers to the quiz.
     */
    public int getWrongAnswers() { return wrongAnswers.get(); }

    /**
     * Get the number of wrong answers to the quiz.
     * @return The number of wrong answers to the quiz.
     */
    public IntegerProperty wrongAnswersProperty() { return wrongAnswers; }

    /**
     * Set the number of wrong answers to the quiz.
     * @param wrongAnswers The number of wrong answers to the quiz.
     */
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers.set(wrongAnswers); }

    /**
     * The property containing the quiz this result is associated to.
     * @return The property containing the quiz this result is associated to.
     */
    public ObjectProperty<Quiz> quizProperty() {
        return quiz;
    }

    /**
     * The quiz this result is associated to.
     * @return The quiz this result is associated to.
     */
    public Quiz getQuiz() {
        return quiz.get();
    }

    /**
     * Set the quiz this result should be associated to.
     * @param quiz The quiz this result is related to.
     */
    public void setQuiz(Quiz quiz) {
        this.quiz.set(quiz);
    }

    /**
     * Add one correct answer to the number of correct answers.
     */
    public synchronized void addCorrectAnswer() {
        this.setCorrectAnswers(this.getCorrectAnswers() + 1);
    }

    /**
     * Add one wrong answer to the number of wrong answers.
     */
    public synchronized  void addWrongAnswer() {
        this.setWrongAnswers(this.getWrongAnswers() + 1);
    }
}

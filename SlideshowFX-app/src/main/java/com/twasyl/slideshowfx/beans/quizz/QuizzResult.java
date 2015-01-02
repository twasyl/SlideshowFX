/*
 * Copyright 2014 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.beans.quizz;

import javafx.beans.property.*;

/**
 * This class represents the result of a given quizz.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class QuizzResult {

    private final ObjectProperty<Quizz> quizz = new SimpleObjectProperty<>();
    private final ReadOnlyIntegerProperty totalAnswers = new SimpleIntegerProperty(0);
    private final IntegerProperty correctAnswers = new SimpleIntegerProperty(0);
    private final IntegerProperty wrongAnswers = new SimpleIntegerProperty(0);

    public QuizzResult() {
        ((SimpleIntegerProperty) this.totalAnswers).bind(this.correctAnswers.add(this.wrongAnswers));
    }

    /**
     * Get the number of total answers get for the quizz. It correspond to the sum of correct and wrong answers.
     * @return The total number of answers get for the quizz.
     */
    public int getTotalAnswers() { return totalAnswers.get(); }

    /**
     * Get the number of total answers get for the quizz. It correspond to the sum of correct and wrong answers. This
     * property is bind to the {@link #correctAnswersProperty()} and {@link #wrongAnswersProperty()}
     * @return The total number of answers get for the quizz.
     */
    public ReadOnlyIntegerProperty totalAnswersProperty() { return totalAnswers; }

    /**
     * Get the number of correct answers to the quizz.
     * @return The number of correct answers to the quizz.
     */
    public IntegerProperty correctAnswersProperty() { return correctAnswers; }

    /**
     * Get the number of correct answers to the quizz.
     * @return The number of correct answers to the quizz.
     */
    public int getCorrectAnswers() { return correctAnswers.get(); }

    /**
     * Set the number of correct answers to the quizz.
     * @param correctAnswers The number of correct answers to the quizz.
     */
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers.set(correctAnswers); }

    /**
     * Get the number of wrong answers to the quizz.
     * @return The number of wrong answers to the quizz.
     */
    public int getWrongAnswers() { return wrongAnswers.get(); }

    /**
     * Get the number of wrong answers to the quizz.
     * @return The number of wrong answers to the quizz.
     */
    public IntegerProperty wrongAnswersProperty() { return wrongAnswers; }

    /**
     * Set the number of wrong answers to the quizz.
     * @param wrongAnswers The number of wrong answers to the quizz.
     */
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers.set(wrongAnswers); }

    /**
     * The property containing the quizz this result is associated to.
     * @return The property containing the quizz this result is associated to.
     */
    public ObjectProperty<Quizz> quizzProperty() {
        return quizz;
    }

    /**
     * The quizz this result is associated to.
     * @return The quizz this result is associated to.
     */
    public Quizz getQuizz() {
        return quizz.get();
    }

    /**
     * Set the quizz this result should be associated to.
     * @param quizz The quizz this result is related to.
     */
    public void setQuizz(Quizz quizz) {
        this.quizz.set(quizz);
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

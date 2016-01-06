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

package com.twasyl.slideshowfx.beans.quizz;

import javafx.beans.property.*;

/**
 * This class is a possible answer to a {@link com.twasyl.slideshowfx.beans.quizz.Quizz}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class Answer {

    private LongProperty id = new SimpleLongProperty();
    private final StringProperty text = new SimpleStringProperty();
    private final BooleanProperty correct = new SimpleBooleanProperty(false);
    private final ObjectProperty<Quizz> quizz = new SimpleObjectProperty<>();

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
     * The property indicating if this answer is correct for its quizz.
     * @return The property indicating if this answer is correct for its quizz.
     */
    public BooleanProperty correctProperty() {
        return correct;
    }

    /**
     * Indicates if this answer is a correct answer for its quizz.
     * @return <code>true</code> if this answer is correct for the quizz, <code>false</code> otherwise.
     */
    public boolean isCorrect() {
        return correct.get();
    }

    /**
     * Set if this answer is correct for its quizz.
     * @param correct <code>true</code> is the answer is correct, <code>false</code> otherwise.
     */
    public void setCorrect(boolean correct) {
        this.correct.set(correct);
    }

    /**
     * The property containing the quizz this answer is associated to.
     * @return The property containing the quizz this answer is associated to.
     */
    public ObjectProperty<Quizz> quizzProperty() {
        return quizz;
    }

    /**
     * The quizz this answer is associated to.
     * @return The quizz this answer is associated to.
     */
    public Quizz getQuizz() {
        return quizz.get();
    }

    /**
     * Set the quizz this answer should be associated to.
     * @param quizz The quizz this answer is related to.
     */
    public void setQuizz(Quizz quizz) {
        this.quizz.set(quizz);
    }
}

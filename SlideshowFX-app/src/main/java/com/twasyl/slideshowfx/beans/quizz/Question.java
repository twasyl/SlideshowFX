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
 * This class represents a question of a quizz.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class Question {

    private LongProperty id = new SimpleLongProperty();
    private final StringProperty text = new SimpleStringProperty();
    private final ObjectProperty<Quizz> quizz = new SimpleObjectProperty<>();

    /**
     * The property for the ID of this question.
     * @return The property for the ID of this question.
     */
    public LongProperty idProperty() { return id; }

    /**
     * The ID of this question.
     * @return The ID of this question.
     */
    public long getId() { return id.get(); }

    /**
     * Set the ID for this question.
     * @param id The new ID for this question.
     */
    public void setId(long id) { this.id.set(id); }

    /**
     * The text of this question.
     * @return The property for the text of this question.
     */
    public StringProperty textProperty() { return text; }

    /**
     * Get the text of this question.
     * @return the text of this question.
     */
    public String getText() { return text.get(); }

    /**
     * Set the text of this question.
     * @param text The new text of this question.
     */
    public void setText(String text) { this.text.set(text); }

    /**
     * The property containing the quizz this question is associated to.
     * @return The property containing the quizz this question is associated to.
     */
    public ObjectProperty<Quizz> quizzProperty() {
        return quizz;
    }

    /**
     * The quizz this question is associated to.
     * @return The quizz this question is associated to.
     */
    public Quizz getQuizz() {
        return quizz.get();
    }

    /**
     * Set the quizz this question should be associated to.
     * @param quizz The quizz this question is related to.
     */
    public void setQuizz(Quizz quizz) {
        this.quizz.set(quizz);
    }
}

/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.beans.quizz;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Arrays;
import java.util.List;

/**
 * This class represents a Quizz that can be performed during a presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class Quizz {

    private LongProperty id = new SimpleLongProperty();
    private ObjectProperty<Question> question = new SimpleObjectProperty<>();
    private ListProperty<Answer> answers = new SimpleListProperty<>(FXCollections.observableArrayList());

    /**
     * The property for the ID of this quizz.
     * @return The property for the ID of this quizz.
     */
    public LongProperty idProperty() { return id; }

    /**
     * The ID of this quizz.
     * @return The ID of this quizz.
     */
    public long getId() { return id.get(); }

    /**
     * Set the ID for this quizz.
     * @param id The new ID for this quizz.
     */
    public void setId(long id) { this.id.set(id); }

    /**
     * Get the question of this quizz.
     * @return The question of this quizz.
     */
    public Question getQuestion() { return question.get(); }

    /**
     * The question of this quizz.
     * @return The property associated to this quizz.
     */
    public ObjectProperty<Question> questionProperty() { return question; }

    /**
     * Set the question of this quizz.
     * @param question The question of this quizz.
     */
    public void setQuestion(Question question) {
        this.question.set(question);
    }

    /**
     * The property containing the possible answers of this quizz.
     * @return The property containing the possible answers for this quizz.
     */
    public ListProperty<Answer> answersProperty() { return answers; }

    /**
     * Get the possible answers for this quizz.
     * @return The list of possible answers.
     */
    public ObservableList<Answer> getAnswers() { return answers.get(); }

    /**
     * Set the possible answers for this quizz.
     * @param answers The list of possible answers for this quizz.
     */
    public void setAnswers(ObservableList<Answer> answers) { this.answers.set(answers); }

    /**
     * Get all correct answers for this quizz.
     * @return The list containing only the correct answers for this quizz.
     */
    public FilteredList<Answer> getCorrectAnswers() { return this.getAnswers().filtered(answer -> answer.isCorrect()); }

    /**
     * Performs a check to know if the quizz is answered correctly. This method checks if all correct answers contained
     * in this quizz has its ID referenced in the given <code>answersIDs</code> collection.
     *
     * @param answersIDs All answers' ID the user has checked.
     * @return <code>true</code> if all correct answers of this quizz has its ID referenced in <code>answersIDs</code>,
     * false otherwise
     */
    public boolean checkAnswers(Long ... answersIDs) {
        boolean correct = answersIDs != null && answersIDs.length > 0;

        // Avoid unnecessary treatment and variable assignment
        if(correct) {
            final List<Answer> correctAnswers = this.getAnswers().filtered(answer -> answer.isCorrect());

            /* If the size of the list of expected answers are not the same that the size of the given ansewers,
             * it is unnecessary to continue.
              */
            if(correct = correctAnswers.size() == answersIDs.length) {
                final List<Long> answersIDsList = Arrays.asList(answersIDs);

                int index = 0;
                // The "real" number of correct answers received.
                int numberOfCorrectAnswersReceived = 0;

                /*
                 * Loop on every correct answer while everything is correct
                 */
                while (index < correctAnswers.size() && correct) {
                    correct = answersIDsList.contains(correctAnswers.get(index).getId());

                    if(correct) numberOfCorrectAnswersReceived++;

                    index++;
                }

                correct = numberOfCorrectAnswersReceived == correctAnswers.size();
            }
        }

        return correct;
    }

    /**
     * Convert this Quizz object into a JSON object.
     * @return The JSON object representing this quizz
     */
    public JsonObject toJSON() {
        final JsonObject object = new JsonObject();
        object.putNumber("id", this.getId());
        object.putNumber("correctAnswers", this.getCorrectAnswers().size());

        if(this.getQuestion() != null) {
            final JsonObject questionObject = new JsonObject();
            questionObject.putNumber("id", this.getQuestion().getId());
            questionObject.putString("text", this.getQuestion().getText());

            object.putObject("question", questionObject);
        }

        if(!this.getAnswers().isEmpty()) {
            final JsonArray answersArray = new JsonArray();

            for(Answer answer : this.getAnswers()) {
                answersArray.addObject(new JsonObject()
                                            .putNumber("id", answer.getId())
                                            .putString("text", answer.getText())
                                            .putBoolean("correct", answer.isCorrect()));
            }

            object.putArray("answers", answersArray);
        }

        return object;
    }

    /**
     * Build a Quizz object from the given JSON string.
     * @param json The JSON string representing the quizz.
     * @return The created Quizz object.
     * @throws java.lang.NullPointerException if <code>json</code> is null.
     * @throws java.lang.IllegalArgumentException if <code>json</code> is empty.
     */
    public static Quizz build(final String json) {
        if(json == null) throw new NullPointerException("The JSON string can not be null");
        if(json.trim().isEmpty()) throw new IllegalArgumentException("The JSON string is empty");

        final JsonObject object = new JsonObject(json);
        final Quizz quizz = new Quizz();

        quizz.setId(object.getNumber("id").longValue());

        final JsonObject questionJson = object.getObject("question");
        if(questionJson != null) {
            final Question question = new Question();
            question.setQuizz(quizz);
            question.setText(questionJson.getString("text"));
            question.setId(questionJson.getNumber("id").longValue());

            quizz.setQuestion(question);
        }

        final JsonArray answersJson = object.getArray("answers");
        if(answersJson != null) {
            answersJson.forEach(answerJson -> {
                final Answer answer = new Answer();
                answer.setQuizz(quizz);
                answer.setId(((JsonObject) answerJson).getNumber("id").longValue());
                answer.setText(((JsonObject) answerJson).getString("text"));
                answer.setCorrect(((JsonObject) answerJson).getBoolean("correct"));

                quizz.getAnswers().add(answer);
            });
        }

        return quizz;
    }
}

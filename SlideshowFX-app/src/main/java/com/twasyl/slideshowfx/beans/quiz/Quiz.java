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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.Arrays;
import java.util.List;

/**
 * This class represents a quiz that can be performed during a presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class Quiz {

    private LongProperty id = new SimpleLongProperty();
    private ObjectProperty<Question> question = new SimpleObjectProperty<>();
    private ListProperty<Answer> answers = new SimpleListProperty<>(FXCollections.observableArrayList());

    /**
     * The property for the ID of this quiz.
     * @return The property for the ID of this quiz.
     */
    public LongProperty idProperty() { return id; }

    /**
     * The ID of this quiz.
     * @return The ID of this quiz.
     */
    public long getId() { return id.get(); }

    /**
     * Set the ID for this quiz.
     * @param id The new ID for this quiz.
     */
    public void setId(long id) { this.id.set(id); }

    /**
     * Get the question of this quiz.
     * @return The question of this quiz.
     */
    public Question getQuestion() { return question.get(); }

    /**
     * The question of this quiz.
     * @return The property associated to this quiz.
     */
    public ObjectProperty<Question> questionProperty() { return question; }

    /**
     * Set the question of this quiz.
     * @param question The question of this quiz.
     */
    public void setQuestion(Question question) {
        this.question.set(question);
    }

    /**
     * The property containing the possible answers of this quiz.
     * @return The property containing the possible answers for this quiz.
     */
    public ListProperty<Answer> answersProperty() { return answers; }

    /**
     * Get the possible answers for this quiz.
     * @return The list of possible answers.
     */
    public ObservableList<Answer> getAnswers() { return answers.get(); }

    /**
     * Set the possible answers for this quiz.
     * @param answers The list of possible answers for this quiz.
     */
    public void setAnswers(ObservableList<Answer> answers) { this.answers.set(answers); }

    /**
     * Get all correct answers for this quiz.
     * @return The list containing only the correct answers for this quiz.
     */
    public FilteredList<Answer> getCorrectAnswers() { return this.getAnswers().filtered(answer -> answer.isCorrect()); }

    /**
     * Performs a check to know if the quiz is answered correctly. This method checks if all correct answers contained
     * in this quiz has its ID referenced in the given {@code answersIDs} collection.
     *
     * @param answersIDs All answers' ID the user has checked.
     * @return <code>true</code> if all correct answers of this quiz has its ID referenced in {@code answersIDs},
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
     * Convert this Quiz object into a JSON object.
     * @return The JSON object representing this quiz
     */
    public JsonObject toJSON() {
        final JsonObject object = new JsonObject();
        object.put("id", this.getId());
        object.put("correctAnswers", this.getCorrectAnswers().size());

        if(this.getQuestion() != null) {
            final JsonObject questionObject = new JsonObject();
            questionObject.put("id", this.getQuestion().getId());
            questionObject.put("text", this.getQuestion().getText());

            object.put("question", questionObject);
        }

        if(!this.getAnswers().isEmpty()) {
            final JsonArray answersArray = new JsonArray();

            for(Answer answer : this.getAnswers()) {
                answersArray.add(new JsonObject()
                                        .put("id", answer.getId())
                                        .put("text", answer.getText())
                                        .put("correct", answer.isCorrect()));
            }

            object.put("answers", answersArray);
        }

        return object;
    }

    /**
     * Converts this{@link Quiz} as a JSON representation.
     * @return The JSON string representation of this quiz.
     */
    public String toJSONString() {
        return toJSON().encode();
    }

    /**
     * Build a Quiz object from the given JSON string.
     * @param json The JSON string representing the quiz.
     * @return The created Quiz object.
     * @throws java.lang.NullPointerException if {@code json} is {@code null}.
     * @throws java.lang.IllegalArgumentException if {@code json} is empty.
     */
    public static Quiz build(final String json) {
        if(json == null) throw new NullPointerException("The JSON string can not be null");
        if(json.trim().isEmpty()) throw new IllegalArgumentException("The JSON string is empty");

        final JsonObject object = new JsonObject(json);
        final Quiz quiz = new Quiz();

        quiz.setId(object.getLong("id"));

        final JsonObject questionJson = object.getJsonObject("question");
        if(questionJson != null) {
            final Question question = new Question();
            question.setQuiz(quiz);
            question.setText(questionJson.getString("text"));
            question.setId(questionJson.getLong("id"));

            quiz.setQuestion(question);
        }

        final JsonArray answersJson = object.getJsonArray("answers");
        if(answersJson != null) {
            answersJson.forEach(answerJson -> {
                final Answer answer = new Answer();
                answer.setQuiz(quiz);
                answer.setId(((JsonObject) answerJson).getLong("id"));
                answer.setText(((JsonObject) answerJson).getString("text"));
                answer.setCorrect(((JsonObject) answerJson).getBoolean("correct"));

                quiz.getAnswers().add(answer);
            });
        }

        return quiz;
    }
}

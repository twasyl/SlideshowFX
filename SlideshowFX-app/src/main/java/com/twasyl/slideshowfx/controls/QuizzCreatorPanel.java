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

package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.beans.quizz.Answer;
import com.twasyl.slideshowfx.beans.quizz.Question;
import com.twasyl.slideshowfx.beans.quizz.Quizz;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import java.util.Base64;

/**
 * This class is used to display a panel that will provide everything needed in order to create a quizz.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class QuizzCreatorPanel extends BorderPane {

    private final ObjectProperty<Quizz> quizz = new SimpleObjectProperty<>();
    private final VBox answersBox = new VBox(5);
    private final Button addAnswer = new Button();

    public QuizzCreatorPanel() {

        this.quizz.set(new Quizz());
        this.quizz.get().setId(System.currentTimeMillis());

        // Initialize the question
        this.quizz.get().setQuestion(new Question());
        this.quizz.get().getQuestion().setQuizz(this.quizz.get());
        this.quizz.get().getQuestion().setId(System.currentTimeMillis());

        // Initialize the UI
        this.addAnswer();

        final ImageView icon = new ImageView(new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/quizz.png")));

        final TextArea questionText = new TextArea();
        questionText.setPromptText("Question ?");
        questionText.setWrapText(true);
        questionText.setPrefColumnCount(25);
        questionText.setPrefRowCount(2);
        questionText.setTooltip(new Tooltip("Enter the text of the question"));
        questionText.textProperty().bindBidirectional(this.quizz.get().getQuestion().textProperty());

        this.addAnswer.setGraphic(new ImageView(new Image(ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/add_button.png"), 15, 15, true, true)));
        this.addAnswer.getStyleClass().add("image");
        this.addAnswer.setTooltip(new Tooltip("Add an answer to this quizz"));
        this.addAnswer.setOnAction(event -> this.addAnswer());

        final ScrollPane answersScrollPane = new ScrollPane(this.answersBox);
        answersScrollPane.setPrefSize(400, 300);

        final HBox topContent = new HBox(5, icon, questionText);

        this.answersBox.setPadding(new Insets(10, 0, 0, 0));

        this.setPadding(new Insets(10, 10, 10, 10));
        this.setTop(topContent);
        this.setCenter(answersScrollPane);
    }

    /**
     * Add an answer to this {@link #quizz}. This method creates an {@link com.twasyl.slideshowfx.beans.quizz.Answer}
     * object and binds it to the elements that are used to specify the text of it and if it is considered as a right
     * answer.
     * The method also updates this panel with elements used to define the answer. The answer is also added to this
     * quizz.
     */
    private void addAnswer() {
        final Answer answer = new Answer();
        answer.setQuizz(this.quizz.get());
        answer.setId(System.currentTimeMillis());

        this.quizz.get().getAnswers().add(answer);

        final TextField answerText = new TextField();
        answerText.setPromptText("Answer");
        answerText.setPrefColumnCount(25);
        answerText.setTooltip(new Tooltip("Enter the text for this answer"));
        answerText.textProperty().bindBidirectional(answer.textProperty());

        final CheckBox isCorrect = new CheckBox();
        isCorrect.setTooltip(new Tooltip("Check if this answer is considered as a correct answer"));
        isCorrect.selectedProperty().bindBidirectional(answer.correctProperty());

        if(this.addAnswer.getParent() != null) {
            PlatformHelper.run(() -> ((HBox) this.addAnswer.getParent()).getChildren().remove(this.addAnswer));
        }

        final HBox answerContainer = new HBox(5, answerText, isCorrect, this.addAnswer);
        answerContainer.setAlignment(Pos.CENTER_LEFT);

        PlatformHelper.run(() -> this.answersBox.getChildren().add(answerContainer));
    }

    /**
     * This methods converts the {@link #quizz} as a HTML code fragment that can be inserted in a slide.
     * @return The String representing the HTML code fragment to be inserted in a slide.
     */
    public String convertToHtml() {
        final Element divElement = new Element(Tag.valueOf("div"), "");
        divElement.attr("id", "quizz-" + this.quizz.get().getId())
                .addClass("slideshowfx-quizz")
                .appendElement("span")
                .attr("id", System.currentTimeMillis() + "")
                .appendText(this.quizz.get().getQuestion().getText());

        final Element ulElement = new Element(Tag.valueOf("ul"), "");
        this.quizz.get().getAnswers().forEach(answer -> {
            ulElement.appendElement("li")
                    .appendText(answer.getText());
        });

        final Element startButton = new Element(Tag.valueOf("button"), "");
        startButton.attr("onclick", "javascript:startQuizz('" + Base64.getEncoder().encodeToString(this.quizz.get().toJSON().encode().getBytes()) + "');")
                .attr("id", "startQuizzBtn-" + this.quizz.get().getId())
                .appendText("Start");

        final Element stopButton = new Element(Tag.valueOf("button"), "");
        stopButton.attr("onclick", "javascript:stopQuizz(" + this.quizz.get().getId() + ");")
                .attr("id", "stopQuizzBtn-" + this.quizz.get().getId())
                .appendText("Stop");

        divElement.appendChild(ulElement);
        divElement.appendChild(startButton).appendChild(stopButton);

        return divElement.toString();
    }

    /**
     * Get the Quizz associated to this panel. The quizz is fully initialized and doesn't need further configuration.
     * The Quizz is never null.
     * @return The property containing the Quizz.
     */
    public ObjectProperty<Quizz> quizzProperty() { return quizz; }

    /**
     * Get the Quizz associated to this panel. The quizz is fully initialized and doesn't need further configuration.
     * The Quizz is never null.
     * @return The Quizz.
     */
    public Quizz getQuizz() {  return quizz.get(); }

}

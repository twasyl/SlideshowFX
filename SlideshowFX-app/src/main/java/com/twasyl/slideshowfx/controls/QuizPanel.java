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

package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.beans.quiz.QuizResult;
import com.twasyl.slideshowfx.utils.PlatformHelper;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.Region;

/**
 * This class is used in order to display a Pie chart indicating the results for a current quiz.
 * This panel should be used in a {@link com.twasyl.slideshowfx.controls.slideshow.SlideshowPane}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class QuizPanel extends Region {

    private final ObjectProperty<QuizResult> quizResult = new SimpleObjectProperty<>();
    private final ObjectProperty<PieChart> chart = new SimpleObjectProperty<>();

    /**
     * Creates the {@link QuizPanel} and initialize the chart that is displayed into it.
     */
    public QuizPanel() {
        this.getStylesheets().add(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/css/quiz-panel.css"));

        this.chart.set(new PieChart());

        this.chart.get().prefWidthProperty().bind(this.prefWidthProperty());
        this.chart.get().prefHeightProperty().bind(this.prefHeightProperty());
        this.chart.get().setLabelsVisible(false);

        this.quizResult.addListener((value, oldQuizResult, newQuizResult) -> {
            this.chart.get().getData().clear();

            if(newQuizResult != null) {

                PlatformHelper.run(() -> {
                    PieChart.Data correctAnswers = new PieChart.Data("Correct answers", 0);
                    correctAnswers.pieValueProperty().bind(newQuizResult.correctAnswersProperty());

                    PieChart.Data wrongAnswers = new PieChart.Data("Wrong answers", 0);
                    wrongAnswers.pieValueProperty().bind(newQuizResult.wrongAnswersProperty());

                    this.chart.get().setData(FXCollections.observableArrayList(correctAnswers, wrongAnswers));
                    this.chart.get().setTitle(newQuizResult.getQuiz().getQuestion().getText());
                });
            }
        });

        this.getChildren().add(this.chart.get());
    }

    /**
     * Creates the {@link QuizPanel} and initialize the chart that is displayed into it with the provided result.
     * @param result The result that is associated to this panel
     */
    public QuizPanel(QuizResult result) {
        this();
        this.quizResult.set(result);
    }

    /**
     * The result that is attached to this panel.
     * @return The {@link QuizPanel} associated to this panel.
     */
    public ObjectProperty<QuizResult> quizResultProperty() { return quizResult; }

    /**
     * The result that is attached to this panel.
     * @return The {@link QuizResult} associated to this panel.
     */
    public QuizResult getQuizResult() { return quizResult.get(); }

    /**
     * Set the {@link QuizPanel} associated to this panel.
     * @param quizResult The new result associated to this panel
     */
    public void setQuizResult(QuizResult quizResult) { this.quizResult.set(quizResult); }
}

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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Perform tests on a quizz.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class QuizzTest {

    private static Quizz quizz;

    @BeforeClass
    public static void setUp() {

        quizz = new Quizz();
        quizz.setId(System.currentTimeMillis());

        Question question = new Question();
        question.setText("My question");
        question.setQuizz(quizz);
        question.setId(System.currentTimeMillis());

        Answer answer1 = new Answer();
        answer1.setText("Answer 1");
        answer1.setCorrect(false);
        answer1.setQuizz(quizz);
        answer1.setId(1L);

        Answer answer2 = new Answer();
        answer2.setText("Answer 2");
        answer2.setCorrect(true);
        answer2.setQuizz(quizz);
        answer2.setId(2L);

        Answer answer3 = new Answer();
        answer3.setText("Answer 3");
        answer3.setCorrect(true);
        answer3.setQuizz(quizz);
        answer3.setId(3L);

        quizz.setQuestion(question);
        quizz.getAnswers().addAll(answer1, answer2, answer3);
    }

    @Test
    public void testWithoutAnswers() {
        Assert.assertFalse(quizz.checkAnswers());
    }

    @Test
    public void testWithFalseAnswer() {
        Assert.assertFalse(quizz.checkAnswers(1L));
    }

    @Test
    public void testWithoutAllCorrectAnswers() {
        Assert.assertFalse(quizz.checkAnswers(2L));
    }

    @Test
    public void testWithAllCorrectAnswersAndAWrongAnswer() {
        Assert.assertFalse(quizz.checkAnswers(1L, 2L, 3L));
    }

    @Test
    public void testWithCorrectAnswers() {
        Assert.assertTrue(quizz.checkAnswers(2L, 3L));
    }
}

package com.twasyl.slideshowfx.server.beans.quiz;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Perform tests on a quiz.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class QuizTest {

    private static Quiz quiz;

    @BeforeAll
    public static void setUp() {

        quiz = new Quiz();
        quiz.setId(System.currentTimeMillis());

        Question question = new Question();
        question.setText("My question");
        question.setQuiz(quiz);
        question.setId(System.currentTimeMillis());

        Answer answer1 = new Answer();
        answer1.setText("Answer 1");
        answer1.setCorrect(false);
        answer1.setQuiz(quiz);
        answer1.setId(1L);

        Answer answer2 = new Answer();
        answer2.setText("Answer 2");
        answer2.setCorrect(true);
        answer2.setQuiz(quiz);
        answer2.setId(2L);

        Answer answer3 = new Answer();
        answer3.setText("Answer 3");
        answer3.setCorrect(true);
        answer3.setQuiz(quiz);
        answer3.setId(3L);

        quiz.setQuestion(question);
        quiz.getAnswers().addAll(answer1, answer2, answer3);
    }

    @Test
    public void testWithoutAnswers() {
        assertFalse(quiz.checkAnswers());
    }

    @Test
    public void testWithFalseAnswer() {
        assertFalse(quiz.checkAnswers(1L));
    }

    @Test
    public void testWithoutAllCorrectAnswers() {
        assertFalse(quiz.checkAnswers(2L));
    }

    @Test
    public void testWithAllCorrectAnswersAndAWrongAnswer() {
        assertFalse(quiz.checkAnswers(1L, 2L, 3L));
    }

    @Test
    public void testWithCorrectAnswers() {
        assertTrue(quiz.checkAnswers(2L, 3L));
    }
}
